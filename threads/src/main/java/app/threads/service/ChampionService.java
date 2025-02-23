package app.threads.service;

import app.threads.database.DatabaseBackup;
import app.threads.exception.ChampionNotFoundException;
import app.threads.model.Champion;
import app.threads.repository.ChampionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChampionService {

    ChampionRepository championRepository;
    DatabaseBackup databaseBackup;

    public ChampionService(ChampionRepository championRepository
                            ,DatabaseBackup databaseBackup){

        this.championRepository = championRepository;
        this.databaseBackup = databaseBackup;
    }

    public List<Champion> findAll(){
        List<Champion> champions = championRepository.findAll();

        return champions;
    }

    public Champion findById(Long id){
        Optional<Champion> optionalChampion = championRepository.findById(id);

        return optionalChampion.orElseThrow(() -> new ChampionNotFoundException(id));
    }

    public boolean existsById(Long id){
        if(databaseBackup.dbCorrupted()){
            sleep(5500); /* Enough time to let the database restore */
        }

        return championRepository.existsById(id);
    }

    public Champion save(Champion champion){
        if(databaseBackup.dbCorrupted()){
            sleep(5500); /* Enough time to let the database restore */
        }

        Champion newChampion = championRepository.save(champion);

        databaseBackup.createBackup();

        return newChampion;

    }

    public void deleteAll(){
        championRepository.deleteAll();
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

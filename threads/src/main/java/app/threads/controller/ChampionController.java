package app.threads.controller;

import app.threads.database.DatabaseBackup;
import app.threads.model.Champion;
import app.threads.service.ChampionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/v1/champions")
public class ChampionController {

    ChampionService championService;
    DatabaseBackup databaseBackup;

    public ChampionController(ChampionService championService, DatabaseBackup databaseBackup){
        this.championService = championService;
        this.databaseBackup = databaseBackup;
    }

    @GetMapping
    public ResponseEntity<List<Champion>> findAll(){
        List<Champion> champions = championService.findAll();

        return ResponseEntity.ok(champions);
    }

    @GetMapping("/{requestedId}")
    public ResponseEntity<Champion> findById(@PathVariable Long requestedId){
        Champion foundChampion = championService.findById(requestedId);

        return ResponseEntity.ok(foundChampion);
    }

    @PostMapping
    public ResponseEntity<Champion> saveChampion(@RequestBody Champion champion, UriComponentsBuilder ucb){
        Champion newChampion = championService.save(champion);

        URI newChampionLocation = ucb
                .path("/api/v1/champions/{id}")
                .buildAndExpand(newChampion.getId())
                .toUri();

        return ResponseEntity.created(newChampionLocation).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Champion> updateChampion(@PathVariable Long id,
                                                   @RequestBody Champion champion
                                                   , UriComponentsBuilder ucb){

        if(championService.existsById(id)){
           championService.save(champion);

           return ResponseEntity.noContent().build();
        }
        else{
            Champion newChampion = championService.save(champion);
            URI newChampionLocation = ucb
                    .path("/api/v1/champions/{id}")
                    .buildAndExpand(newChampion.getId())
                    .toUri();

            return ResponseEntity.created(newChampionLocation).build();
        }
    }

    @GetMapping("/backup")
    public ResponseEntity<String> makeBackup(){
        databaseBackup.createBackup();

        return ResponseEntity.ok("Backup completed succesfully!");
    }

    @GetMapping("/restore-db")
    public ResponseEntity<String> restoreDB(){
        databaseBackup.restoreDB();

        return ResponseEntity.ok("Database restored succesfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAll(){
        championService.deleteAll();

        return ResponseEntity.ok("Champions deleted");
    }

    /*
    * https://stackoverflow.com/questions/68220209/how-to-create-backup-of-a-mysql-database-in-java
    * https://www.springcloud.io/post/2022-03/commons-exec-backup-database/#google_vignette
    * https://mvnrepository.com/artifact/org.apache.commons/commons-exec/1.4.0
    * https://www.youtube.com/watch?v=JeXl60PbXXw
    * https://github.com/CristopherLodbrok117/silvia-care/blob/main/backend/silvia_care/src/main/java/com/silvia_care/notes/NoteController.java
    * */
}

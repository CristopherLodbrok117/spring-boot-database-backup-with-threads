package app.threads.database;

import app.threads.model.Champion;
import app.threads.repository.ChampionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    private final ChampionRepository championRepository;
    private final DatabaseBackup databaseBackup;

    public LoadDatabase(ChampionRepository championRepository, DatabaseBackup databaseBackup){
        this.championRepository = championRepository;
        this.databaseBackup = databaseBackup;
    }

    @Bean
    CommandLineRunner initDatabase(){
        return args -> {
            Champion jax = Champion.builder()
                    .name("Jax")
                    .health(2200)
                    .ad(150)
                    .ap(30)
                    .active(true)
                    .build();

            Champion lux = Champion.builder()
                    .name("Lux")
                    .health(1900)
                    .ad(0)
                    .ap(670)
                    .active(true)
                    .build();

            Champion garen = Champion.builder()
                    .name("Garen")
                    .health(3700)
                    .ad(110)
                    .ap(0)
                    .active(true)
                    .build();

            log.info("Preloading: " + championRepository.save(jax));
            log.info("Preloading: " + championRepository.save(lux));
            log.info("Preloading: " + championRepository.save(garen));

            databaseBackup.createBackup();
            log.info("Database backup created");
        };
    }
}

package app.threads.database;

import app.threads.repository.ChampionRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
public class DatabaseBackup{

    private static final int INPUT_REDIRECT = 0; // For mysql restore
    private static final int OUTPUT_REDIRECT = 1; // For mysqldump backup

    ChampionRepository championRepository;

    public DatabaseBackup(ChampionRepository championRepository){
        this.championRepository = championRepository;
    }

    @Scheduled(fixedDelay =  5000)
    public void monitoringDatabase(){
        if(dbCorrupted()){
            System.out.println(LocalDateTime.now() + " - database corrupted! restoring...");
            restoreDB();
            System.out.println(LocalDateTime.now() + " - database restored succesfully!");
        }
        else{
            System.out.println(LocalDateTime.now() + " - database ok");
        }
        /*https://erkanyasun.medium.com/unlocking-spring-boots-scheduling-capabilities-with-scheduled-de5796408137*/
    }

    public boolean dbCorrupted(){
        return championRepository.count() <= 0;
    }

    public void createBackup(){
        String mysqldump = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump";
        ProcessBuilder processBuilder = getProcessBuilder(mysqldump, OUTPUT_REDIRECT);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Backup successful!");
            } else {
                System.out.println("Backup failed with exit code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error occurred during backup: " + e.getMessage());;
        }
    }



    public void restoreDB(){
        String mysql = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql";
        ProcessBuilder processBuilder = getProcessBuilder(mysql, INPUT_REDIRECT);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Database restored successfully!");
            } else {
                System.out.println("Restore failed with exit code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error occurred during restore: " + e.getMessage());;
        }
    }

    private static ProcessBuilder getProcessBuilder(String commandToExecute, int redirectionType) {

        String dbUser = "root";
        String dbPassword = "1234";
        String dbName = "threads_db";
        String backupPath = "C:\\Users\\kim_j\\Desktop\\service_backup.sql";

        List<String> command = new ArrayList<>();

        command.add(commandToExecute);
        command.add("-u" + dbUser);
        command.add("-p" + dbPassword);
        command.add(dbName);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        if(redirectionType == OUTPUT_REDIRECT){
            processBuilder.redirectOutput(new File(backupPath));
        }
        else{
            processBuilder.redirectInput(new File(backupPath));
        }

        return processBuilder;
    }

    // Another choice: https://github.com/SeunMatt/mysql-backup4j
}

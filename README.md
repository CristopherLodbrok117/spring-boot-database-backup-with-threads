# Database backup with threads in spring boot 

<br>

## Creación de la API

Dependencias:
- Spring web
- MySQL Driver
- Spring Data JPA
- Lombok

1. Implementaremos el CRUD de nuestra aplicación de manera normal y configuramos el aplication properties
2. Creamos la base de datos con MySQL Workbench
```sql
CREATE DATAABSE threads_db;
```
3. Para propositos ilustrativos añadimos un endpoint que nos permita eliminar de manera física todos los registros. En un supuesto donde la eliminación deberia ser lógica, al eliminar de manera física se estaria corrompiendo la integridad de la base de datos.

<br>

## Threads

Utilizaremos dos anotaciones para trabajar con hilos en Spring Boot.
1. `@EnableScheduling`: habilita la ejecución de tareas programadas en la aplicación. Esta anotación se escribe en la clase
2. `@Scheduled`: por defecto, cuando se usa @Scheduled en un método, Spring lo ejecuta en un hilo separado del hilo principal. Sin embargo todos los métodos anotados con @Scheduled. Es decir, todos los métodos que tengan esta anotación seran parte del mismo hilo.

En nuestro escenario es suficiente tener un hilo trabajando en paralelo al principal, que se encargara de verificar y restaurar el estado de la base de datos. Sin embargo, si se desea tener un hilo por cada tarea programada (@Scheduled) sin bloquearse entre sí, se puede configurar un [ThreadPoolTaskScheduler ](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/concurrent/ThreadPoolTaskScheduler.html).

<br>

## Backup, restore y ProcessBuilder

Este código se encarga de crear un backup y restaurar una base de datos MySQL utilizando mysqldump y mysql. Se apoya en ProcessBuilder para ejecutar estos comandos en el sistema operativo.

Definimos el criterio para detectar si la base de datos fue alterada. En este caso utilizaremos la cuenta de registros de la tabla de campeones
`championRepository.count()`

### monitoringDB
```java
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
```
Creamos un hilo con @Scheduled que verifica cada 5 segundos el estado de la base de datos. Si deseamos ajustar el intervalo en que se ejecutara esta tarea, modificamos la propiedad fixedDelay de la anotación.

<br>

### ChampionService::save
```java
public Champion save(Champion champion){
        if(databaseBackup.dbCorrupted()){
            sleep(5500); /* Enough time to let the database restore */
        }

        Champion newChampion = championRepository.save(champion);

        databaseBackup.createBackup();

        return newChampion;

    }
```
En la capa de lógica de la aplicación (service) verificamos si la base de datos fue corrompida, evitando nuevos registros hasta que haya sido restaurada con exito

<br>

### backupDB
```java
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
```
- Define la ruta de mysqldump, que es la herramienta de MySQL para exportar bases de datos.
- Llama a getProcessBuilder() pasando la ruta de mysqldump y OUTPUT_REDIRECT (para indicar que estamos creando un backup).
- Ejecuta el proceso con processBuilder.start().
- Espera a que el proceso termine con process.waitFor(), y verifica si el código de salida (exitCode) es 0 (éxito) o distinto de 0 (error).
- Maneja excepciones en caso de que haya problemas al ejecutar el proceso.

<br>

### restoreDB
```java
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
```
- Define la ruta de mysql, que es la herramienta para importar bases de datos.
- Llama a getProcessBuilder() pasando la ruta de mysql y INPUT_REDIRECT (para restaurar la base de datos desde un archivo).
- Ejecuta el proceso y espera a que termine (process.waitFor()).
- Imprime un mensaje dependiendo del código de salida (exitCode).
- Maneja excepciones en caso de errores.

<br>

### getProcessBuilder
```java
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
```
- Define las credenciales de la base de datos y la ubicación del archivo de backup.
- Construye una lista de comandos (command), añadiendo:
1. El comando (mysqldump o mysql).
2. El usuario de la BD (-uroot).
3. La contraseña (-p1234).
4. El nombre de la base de datos.

- Crea un ProcessBuilder con esta lista de comandos.
- Configura la redirección de entrada/salida:
- Si es OUTPUT_REDIRECT → Redirige la salida del comando (mysqldump) a un archivo SQL.
- Si es INPUT_REDIRECT → Usa un archivo SQL como entrada para mysql (restauración).
- Retorna el ProcessBuilder, que será usado en createBackup() o restoreDB().

<br>

## Ejecución y pruebas

Desde [Insomnia](https://insomnia.rest/) creamos las request con las que probaremos los endpoints de la API 

1. Corremos la API

![run API](https://github.com/CristopherLodbrok117/spring-boot-database-backup-with-threads/blob/c75f098ba17083384c49ad67e25737763ad26fad/screens/0%20-%20Running%20API.png)

2. 

![show all]()

3. Mostramos todos los registros actuales (GET)

![]()

4. Intentamos obtener un registro no existente

![]()

5. Añadimos un nuevo registro

![]()

6. Tras crear un nuevo registro se hace backup

![]()

7. Verificamos que se creo correctamente el nuevo campeón

![]()

8. Utilizamos el endpoint para hacer una eliminación física

![]()

El hilo detecta que la base de datos se corrompe y la restaura

# Database backup with threads in spring boot 

<br>

## Creación de la API

Dependencias
- Spring web
- MySQL Driver
- Spring Data JPA
- Lombok

Implementaremos el CRUD de nuestra aplicación de manera normal y configuramos el aplication properties

Creamos la base de datos con MySQL Workbench
CREATE DATAABSE threads_db;

Para propositos ilustrativos añadimos un endpoint que nos permita eliminar de manera física todos los registros. En un supuesto donde la eliminación deberia ser lógica, al eliminar de manera física se estaria corrompiendo la integridad de la base de datos.

<br>

## Threads

Utilizaremos dos anotaciones para trabajar con hilos en Spring Boot.
1. @EnableScheduling: habilita la ejecución de tareas programadas en la aplicación. Esta anotación se escribe en la clase
2. @Scheduled: por defecto, cuando se usa @Scheduled en un método, Spring lo ejecuta en un hilo separado del hilo principal. Sin embargo todos los métodos anotados con @Scheduled. Es decir, todos los métodos que tengan esta anotación seran parte del mismo hilo.

En nuestro escenario es suficiente tener un hilo trabajando en paralelo al principal, que se encargara de verificar y restaurar el estado de la base de datos. Sin embargo, si se desea tener un hilo por cada tarea programada (@Scheduled) sin bloquearse entre sí, se puede configurar un ThreadPoolTaskScheduler.

<br>

## Backup, restore y ProcessBuilder

Este código se encarga de crear un backup y restaurar una base de datos MySQL utilizando mysqldump y mysql. Se apoya en ProcessBuilder para ejecutar estos comandos en el sistema operativo.

Definimos el criterio para detectar si la base de datos fue alterada. En este caso utilizaremos la cuenta de registros de la tabla de campeones
championRepository.count()

monitoringDB
Creamos un hilo con @Scheduled que verifica cada 5 segundos el estado de la base de datos. Si deseamos ajustar el intervalo en que se ejecutara esta tarea, modificamos la propiedad fixedDelay de la anotación.

service - create
En la capa de lógica de la aplicación (service) verificamos si la base de datos fue corrompida, evitando nuevos registros hasta que haya sido restaurada con exito

backupDB
Define la ruta de mysqldump, que es la herramienta de MySQL para exportar bases de datos.
Llama a getProcessBuilder() pasando la ruta de mysqldump y OUTPUT_REDIRECT (para indicar que estamos creando un backup).
Ejecuta el proceso con processBuilder.start().
Espera a que el proceso termine con process.waitFor(), y verifica si el código de salida (exitCode) es 0 (éxito) o distinto de 0 (error).
Maneja excepciones en caso de que haya problemas al ejecutar el proceso.


restoreDB
Define la ruta de mysql, que es la herramienta para importar bases de datos.
Llama a getProcessBuilder() pasando la ruta de mysql y INPUT_REDIRECT (para restaurar la base de datos desde un archivo).
Ejecuta el proceso y espera a que termine (process.waitFor()).
Imprime un mensaje dependiendo del código de salida (exitCode).
Maneja excepciones en caso de errores.

getProcessBuilder
Define las credenciales de la base de datos y la ubicación del archivo de backup.
Construye una lista de comandos (command), añadiendo:
El comando (mysqldump o mysql).
El usuario de la BD (-uroot).
La contraseña (-p1234).
El nombre de la base de datos.
Crea un ProcessBuilder con esta lista de comandos.
Configura la redirección de entrada/salida:
Si es OUTPUT_REDIRECT → Redirige la salida del comando (mysqldump) a un archivo SQL.
Si es INPUT_REDIRECT → Usa un archivo SQL como entrada para mysql (restauración).
Retorna el ProcessBuilder, que será usado en createBackup() o restoreDB().

<br>

## Ejecución y pruebas

Corremos la API

Desde Insomnia creamos las request con las que probaremos los endpoints de la API

Mostramos todos los registros actuales (GET)

Intentamos obtener un registro no existente

Añadimos un nuevo registro

Tras crear un nuevo registro se hace backup

Verificamos que se creo correctamente el nuevo campeón

Utilizamos el endpoint para hacer una eliminación física

El hilo detecta que la base de datos se corrompe y la restaura

# Shared Folder - University project

## Shared Folder Client
Client side of Shared Folder application <br>
Used to connect to the shared folder server <br>
java based application (swing with spring boot)

## Pre Installation
Make sure you have JAVA 11 installed <br>
run in terminal ```java -version```
to validate your java version 

## Running
run ```java -jar shared-folder-client.jar``` from command line


## Environment Variables
| Name                     | Description                                     | Default value                           |
| :---:                    | :---:                                           | :---:                                   |
| `LOG_LEVEL`              | Log level                                       | `INFO`                                  |
| `SHARED_FOLDER_URL`      | Url of the shared folder server                 | `http://localhost:8080`                 |

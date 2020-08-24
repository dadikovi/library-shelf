# library-shelf



## Development

To start your application in the dev profile, run:

```
./mvnw
```


### Doing API-First development using openapi-generator

[OpenAPI-Generator]() is configured for this application. You can generate API code from the `src/main/resources/swagger/api.yml` definition file by running:

```bash
./mvnw generate-sources
```

Then implements the generated delegate classes with `@Service` classes.

To edit the `api.yml` definition file, you can use a tool such as [Swagger-Editor](). Start a local instance of the swagger-editor using docker by running: `docker-compose -f src/main/docker/swagger-editor.yml up -d`. The editor will then be reachable at [http://localhost:7742](http://localhost:7742).

Refer to [Doing API-First development][] for more details.

### Running the application

1. Build the docker images

    ```
    ./mvnw -Pprod verify jib:dockerBuild
    ```

2. Run docker compose

    ```
    docker-compose -f src/main/docker/app.yml up -d
    ```
   
3. Access of application
    - jhipster registry (eureka, configserver, springbootadmin, swagger UI): localhost:8761   (admin / admin)

# library-shelf

This microservice manages the books in the library.

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

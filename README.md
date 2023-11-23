# <img height="25" src="./images/AILLogoSmall.png" width="40"/> order-service

<a href="https://www.legosoft.com.mx"><img height="150px" src="./images/Icon.png" alt="AI Legorreta" align="left"/></a>
Microservice that is a part for an `Ingestor` demonstration. The user using the `acme-ui` micro-front creates a 
datasource for new orders. The `python ingestor` microservice reads a `cvs` file parse it using `Spark`,
validates the fields (according to the rules defined in the datasource) and the correct record send a 
kafka event that it is listened by this microservice.

The `order-service` depends on `Kafka` events generated by the `Python ingestor` microservice.


## Introduction

This is a microservice for demo purpose to test de Ingestor microservice in a high volume fashion transactions.

This microservice receives a bunch of `Kafka` events from `PhySpark` to insert new orders in a Neo4j
database (in this case is the acmeDB);. We test how this microservice behaves with volume of transactions.

And also has the functionality to query the orders in the `acme-ui` microservice.

note: The purpose is to create a table for time response for thousands of transaction with more than one
      `spark-workers` instances.

### Send Events

No events are sent via  **Kafka** it is just a listener for `ordenes` topic.


### Databases

No database is used directly for this microservice:

- The bupNeo4j is handled by the `bup-service` microservice.

### Orders queries from Neo4jDB

This microservice utilizes bupDB to store in the BUP database, GraphQL as an API to query parameters
a Spring GraphQL and Neo4j GraphQL Java library.

note: NOT use Spring Data and Spring Neo4j Data libraries (i.e., repositories). All is done in GraphQL 
which we believe is much better solution.

For more information see example:

graphql-spring-boot or visit link:

https://github.com/neo4j-graphql/neo4j-graphql-java/tree/master/examples/graphql-spring-boot

Or the official repository (see examples) for Spring for GraphQL

https://github.com/spring-projects/spring-graphql


### Orders table

For simplicity of the example and not create a new RDBMS we use Neo4j to store the orders table. 
No relationships exist for this table.

### Schema

For developers, we can generate at compile time the schema augmentation for debug purpose. This is done with the
`neo4j-graphql-augmented-schema-generator-maven-plugin`. Since there is not Gradle plugin you can do the following
process:

(a) In Gradle use the `maven-publish` plugin with the `publish` task goal. When you run:

```
./gradlew publish
```

A pom file is created in the directory ./build/publications with the name `pom-default.xml`.

(b) The file was copied in the project directory and modified to include the `neo4j-graphql-augmented-schema-generator-maven-plugin`
and comment some dependencies from the maven local repository (i.e., ailegorreta-kit)

(c) The run the goal
```
mvn -f pom-default.xml compile
```

To create tha augmented schema in the directory `/target/augmented-schem/neo4j.graphql`.

note: This process has to be done every time the schema is modified.

### Events listener

Listener any event with the topic `ordenes`

  
## Running on Docker Desktop

### Create the image manually

```
./gradlew bootBuildImage
```

### Publish the image to GitHub manually

```
./gradlew bootBuildImage \
   --imageName ghcr.io/rlegorreta/order-service \
   --publishImage \
   -PregistryUrl=ghcr.io \
   -PregistryUsername=rlegorreta \
   -PregistryToken=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
```

### Publish the image to GitHub from the IntelliJ

To publish the image to GitHub from the IDE IntelliJ a file inside the directory `.github/workflows/commit-stage.yml`
was created.

To validate the manifest file for kubernetes run the following command:

```
kubeval --strict -d k8s
```

This file compiles de project, test it (for this project is disabled for some bug), test vulnerabilities running
skype, commits the code, sends a report of vulnerabilities, creates the image and lastly push the container image.

<img height="340" src="./images/commit-stage.png" width="550"/>

For detail information see `.github/workflows/commit-stage.yml` file.


### Run the image inside the Docker desktop

```
docker run \
    --net ailegorretaNet \
    -p 8530:8530 \
    -e SPRING_PROFILES_ACTIVE=local \
    order-service
```

Or a better method use the `docker-compose` tool. Go to the directory `ailegorreta-deployment/docker-platform` and run
the command:

```
docker-compose up
```

## Run inside Kubernetes

### Manually

If we do not use the `Tilt`tool nd want to do it manually, first we need to create the image:

Fist step:

```
./gradlew bootBuildImage
```

Second step:

Then we have to load the image inside the minikube executing the command:

```
image load ailegorreta/order-service --profile ailegorreta 
```

To verify that the image has been loaded we can execute the command that lists all minikube images:

```
kubectl get pods --all-namespaces -o jsonpath="{..image}" | tr -s '[[:space:]]' '\n' | sort | uniq -c\n
```

Third step:

Then execute the deployment defined in the file `k8s/deployment.yml` with the command:

```
kubectl apply -f k8s/deployment.yml
```

And after the deployment can be deleted executing:

```
kubectl apply -f k8s/deployment.yml
```

Fourth step:

For service discovery we need to create a service applying with the file: `k8s/service.yml` executing the command:

```
kubectl apply -f k8s/service.yml
```

And after the process we can delete the service executing:

```
kubectl deltete -f k8s/service.yml
```

Fifth step:

If we want to use the project outside kubernetes we have to forward the port as follows:

```
kubectl port-forward service/order-service 8530:80
```

Appendix:

If we want to see the logs for this `pod` we can execute the following command:

```
kubectl logs deployment/order-service
```

### Using Tilt tool

To avoid all these boilerplate steps is much better and faster to use the `Tilt` tool as follows: first create see the
file located in the root directory of the project called `TiltFile`. This file has the content:

```
# Tilt file for order-service
# Build
custom_build(
    # Name of the container image
    ref = 'order-service',
    # Command to build the container image
    command = './gradlew bootBuildImage --imageName $EXPECTED_REF',
    # Files to watch that trigger a new build
    deps = ['build.gradle', 'src']
)

# Deploy
k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])

# Manage
k8s_resource('order-service', port_forwards=['8530'])
```

To execute all five steps manually we just need to execute the command:

```
tilt up
```

In order to see the log of the deployment process please visit the following URL:

```
http://localhost:10350
```

Or execute outside Tilt the command:

```
kubectl logs deployment/order-service
```

In order to undeploy everything just execute the command:

```
tilt down
```

To run inside a docker desktop the microservice need to use http://order-service:8530 to 8530 path


### Reference Documentation

* [Spring Boot Gateway](https://cloud.spring.io/spring-cloud-gateway/reference/html/)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.1/maven-plugin/reference/html/)
* [Config Client Quick Start](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_client_side_usage)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/3.0.1/reference/htmlsingle/#production-ready)

### Links to Springboot 3 Observability

https://tanzu.vmware.com/developer/guides/observability-reactive-spring-boot-3/

Baeldung:

https://www.baeldung.com/spring-boot-3-observability



### Contact AI Legorreta

Feel free to reach out to AI Legorreta on [web page](https://legosoft.com.mx).


Version: 2.0.0
©LegoSoft Soluciones, S.C., 2023
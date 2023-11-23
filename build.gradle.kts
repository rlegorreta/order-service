import org.gradle.internal.classpath.Instrumented.systemProperty
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.10"
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.10"
    kotlin("kapt") version "1.9.10"
    id("io.freefair.lombok") version "8.4"
    id("maven-publish")
}

group = "com.ailegorreta"
version = "2.0.0"
description = "Micro service for ingestor demo for adding new orders from Kafka events."

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

apply(plugin = "maven-publish")         // This is just to generate the augmented-schema (see README.md file)
apply(plugin = "io.freefair.lombok")

repositories {
    mavenLocal()
    mavenCentral()
     maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/" +
        project.findProperty("registryPackageUrl") as String? ?:
            System.getenv("URL_PACKAGE") ?:
            "rlegorreta/ailegorreta-kit")
        credentials {
            username = project.findProperty("registryUsername") as String? ?:
                    System.getenv("USERNAME") ?:
                    "rlegorreta"
            password = project.findProperty("registryToken") as String? ?: System.getenv("TOKEN")
        }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

extra["springCloudVersion"] = "2022.0.4"
extra["testcontainersVersion"] = "1.18.1"
extra["otelVersion"] = "1.26.0"
extra["ailegorreta-kit-version"] = "2.0.0"
extra["jsoup-version"] = "1.15.3"
extra["coroutines-version"] = "1.7.3"
extra["neo4j-driver.version"] = "4.3.6.0"
extra["neo4j-graphql-java.version"] = "1.8.0"
extra["neo4j-graphql-augmented-schema-generator-maven-plugin.version"] = "1.8.0"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")        // Reactive version

    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") // this includes reactive version
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.neo4j.driver:neo4j-java-driver-spring-boot-starter:${property("neo4j-driver.version")}")
    implementation("org.neo4j:neo4j-graphql-java:${property("neo4j-graphql-java.version")}")
    // implementation("org.neo4j:neo4j-graphql-augmented-schema-generator-maven-plugin:${property("neo4j-graphql-augmented-schema-generator-maven-plugin.version")}")
    // ^ Graphql dependency (optional) to generate de augmented graphql schema for debugging purpose

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client") {
        exclude(group = "org.springframework.cloud", module = "spring-cloud-starter-ribbon")
        exclude(group = "com.netflix.ribbon", module = "ribbon-eureka")
    }
    // ^ This library work just for docker container. Kubernetes ignores it (setting eureka.client.registerWithEureka
    // property to false

    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("coroutines-version")}")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${property("coroutines-version")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")

    implementation("com.ailegorreta:ailegorreta-kit-commons-utils:${property("ailegorreta-kit-version")}")
    implementation("com.ailegorreta:ailegorreta-kit-data-neo4j:${property("ailegorreta-kit-version")}")
    implementation("com.ailegorreta:ailegorreta-kit-commons-event:${property("ailegorreta-kit-version")}")
    implementation("com.ailegorreta:ailegorreta-kit-resource-server-security:${property("ailegorreta-kit-version")}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
    // testImplementation("io.projectreactor:reactor-test")                // this is for web-flux testing
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("io.mockk:mockk:1.9.3")

    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:neo4j:1.17.6")
    testImplementation("org.neo4j.driver:neo4j-java-driver:5.6.0")
    // ^ see hint in https://java.testcontainers.org/modules/databases/neo4j/
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.named<BootBuildImage>("bootBuildImage") {
    environment.set(environment.get() + mapOf("BP_JVM_VERSION" to "17.*"))
    imageName.set("ailegorreta/${project.name}")
    docker {
        publishRegistry {
            username.set(project.findProperty("registryUsername").toString())
            password.set(project.findProperty("registryToken").toString())
            url.set(project.findProperty("registryUrl").toString())
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        freeCompilerArgs += "-Xjvm-default=all-compatibility"            // needed to override default methods on interfaces
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.acme"
            artifactId = "order-service"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("order-service")
                description.set("Server repository for Neo4j for Orders only")
                properties.set(mapOf(
                    "version" to "2.0.0"
                ))
                developers {
                    developer {
                        id.set("rlh")
                        name.set("Ricardo Legorreta")
                        email.set("rlegorreta@legosoft.com.mx")
                    }
                }
            }

        }
    }

    repositories {
        maven {
            name = "sampleRepo"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}
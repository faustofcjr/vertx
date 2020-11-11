import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "io.vertx.microservice"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "3.9.4"
val junitJupiterVersion = "5.6.0"

val mainVerticleName = "io.vertx.microservice.MainVerticle"
val watchForChange = "src/**/*"
val doOnChange = "./gradlew classes"
val launcherClassName = "io.vertx.core.Launcher"

application {
  mainClassName = launcherClassName
}

dependencies {
  implementation("io.vertx:vertx-rx-java:$vertxVersion")
//  implementation("io.vertx:vertx-rx-java:$vertxVersion")
//  implementation("io.vertx:vertx-infinispan:$vertxVersion")
//  implementation("io.vertx:vertx-zookeeper:$vertxVersion")
//  implementation("io.vertx:vertx-ignite:$vertxVersion")
  implementation("io.vertx:vertx-hazelcast:$vertxVersion")
  implementation("io.vertx:vertx-zookeeper:$vertxVersion")
  testImplementation("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

  java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles {
    include("META-INF/services/io.vertx.core.spi.VerticleFactory")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange",  "-cluster")
}
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Properties

plugins {
    id("java")
    id("io.freefair.lombok") version "8.11"
    id("com.gradleup.shadow") version "9.2.2"
    application
}

group = "moe.vitamin.campuslink"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("moe.vitamin.campuslink.CampusLink")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")

    implementation("net.dv8tion:JDA:6.4.1")
    implementation("org.jooq:jooq:3.19.8")
    implementation("com.zaxxer:HikariCP:6.3.3")
    implementation("org.yaml:snakeyaml:2.2")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.32")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "moe.vitamin.campuslink.CampusLink"
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}

tasks {
    build {
        dependsOn(test)
    }
    jar { finalizedBy(shadowJar) }
    test { useJUnitPlatform() }
    compileJava { options.encoding = "UTF-8" }
    javadoc { options.encoding = "UTF-8" }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.test {
    jvmArgs("-Dfile.encoding=UTF-8")
}

tasks.withType<ShadowJar> {
    archiveFileName = "${project.name}.jar"

    val names = project.gradle.startParameter.taskNames;
    val name = if (names.size == 0) "build" else names[0]

    val exc = "The build path doesn't exist. Build it on the default path."
    val localProperties = project.rootProject.file("local.properties")
    try {
        val properties = Properties()
        val stream = localProperties.inputStream()
        properties.load(stream)

        val buildDir = properties.getProperty("${name}Dir")
        if (buildDir != null && buildDir.isNotBlank()) {
            destinationDirectory = file(buildDir)
        } else println(exc)
        stream.close()
    } catch (ignored: Exception) {
        localProperties.writeText("buildDir=\ndeployDir=\n")
        println(exc)
    }
}
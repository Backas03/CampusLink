plugins {
    id("java")
    id("io.freefair.lombok") version "8.11"
}

group = "moe.vitamin.campuslink"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")

    implementation("net.dv8tion:JDA:6.4.1")
    implementation("org.jooq:jooq:3.19.8")
    implementation("com.zaxxer:HikariCP:6.3.3")
    implementation("org.yaml:snakeyaml:2.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
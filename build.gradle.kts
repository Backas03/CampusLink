plugins {
    id("java")
}

group = "moe.vitamin.campuslink"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    implementation("net.dv8tion:JDA:6.4.1")
    implementation("org.jooq:jooq:3.19.8")
    implementation("com.zaxxer:HikariCP:6.3.3")
}

tasks.test {
    useJUnitPlatform()
}
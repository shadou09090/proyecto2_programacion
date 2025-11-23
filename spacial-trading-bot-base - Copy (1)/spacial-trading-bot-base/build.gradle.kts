plugins {
    id("java")
    id("application")
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
    id("pmd")
}

group = "tech.hellsoft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // GSON for JSON processing
    implementation("com.google.code.gson:gson:2.10.1")

    // SLF4J Simple Logger (fixes SDK logging warnings)
    implementation("org.slf4j:slf4j-simple:2.0.16")

    // Yavi for validation
    implementation("am.ik.yavi:yavi:0.13.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("tech.hellsoft.trading.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        eclipse().configFile("${project.rootDir}/config/eclipse-format.xml")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("${project.rootDir}/config/checkstyle/checkstyle.xml")
}

pmd {
    toolVersion = "6.55.0"
    ruleSetFiles = files("${project.rootDir}/config/pmd/ruleset.xml")
}
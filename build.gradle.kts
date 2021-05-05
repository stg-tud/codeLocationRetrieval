import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    id("com.squareup.sqldelight")
    application
}

repositories {
    mavenCentral()
    google()
}
group = "com.bekiroe.featurelocation"
version = "1.0-SNAPSHOT"


application {
    mainClass.set("main.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.squareup.sqldelight:sqlite-driver:1.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.assertj:assertj-core:3.11.1")

    implementation("com.google.code.gson:gson:2.8.5")
    // Commons Math
    implementation("org.apache.commons:commons-math3:3.6.1")

    // JCommander (to parse command line arguments)
    implementation("com.beust:jcommander:1.78")

    // MockK
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

/* sqldelight {
    database("Corpus") {
        packageName = "corpus"
        sourceFolders = listOf("db")
        schemaOutputDirectory = file("build/dbs")
    }
}*/

val run by tasks.getting(JavaExec::class) {
    // By default, Gradle's run task uses an empty stream for standard input -> we have to manually set it to System.in
    // see https://discuss.gradle.org/t/why-doesnt-system-in-read-block-when-im-using-gradle/3308
    // and https://stackoverflow.com/questions/45747112/kotlin-and-gradle-reading-from-stdio
    standardInput = System.`in`

    // Pass command line arguments via -Pmyargs
    if(project.hasProperty("myargs")) {
        args(project.findProperty("myargs"))
    }
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
    application
}

group = "com.bekiroe.featurelocation"
version = "1.0-SNAPSHOT"


application {
    mainClassName = "main.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.assertj:assertj-core:3.11.1")

    // Commons Math
    compile("org.apache.commons:commons-math3:3.6.1")

    // MockK
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// By default, Gradle's run task uses an empty stream for standard input -> manually set to System.in
// see https://discuss.gradle.org/t/why-doesnt-system-in-read-block-when-im-using-gradle/3308
// and https://stackoverflow.com/questions/45747112/kotlin-and-gradle-reading-from-stdio
val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}
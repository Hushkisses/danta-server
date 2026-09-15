plugins {
    java
}

val bundled by configurations.creating

dependencies {
    implementation(project(":core"))
    compileOnly("io.papermc.paper:paper-api:26.2.build.123-stable")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // DEV-016: bundled because Paper does not provide the PostgreSQL JDBC driver.
    bundled("org.postgresql:postgresql:42.7.13")
}

// The production server uses one main plugin JAR. Bundle the Paper-independent
// core classes and explicit runtime libraries into the Paper plugin JAR.
tasks.jar {
    dependsOn(project(":core").tasks.named("classes"))
    from(project(":core").extensions.getByType<SourceSetContainer>()["main"].output)
    from({
        bundled.map { dependency ->
            if (dependency.isDirectory) dependency else zipTree(dependency)
        }
    })

    archiveBaseName.set("danta-server")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

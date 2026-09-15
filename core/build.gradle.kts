plugins {
    `java-library`
}

dependencies {
    api("org.yaml:snakeyaml:2.5")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

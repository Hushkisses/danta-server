plugins {
    application
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass.set("kr.danta.simulator.CombatSimulatorApp")
}

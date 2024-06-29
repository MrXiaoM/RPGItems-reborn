pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "RPGItems-reborn"
listOf(
    ":nms",
    ":nms:shared",
    ":nms:1_17_R1",
    ":nms:1_18_R1",
    ":nms:1_18_R2",
    ":nms:1_19_R3",
    ":nms:1_20_R1",
    ":nms:1_20_R2",
    ":nms:1_20_R3",
).forEach(::include)

if (JavaVersion.current().majorVersion.toDouble() >= 21) {
    include(":nms:1_20_R4")
    include(":nms:1_21_R1")
}

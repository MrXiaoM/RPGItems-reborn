pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "RPGItems-reborn"
listOf(
    ":paper",
    ":nms",
    ":nms:shared",
    ":nms:1_8_R1",
    ":nms:1_8_R2",
    ":nms:1_8_R3",
    ":nms:1_9_R1",
    ":nms:1_9_R2",
    ":nms:1_10_R1",
    ":nms:1_11_R1",
    ":nms:1_12_R1",
    ":nms:1_13_R1",
    ":nms:1_13_R2",
    ":nms:1_14_R1",
    ":nms:1_15_R1",
    ":nms:1_16_R1",
    ":nms:1_16_R2",
    ":nms:1_16_R3",
    ":nms:1_17_R1",
    ":nms:1_18_R1",
    ":nms:1_18_R2",
    ":nms:1_19_R1",
    ":nms:1_19_R2",
    ":nms:1_19_R3",
    ":nms:1_20_R1",
    ":nms:1_20_R2",
    ":nms:1_20_R3",
).forEach(::include)

if (JavaVersion.current().majorVersion.toDouble() >= 21) {
    include(":nms:1_20_R4")
    include(":nms:1_21_R1")
}

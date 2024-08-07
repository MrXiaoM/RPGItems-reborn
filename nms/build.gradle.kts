subprojects {
    apply(plugin = "java")
    repositories {
        maven("https://libraries.minecraft.net/")
    }
    version = "1.0-SNAPSHOT"
    dependencies {
        if (name != "shared") {
            add("compileOnly", project(":nms:shared")) { isTransitive = false }
            add("compileOnly", "com.mojang:brigadier:1.0.18")
        }
    }
}

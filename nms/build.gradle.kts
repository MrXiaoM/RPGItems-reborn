subprojects {
    apply(plugin = "java")
    repositories {
        maven("https://libraries.minecraft.net/")
    }
    dependencies {
        if (name != "shared") {
            add("compileOnly", project(":nms:shared")) { isTransitive = false }
        }
    }
}

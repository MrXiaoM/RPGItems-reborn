import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    `maven-publish`
    signing
    id ("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id ("com.github.johnrengelman.shadow") version "8.1.1"
    id ("org.ajoberstar.grgit") version "3.0.0"
}

var majorVersion = "3"
var minorVersion = "12"
val repo: Grgit = Grgit.open(mapOf("currentDir" to project.projectDir))

val commit: Commit = repo.head()
val commitHash: String = commit.abbreviatedId

val branch: String = System.getenv("BRANCH_NAME") ?: repo.branch.current().name

val releasing = commit.shortMessage.contains("[release]")

val buildNumber = findProperty("BUILD_NUMBER") ?: (if (releasing) "0" else commitHash.substring(0,6))
val jdDirectory: String? = System.getenv("JAVADOCS_DIR")

version = "$majorVersion.$minorVersion.$buildNumber"
val mavenVersion: String = if (releasing) version.toString() else "$majorVersion.$minorVersion-SNAPSHOT"

println("version: $version")
println("isReleasing: $releasing")
println("mavenVersion: $mavenVersion")

configurations.configureEach {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

allprojects {
    group = "top.mrxiaom"
    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://mvn.lumine.io/repository/maven/")
        maven("https://jitpack.io")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly("io.netty:netty-all:4.1.25.Final") // netty is shadowed inside spigot jar

    compileOnly("org.ow2.asm:asm:9.3")
    compileOnly("net.bytebuddy:byte-buddy:1.12.16")
    compileOnly("com.udojava:EvalEx:2.7")
    compileOnly("commons-lang:commons-lang:2.6")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")

    compileOnly("io.lumine.xikage:MythicMobs:4.12.0")
    compileOnly("io.lumine:Mythic:5.4.1")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.sk89q.worldguard:worldguard-core:7.0.4-SNAPSHOT")
    implementation("com.sk89q.worldguard:worldguard-bukkit:7.0.4-SNAPSHOT") {
        exclude("io.papermc", "paperlib")
        exclude("org.bstats", "bstats-bukkit")
        exclude("org.bukkit", "bukkit")
        exclude("org.spigotmc", "spigot-api")
    }

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { isTransitive = false }

    implementation("de.tr7zw:item-nbt-api:2.13.1")
    shadow("de.tr7zw:item-nbt-api:2.13.1")

    for (proj in rootProject.project(":nms").subprojects) {
        implementation(proj)
        shadow(proj)
    }
}

tasks {
    withType<ProcessResources>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to project.version))
            include("plugin.yml")
        }
        from(sourceSets.main.get().resources.srcDirs) {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "GIT_BRANCH" to branch,
                    "GIT_HASH" to commitHash
                ),
                "beginToken" to "{",
                "endToken" to "}"
            )
            include("lang/")
        }
    }

    jar {
        archiveClassifier.set("core")
    }
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        val p = "think.rpgitems.utils"
        relocate("de.tr7zw.changeme.nbtapi", "$p.nbtapi")
    }
    build {
        dependsOn(shadowJar)
    }
    register<Jar>("apiJar") {
        includeEmptyDirs = false
        from(sourceSets.main.get().output.classesDirs)
        include("**/think/rpgitems/RPGItems.class")
        include("**/think/rpgitems/Events.class")
        include("**/think/rpgitems/api/")
        include("**/think/rpgitems/item/")
        include("**/think/rpgitems/power/")
        include("**/think/rpgitems/event/")
        include("**/think/rpgitems/utils/")
        exclude("**/think/rpgitems/power/impl/")
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().java.srcDirs)
    }

    register<Jar>("javadocJar") {
        dependsOn(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc.get().destinationDir)
    }

    register("checkRelease") {
        doLast {
            if (!releasing) {
                throw GradleException ("This is a canary version, use with caution!")
            }
        }
    }

    // write javadocs to an external folder which can be served via nginx
    if (jdDirectory != null) {
        javadoc.get().setDestinationDir(file("${jdDirectory}/rpgitems-${mavenVersion}"))
    }

    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            links("https://docs.oracle.com/javase/17/docs/api/")
            links("https://hub.spigotmc.org/javadocs/spigot/")
            links("https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/")
            links("https://netty.io/4.1/api/")

            locale("en_US")
            encoding("UTF-8")
            docEncoding("UTF-8")
            addBooleanOption("keywords", true)
            addBooleanOption("Xdoclint:none", true)

            val currentJavaVersion = JavaVersion.current()
            if (currentJavaVersion > JavaVersion.VERSION_1_9) {
                addBooleanOption("html5", true)
            }

            windowTitle = "RPGItems Javadoc"
            docTitle = "<b>RPGItems</b> $version"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenRelease") {
            from(components.getByName("java"))
            groupId = "top.mrxiaom"
            artifactId = "rpgitems"
            version = mavenVersion

            artifact(tasks.getByName("apiJar"))
            artifact(tasks.shadowJar)
            artifact(tasks.getByName("sourcesJar"))
            artifact(tasks.getByName("javadocJar"))

            pom {
                name = "RPGItems-reborn"
                description = "The legacy RPGItems continues. (fork)"
                url = "https://github.com/MrXiaoM/RPGItems-reborn"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://mit-license.org/"
                    }
                }
                developers {
                    developer {
                        name = "MrXiaoM"
                        email = "mrxiaom@qq.com"
                    }
                }
                scm {
                    url = "https://github.com/MrXiaoM/RPGItems-reborn"
                    connection = "scm:git:https://github.com/MrXiaoM/RPGItems-reborn.git"
                    developerConnection = "scm:git:https://github.com/MrXiaoM/RPGItems-reborn.git"
                }
            }
        }
    }
}
signing {
    val signingKey = findProperty("signingKey")?.toString()
    val signingPassword = findProperty("signingPassword")?.toString()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications.getByName("mavenRelease"))
    }
}
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(findProperty("MAVEN_USERNAME")?.toString())
            password.set(findProperty("MAVEN_PASSWORD")?.toString())
        }
    }
}

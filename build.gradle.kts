plugins {
    id("net.fabricmc.fabric-loom")
}

version = rootProject.property("mod_version")!!
group = rootProject.property("maven_group")!!

base {
    archivesName = providers.gradleProperty("archives_base_name")
}

repositories {
    maven("https://maven.terraformersmc.com/")
}

val minecraft = providers.gradleProperty("minecraft_version").get()
val loader = providers.gradleProperty("loader_version").get()
val modMenuVer = providers.gradleProperty("modmenu_version").get()

dependencies {
    minecraft("com.mojang:minecraft:${minecraft}")
    implementation("net.fabricmc:fabric-loader:${loader}")
    compileOnly("com.terraformersmc:modmenu:${modMenuVer}")
}

loom {
    mods {
        register("xdbar") {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks {
    processResources {
        inputs.property("version", version)
        inputs.property("minecraft_version", minecraft)
        inputs.property("loader_version", loader)

        filesMatching("fabric.mod.json") {
            expand(
                "version" to version,
                "minecraft_version" to minecraft,
                "loader_version" to loader,
                "modmenu_version" to modMenuVer
            )
        }
    }

    jar {
        inputs.property("archivesName", base.archivesName)

        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25

    withSourcesJar()
}
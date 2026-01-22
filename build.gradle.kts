// Based on intellij-platform-plugin-template (https://github.com/JetBrains/intellij-platform-plugin-template/blob/de6b4c8a9db474d10d00c7cd26fc2f280a057ed3/build.gradle.kts)
import org.jetbrains.changelog.Changelog

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
}

val htmlFixer = { fileName: String -> File(fileName).readText().replace("<html>", "").replace("</html>", "") }

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))

        plugins(providers.gradleProperty("platformPlugins").map { it.split(",") })

        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        description = htmlFixer("src/main/resources/META-INF/description.html")

        val changelog = project.changelog

        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }
}

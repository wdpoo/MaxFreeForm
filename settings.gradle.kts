pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven(file("native-hook-kit/third_party/maven")) {
            content {
                includeGroup("org.lsposed.lsplt")
            }
        }
        mavenCentral()
        maven("https://jitpack.io")
    }
}

include(":app")
rootProject.name = "MaxFreeForm"

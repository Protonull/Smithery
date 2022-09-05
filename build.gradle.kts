val pluginName: String by project

subprojects {
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")

	project.setProperty("archivesBaseName", "$pluginName-$name-$version")

	configure<JavaPluginExtension> {
		withJavadocJar()
		withSourcesJar()

		toolchain {
			languageVersion.set(JavaLanguageVersion.of(17))
		}
	}

	repositories {
		mavenCentral()
		maven("https://libraries.minecraft.net")
		maven("https://repo.civmc.net/repository/maven-public")
	}

	tasks {
		withType<Test> {
			testLogging {
				useJUnitPlatform()
				events(
					org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
					org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
					org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
					org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
					org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
				exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
				showCauses = true
				showExceptions = true
				showStackTraces = true
			}
		}
	}
}

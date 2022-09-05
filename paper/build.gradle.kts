val pluginName: String by project

plugins {
	`java-library`
	// userdev -> https://github.com/PaperMC/paperweight/tags
	id("io.papermc.paperweight.userdev") version "1.3.8"
	// Shadow -> https://github.com/johnrengelman/shadow/releases
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")

	compileOnly("org.projectlombok:lombok:1.18.24")
	annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks {
	processResources {
		filesMatching("plugin.yml") {
			expand(mapOf(
				"name" to pluginName,
				"version" to version,
			))
		}
	}
}
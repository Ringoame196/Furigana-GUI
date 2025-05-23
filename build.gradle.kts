import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	kotlin("jvm")
	id("org.jetbrains.compose")
	id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.github.ringoame196"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	google()
}

dependencies {
	// Note, if you develop a library, you should use compose.desktop.common.
	// compose.desktop.currentOs should be used in launcher-sourceSet
	// (in a separate module for demo project and in testMain).
	// With compose.desktop.common you will also lose @Preview functionality
	implementation(compose.desktop.currentOs)
	implementation("com.squareup.okhttp3:okhttp:4.9.3") // okhttp
	implementation("org.json:json:20210307") // JSON処理ライブラリ
}

compose.desktop {
	application {
		mainClass = "MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
			packageName = "Furigana-GUI"
			packageVersion = "1.0.0"
		}
	}
}

tasks.register<Jar>("fatJar") {
	group = "build"
	archiveClassifier.set("fat")

	duplicatesStrategy = DuplicatesStrategy.EXCLUDE

	manifest {
		attributes["Main-Class"] = "MainKt"
	}

	from(sourceSets.main.get().output)

	dependsOn(configurations.runtimeClasspath)
	from({
		configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
	})
}
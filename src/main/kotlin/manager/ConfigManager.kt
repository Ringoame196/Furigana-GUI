package manager

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

class ConfigManager {
	val configFile = File("./data.properties")

	fun loadConfig(): Pair<String, String> {
		if (!configFile.exists()) return "" to ""
		val props = Properties().apply { load(FileInputStream(configFile)) }
		val token = props.getProperty("token", "")
		val grade = props.getProperty("gradeParameter", "")
		return token to grade
	}

	fun saveConfig(token: String, gradeParameter: String) {
		val props = Properties().apply {
			setProperty("token", token)
			setProperty("gradeParameter", gradeParameter)
		}
		FileOutputStream(configFile).use { props.store(it, "Furigana Tool Config") }
	}
}
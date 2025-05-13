import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

val configFile = File("./data.properties")

@Composable
@Preview
fun App(
	tokenText: String,
	onTokenChange: (String) -> Unit,
	gradeParameter: Int?,
	onGradeChange: (Int) -> Unit,
	inputText: String,
	onInputChange: (String) -> Unit,
	resultText: String,
	onConvert: () -> Unit
) {
	var gradeDropdownExpanded by remember { mutableStateOf(false) }

	val gradeOptionsMap = mapOf<Int, String>(
		1 to "小学1年生向け",
		2 to "小学2年生向け",
		3 to "小学3年生向け",
		4 to "小学4年生向け",
		5 to "小学5年生向け",
		6 to "小学6年生向け",
		7 to "中学生以上向け",
		8 to "一般向け"
	)
	MaterialTheme {
		Box(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier
					.align(Alignment.Center)
					.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				// 入力欄
				TextField(
					value = inputText,
					onValueChange = onInputChange,
					label = { Text("漢字を入力") },
					modifier = Modifier.width(250.dp)
				)

				// 結果表示欄
				TextField(
					value = resultText,
					onValueChange = {},
					label = { Text("ふりがな付き") },
					modifier = Modifier.width(250.dp),
					readOnly = true
				)

				// 実行ボタン
				Button(
					onClick = onConvert,
					modifier = Modifier.width(250.dp)
				) {
					Text("ふりがなを付ける")
				}

				// ▼ 学年ドロップダウン
				Box {
					OutlinedTextField(
						value = gradeParameter?.toString() ?: "",
						onValueChange = {},
						label = { Text("学年") },
						modifier = Modifier
							.width(250.dp)
							.clickable { gradeDropdownExpanded = true },
						enabled = false // ユーザー直接入力不可
					)
					DropdownMenu(
						expanded = gradeDropdownExpanded,
						onDismissRequest = { gradeDropdownExpanded = false }
					) {
						(1..gradeOptionsMap.size).forEach { grade ->
							DropdownMenuItem(onClick = {
								onGradeChange(grade)
								gradeDropdownExpanded = false
							}) {
								Text(gradeOptionsMap[grade] ?: "")
							}
						}
					}
				}

				// Token入力欄
				TextField(
					value = tokenText,
					onValueChange = onTokenChange,
					label = { Text("Token入力") },
					modifier = Modifier.width(250.dp),
					visualTransformation = PasswordVisualTransformation()
				)
			}
		}
	}
}


fun main() = application {
	val windowState = rememberWindowState(width = 300.dp, height = 450.dp)

	// 記憶用の状態変数（remember は Composable 外なので使えない）
	var inputText by remember { mutableStateOf("") }
	var resultText by remember { mutableStateOf("") }
	var tokenText by remember { mutableStateOf("") }
	var gradeParameter by remember { mutableStateOf<Int?>(null) }

	// 起動時に読み込み
	LaunchedEffect(Unit) {
		val (token, grade) = loadConfig()
		tokenText = token
		gradeParameter = grade.toIntOrNull()
	}

	Window(
		onCloseRequest = {
			saveConfig(tokenText, gradeParameter?.toString() ?: "")
			exitApplication()
		},
		state = windowState,
		title = "ふりがな",
		resizable = false,
		alwaysOnTop = true
	) {
		App(
			tokenText = tokenText,
			onTokenChange = { tokenText = it },
			gradeParameter = gradeParameter,
			onGradeChange = { gradeParameter = it },
			inputText = inputText,
			onInputChange = { inputText = it },
			resultText = resultText,
			onConvert = {
				resultText = convertToFurigana(inputText, tokenText, gradeParameter)
			}
		)
	}
}

fun convertToFurigana(text: String,token: String,gradeParameter: Int?): String {
	if (text == "") return ""
	if (token == "") return "Tokenを記入してください"
	if (gradeParameter == null) return "学年を記入してください"

	val furiganaManager = FuriganaManager(token,gradeParameter)

	val response = furiganaManager.post(text)
	return furiganaManager.formatFuriganaResponse(response ?: return "エラーが発生しました")
}

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

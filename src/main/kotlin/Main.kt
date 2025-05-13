import androidx.compose.desktop.ui.tooling.preview.Preview
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
import java.util.Properties

val configFile = File("./data.properties")

@Composable
@Preview
fun App() {
	var inputText by remember { mutableStateOf("") }
	var resultText by remember { mutableStateOf("") }
	var gradeParameterText by remember { mutableStateOf("") }
	var tokenText by remember { mutableStateOf("") }

	// 起動時に読み込む
	LaunchedEffect(Unit) {
		val (token, gradeParameter) = loadConfig()
		tokenText = token
		gradeParameterText = gradeParameter
	}

	// 値変更時に保存（リアルタイム）
	LaunchedEffect(tokenText, gradeParameterText) {
		saveConfig(tokenText, gradeParameterText)
	}

	MaterialTheme {
		Box(modifier = Modifier.fillMaxSize()) {
			// メインのColumn部分（中央に配置）
			Column(
				modifier = Modifier
					.align(Alignment.Center)
					.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(16.dp) // 項目間のスペース
			) {

				// 1行目：漢字入力用テキストフィールド
				TextField(
					value = inputText,
					onValueChange = { inputText = it },
					label = { Text("漢字を入力 ") },
					modifier = Modifier.width(250.dp) // 横幅を少し調整
				)

				// 結果表示欄
				TextField(
					value = resultText,
					onValueChange = {},
					label = { Text("ふりがな付き") },
					modifier = Modifier.width(250.dp), // 横幅を統一
					readOnly = true
				)

				// ふりがなを付けるボタン
				Button(onClick = {
					resultText = convertToFurigana(inputText,tokenText,gradeParameterText.toIntOrNull())
				},
					modifier = Modifier.width(250.dp)
				) {
					Text("ふりがなを付ける")
				}

				TextField(
					value = gradeParameterText,
					onValueChange = { gradeParameterText = it },
					label = { Text("学年 ") },
					modifier = Modifier.width(250.dp), // 横幅を少し調整
				)

				TextField(
					value = tokenText,
					onValueChange = { tokenText = it },
					label = { Text("Token入力 ") },
					modifier = Modifier.width(250.dp), // 横幅を少し調整
					visualTransformation = PasswordVisualTransformation() // ここでパスワード用の変換を指定
				)
			}
		}
	}
}

// 仮のふりがな変換関数
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

	val props = Properties().apply {
		load(FileInputStream(configFile))
	}

	val token = props.getProperty("token", "")
	val another = props.getProperty("gradeParameter", "")
	return token to another
}

fun saveConfig(token: String, gradeParameter: String) {
	val props = Properties().apply {
		setProperty("token", token)
		setProperty("gradeParameter", gradeParameter)
	}
	FileOutputStream(configFile).use { props.store(it, "Furigana Tool Config") }
}


fun main() = application {
	val windowState = rememberWindowState(
		width = 300.dp,
		height = 420.dp,
	)

	Window(
		onCloseRequest = ::exitApplication,
		state = windowState,  // WindowState を指定
		title = "ふりがな",  // タイトルも指定
		resizable = false, // サイズ変更不可
		alwaysOnTop = true // ウィンドウを常に最前面に表示
	) {
		App()
	}
}

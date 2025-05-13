import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class FuriganaManager(private val appId: String, private val grade: Int) {
	private val url = "https://jlp.yahooapis.jp/FuriganaService/V2/furigana"
	private val client = OkHttpClient()

	fun post(query: String): String? {
		val headers = Headers.Builder()
			.add("Content-Type", "application/json")
			.add("User-Agent", "Yahoo AppID: $appId")
			.build()

		val jsonParams = JSONObject().apply {
			put("id", "1234-1")
			put("jsonrpc", "2.0")
			put("method", "jlp.furiganaservice.furigana")
			put("params", JSONObject().apply {
				put("q", query)
				put("grade", grade)
			})
		}

		val body = jsonParams.toString().toRequestBody("application/json".toMediaType())

		val request = Request.Builder()
			.url(url)
			.post(body)
			.headers(headers)
			.build()

		client.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				println("Error: ${response.code} - ${response.body?.string()}")
				return null
			}
			return response.body?.string()
		}
	}

	fun formatFuriganaResponse(jsonResponse: String): String {
		val response = JSONObject(jsonResponse)
		val result = response.optJSONObject("result") ?: return "結果の解析に失敗しました"
		val words = result.optJSONArray("word") ?: return "単語データが存在しません"

		val formattedText = StringBuilder()
		for (i in 0 until words.length()) {
			val wordObj = words.getJSONObject(i)
			val surface = wordObj.optString("surface", "")
			val furiganaText = wordObj.optString("furigana", "")

			formattedText.append(
				if (furiganaText.isNotEmpty()) "$surface($furiganaText)" else surface
			)
		}

		return formattedText.toString()
	}
}

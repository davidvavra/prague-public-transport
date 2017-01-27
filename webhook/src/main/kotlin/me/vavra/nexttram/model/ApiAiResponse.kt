package me.vavra.nexttram.model

/**
 * Response to API.AI
 */
data class ApiAiResponse(val speech: String) {
    val data = Data()
    class Data {
        val google = Google()
        class Google {
            var expect_user_response = true
            var is_ssml = false
        }
    }
}
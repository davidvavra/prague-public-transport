package me.vavra.nexttram.model

/**
 * Query from API.AI.
 */
class ApiAiQuery {
    lateinit var result: Result
    class Result {
        lateinit var parameters: Parameters
        class Parameters {
            val number: String? = null
            val time: String? = null
        }
    }

    fun getTramNumber(): String? {
        return result.parameters.number
    }

    fun getTimeFrom(): String? {
        return result.parameters.time
    }
}
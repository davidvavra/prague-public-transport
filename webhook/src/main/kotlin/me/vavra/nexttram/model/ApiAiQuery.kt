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

            override fun toString(): String {
                return "Parameters(number=$number, time=$time)"
            }
        }

        override fun toString(): String {
            return "Result(parameters=$parameters)"
        }
    }

    fun getTramNumber(): String? {
        return result.parameters.number
    }

    fun getTimeFrom(): String? {
        return result.parameters.time
    }

    override fun toString(): String {
        return "ApiAiQuery(result=$result)"
    }
}
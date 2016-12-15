package me.vavra.nexttram.model

/**
 * Query from API.AI.
 */
class ApiAiQuery {
    lateinit var result: Result
    class Result {
        lateinit var parameters: Parameters
        lateinit var action: String

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
    lateinit var originalRequest: OriginalRequest
    class OriginalRequest {
        lateinit var data: Data
        class Data {
            lateinit var user: User
            class User {
                lateinit var user_id: String
                var current_location: CurrentLocation? = null
                class CurrentLocation {
                    lateinit var latlng: LatLng
                    class LatLng {
                        var latitude: Double = 0.0
                        var longitude: Double = 0.0
                    }
                }
            }
        }
    }

    fun getTramNumber(): String? {
        return result.parameters.number
    }

    fun getTimeFrom(): String? {
        return result.parameters.time
    }

    fun getAction(): String {
        return result.action
    }

    fun getUserId(): String {
        return originalRequest.data.user.user_id
    }

    fun getLocation(): String? {
        val latlng = originalRequest.data.user.current_location?.latlng
        if (latlng == null) {
            return null
        } else {
            return "loc: ${latlng.latitude.convertToCzech()}; ${latlng.longitude.convertToCzech()}"
        }
    }

    private fun Double.convertToCzech(): String {
        return this.toString().replace(".", ",")
    }

    override fun toString(): String {
        return "ApiAiQuery(result=$result)"
    }
}
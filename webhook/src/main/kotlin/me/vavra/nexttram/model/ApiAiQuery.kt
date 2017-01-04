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
            }
            var device: Device? = null
            class Device {
                var location: Location? = null
                class Location {
                    lateinit var coordinates: Coordinates
                    class Coordinates {
                        var latitude: Double = 0.0
                        var longitude: Double = 0.0

                        override fun toString(): String {
                            return "Coordinates(latitude=$latitude, longitude=$longitude)"
                        }
                    }
                }
            }

            override fun toString(): String {
                return "Data(user=$user)"
            }
        }

        override fun toString(): String {
            return "OriginalRequest(data=$data)"
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
        val coordinates = originalRequest.data.device?.location?.coordinates
        if (coordinates == null) {
            return null
        } else {
            return "loc: ${coordinates.latitude.convertToCzech()}; ${coordinates.longitude.convertToCzech()}"
        }
    }

    private fun Double.convertToCzech(): String {
        return this.toString().replace(".", ",")
    }

    override fun toString(): String {
        return "ApiAiQuery(result=$result, originalRequest=$originalRequest)"
    }
}
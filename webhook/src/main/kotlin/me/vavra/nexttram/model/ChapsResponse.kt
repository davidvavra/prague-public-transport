package me.vavra.nexttram.model

/**
 * Response from CHAPS API.
 */
class ChapsResponse {
    var trains: List<Train>? = null

    class Train {
        lateinit var train: Tram
        lateinit var dateTime1: String
        lateinit var stationTrainEnd: Station

        class Tram {
            lateinit var num1: String

            override fun toString(): String {
                return "Tram(num1='$num1')"
            }
        }
        class Station {
            lateinit var name: String

            override fun toString(): String {
                return "Station(name='$name')"
            }
        }

        override fun toString(): String {
            return "Train(train=$train, dateTime1='$dateTime1', stationTrainEnd=$stationTrainEnd)"
        }
    }

    override fun toString(): String {
        return "ChapsResponse(trains=$trains)"
    }
}
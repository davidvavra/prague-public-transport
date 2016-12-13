package me.vavra.nexttram.model

/**
 * Response from CHAPS API.
 */
class ChapsResponse {
    lateinit var trains: List<Train>

    class Train {
        lateinit var train: Tram
        lateinit var dateTime1: String
        lateinit var stationTrainEnd: Station
        class Tram {
            lateinit var num1: String
        }
        class Station {
            lateinit var name: String
        }
    }
}
package me.vavra.nexttram

import com.google.gson.Gson
import me.vavra.nexttram.model.ApiAiQuery
import me.vavra.nexttram.model.ApiAiResponse
import me.vavra.nexttram.model.ChapsResponse
import me.vavra.nexttram.model.LocationPermissionResponse
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Main entry point for the app from API.AI
 *
 * @author David Vávra (david@vavra.me)
 */
class WebHook : HttpServlet() {

    private val timezone = DateTimeZone.forID("Europe/Prague")
    private val chapsDateTimeFormat = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm").withZone(timezone)
    private val gson = Gson()

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.writer.println("This is a webhook for NextTram app")
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        val apiAiQuery = gson.fromJson(request.inputStream.readToString(), ApiAiQuery::class.java)
        l("action=${apiAiQuery.getAction()}")
        when (apiAiQuery.getAction()) {
            "first-query" -> {
                val location = Datastore.getUserLocation(apiAiQuery.getUserId())
                if (location == null) {
                    response.permissionRequest()
                } else {
                    firstQuery(location, response)
                }
            }
            "save-location" -> {
                val location = apiAiQuery.getLocation()
                if (location == null) {
                    response.permissionRequest()
                } else {
                    Datastore.saveUserLocation(apiAiQuery.getUserId(), location)
                    firstQuery(location, response)
                }
            }
            "detailed-query" -> detailedQuery(apiAiQuery.getUserId(), response, apiAiQuery.getTramNumber(), apiAiQuery.getTimeFrom())
            else -> response.ok("")
        }
    }

    private fun firstQuery(location: String, response: HttpServletResponse) {
        val chapsResponse = queryChaps(location)
        var speech = convertToSpeech(chapsResponse)
        val followUp = listOf("Are you satisfied?", "Is that all for you?", "Is that enough?", "Did you find out what you need?")
        speech += followUp.randomElement()
        response.ok(speech)
    }

    private fun detailedQuery(location: String, response: HttpServletResponse, tramNumber: String?, timeFrom: String?) {
        val chapsResponse = queryChaps(location, tramNumber, timeFrom)
        var speech = convertToSpeech(chapsResponse)
        val followUp = listOf("Good bye!", "Have a nice day!", "See you later, aligator!", "Bye!")
        speech += followUp.randomElement()
        response.ok(speech)
    }

    private fun queryChaps(location: String, tramNumber: String? = null, timeFrom: String? = null, numberOfTrams: Int = 2): ChapsResponse {
        val OFFSET_MINUTES = 5
        val nowPlusOffset = DateTime.now(timezone).plusMinutes(OFFSET_MINUTES)
        var dateTime = timeFrom
        if (timeFrom.isNullOrEmpty()) {
            dateTime = chapsDateTimeFormat.print(nowPlusOffset).urlEncode()
        }
        val latlng = location.urlEncode()
        var url = "https://ext.crws.cz/api/ABCz/departures?from=$latlng&remMask=0&ttInfoDetails=0&typeId=3&ttDetails=4128&lang=1&maxCount=$numberOfTrams&dateTime=$dateTime"
        if (tramNumber != null) {
            url += "&line=$tramNumber"
        }
        val responseString = download(url)
        return gson.fromJson(responseString, ChapsResponse::class.java)
    }

    private fun convertToSpeech(chapsResponse: ChapsResponse, numberOfTrams: Int = 2): String {
        var speech = ""
        val trams = chapsResponse.trains.take(numberOfTrams)
        trams.forEachIndexed { i, tram ->
            val departure = DateTime.parse(tram.dateTime1, chapsDateTimeFormat)
            val now = DateTime.now(timezone)
            val minuteDifference = Minutes.minutesBetween(now, departure).minutes
            val direction = tram.stationTrainEnd.name.toEnglishPronunciation()
            speech += "Tram number ${tram.train.num1} direction '$direction' leaves in $minuteDifference minutes"
            if (i == trams.size - 2) {
                speech += " and "
            } else {
                speech += ".\n"
            }
        }
        return speech
    }

    private fun HttpServletResponse.ok(speech: String) {
        this.addHeader("Content-type", "application/json")
        IOUtils.write(gson.toJson(ApiAiResponse(speech)), this.outputStream, "UTF-8")
        IOUtils.closeQuietly(this.outputStream)
    }

    private fun HttpServletResponse.permissionRequest() {
        this.addHeader("Content-type", "application/json")
        val json = gson.toJson(LocationPermissionResponse())
        l(json)
        IOUtils.write(json, this.outputStream, "UTF-8")
        IOUtils.closeQuietly(this.outputStream)
    }

    private fun String.toEnglishPronunciation(): String {
        val map = mapOf("á" to "aa", "c" to "ts", "č" to "tch", "é" to "ai", "y" to "i", "ě" to "ye", "í" to "ee", "j" to "y", "ó" to "oo", "ř" to "rg", "š" to "sh", "ú" to "oo", "ů" to "oo", "ý" to "ee", "ž" to "zh")
        var newString = this.toLowerCase()
        map.entries.forEach { newString = newString.replace(it.key, it.value) }
        return newString
    }
}


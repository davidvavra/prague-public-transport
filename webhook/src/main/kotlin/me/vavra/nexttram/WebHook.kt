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
        response.writer.println("This is a webhook for Czech Public Transport app")
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        val json = request.inputStream.readToString()
        val apiAiQuery = gson.fromJson(json, ApiAiQuery::class.java)
        l("action=" + apiAiQuery.getAction())
        l("location="+ apiAiQuery.getLocation())
        when (apiAiQuery.getAction()) {
            "welcome" -> {
                val location = Datastore.getUserLocation(apiAiQuery.getUserId())
                if (location == null) {
                    response.permissionRequest()
                } else {
                    welcomeResponse(response)
                }
            }
            "detailed-query" -> detailedQuery(apiAiQuery.getUserId(), response, apiAiQuery.getTramNumber(), apiAiQuery.getTimeFrom())
            "permission-granted" -> {
                val location = apiAiQuery.getLocation()
                Datastore.saveUserLocation(apiAiQuery.getUserId(), location)
                welcomeResponse(response)
            }
            else -> response.ok("Unknown command" + apiAiQuery.getAction())
        }
    }

    private fun welcomeResponse(response: HttpServletResponse) {
        val followUp = listOf("Hi! When would you like to go?", "Welcome! What time would you like to leave?")
        response.ok(followUp.randomElement())
    }

    private fun detailedQuery(userId: String, response: HttpServletResponse, tramNumber: String?, timeFrom: String?) {
        val chapsResponse = queryChaps(Datastore.getUserLocation(userId)!!, tramNumber, timeFrom) // Location was set in first query to datastore
        var speech = convertToSpeech(chapsResponse)
        val followUp = listOf("Good bye!", "Have a nice day!", "See you later, alligator!", "Bye!")
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
        val coordinates = location.urlEncode()
        var url = "https://ext.crws.cz/api/ABCz/departures?from=$coordinates&remMask=0&ttInfoDetails=0&typeId=3&ttDetails=4128&lang=1&maxCount=$numberOfTrams&dateTime=$dateTime"
        if (tramNumber != null) {
            url += "&line=$tramNumber"
        }
        val responseString = download(url)
        return gson.fromJson(responseString, ChapsResponse::class.java)
    }

    private fun convertToSpeech(chapsResponse: ChapsResponse, numberOfTrams: Int = 2): String {
        if (chapsResponse.trains == null) {
            return "I could not find any trams around your location."
        }
        var speech = ""
        val trams = chapsResponse.trains!!.take(numberOfTrams) // We have done the null check.
        trams.forEachIndexed { i, tram ->
            val direction = tram.stationTrainEnd.name.toEnglishPronunciation()
            speech += "Tram number ${tram.train.num1} direction '$direction' leaves " + timeToSpeech(tram.dateTime1)
            if (i == trams.size - 2) {
                speech += " and "
            } else {
                speech += ".\n"
            }
        }
        return speech
    }

    private fun timeToSpeech(departure: String): String {
        val date = DateTime.parse(departure, chapsDateTimeFormat)
        val now = DateTime.now(timezone)
        val minuteDifference = Minutes.minutesBetween(now, date).minutes
        if (minuteDifference > 30) {
            val pattern = DateTimeFormat.forPattern("H:mm").withZone(timezone)
            return "at " + pattern.print(date)
        } else {
            return "in $minuteDifference minutes"
        }
    }

    private fun HttpServletResponse.ok(speech: String) {
        this.addHeader("Content-type", "application/json")
        val json = gson.toJson(ApiAiResponse(speech))
        l(json)
        IOUtils.write(json, this.outputStream, "UTF-8")
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


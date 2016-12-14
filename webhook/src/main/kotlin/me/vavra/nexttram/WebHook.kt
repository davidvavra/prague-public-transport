package me.vavra.nexttram

import com.google.gson.Gson
import me.vavra.nexttram.model.ApiAiQuery
import me.vavra.nexttram.model.ApiAiResponse
import me.vavra.nexttram.model.ChapsResponse
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Logger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Main entry point for the app from API.AI
 *
 * @author David Vávra (david@vavra.me)
 */
class WebHook : HttpServlet() {

    private val log = Logger.getLogger(WebHook::class.java.name)
    private val timezone = DateTimeZone.forID("Europe/Prague")
    private val chapsDateTimeFormat = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm").withZone(timezone)

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.writer.println("This is a webhook for NextTram app")
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        val gson = Gson()
        val apiAiQuery = gson.fromJson(request.inputStream.readToString(), ApiAiQuery::class.java)
        val chapsResponseString = queryChaps(apiAiQuery.getTramNumber(), apiAiQuery.getTimeFrom())
        val chapsResponse = gson.fromJson(chapsResponseString, ChapsResponse::class.java)
        val speech = convertToSpeech(chapsResponse)
        response.ok(gson, speech)
    }

    private fun queryChaps(tramNumber: String?, timeFrom: String?, numberOfTrams: Int = 2): String? {
        val OFFSET_MINUTES = 4
        val nowPlusOffset = DateTime.now(timezone).plusMinutes(OFFSET_MINUTES)
        var dateTime = timeFrom
        if (timeFrom.isNullOrEmpty()) {
            dateTime = chapsDateTimeFormat.print(nowPlusOffset).replace(" ", "%20")
        }
        return download("https://ext.crws.cz/api/ABCz/departures?from=loc%3A%2050%2C108167%3B%2014%2C485774&remMask=0&ttInfoDetails=0&typeId=3&ttDetails=4128&lang=1&maxCount=$numberOfTrams&dateTime=$dateTime")
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
        speech += "\n\n Would you like a specific tram or a different time?"
        return speech
    }

    private fun download(url: String): String? {
        val connection = URL(url).openConnection() as HttpURLConnection
        val respCode = connection.responseCode
        if (respCode == HttpURLConnection.HTTP_OK) {
            return connection.inputStream.readToString()
        }
        return null
    }

    private fun InputStream.readToString(): String {
        val response = IOUtils.toString(this, "UTF-8")
        IOUtils.closeQuietly(this)
        return response
    }

    private fun HttpServletResponse.ok(gson: Gson, speech: String) {
        this.addHeader("Content-type", "application/json")
        IOUtils.write(gson.toJson(ApiAiResponse(speech)), this.outputStream, "UTF-8")
        IOUtils.closeQuietly(this.outputStream)
    }

    private fun l(what: Any?) {
        log.warning(what.toString())
    }

    private fun String.toEnglishPronunciation(): String {
        val map = mapOf("á" to "aa", "c" to "ts", "č" to "tch", "é" to "ai", "y" to "i", "ě" to "ye", "í" to "ee", "j" to "y", "ó" to "oo", "ř" to "rg", "š" to "sh", "ú" to "oo", "ů" to "oo", "ý" to "ee", "ž" to "zh")
        var newString = this.toLowerCase()
        map.entries.forEach { newString = newString.replace(it.key, it.value) }
        return newString
    }
}

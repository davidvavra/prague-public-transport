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

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.writer.println("This is a webhook for NextTram app")
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        val gson = Gson()
        val apiAiQuery = gson.fromJson(request.inputStream.readToString(), ApiAiQuery::class.java)
        val chapsResponseString = download("https://ext.crws.cz/api/ABCz/departures?from=loc%3A%2050%2C108167%3B%2014%2C485774&remMask=0&ttInfoDetails=0&typeId=3&ttDetails=4128&lang=1&maxCount=3")
        val chapsResponse = gson.fromJson(chapsResponseString, ChapsResponse::class.java)
        val speech = convertToSpeech(chapsResponse)
        response.ok(gson, speech)
    }

    private fun convertToSpeech(chapsResponse: ChapsResponse): String {
        var speech = ""
        val timezone = DateTimeZone.forID("Europe/Prague")
        val formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm").withZone(timezone)
        chapsResponse.trains.forEach {
            val departure = DateTime.parse(it.dateTime1, formatter)
            val now = DateTime.now(timezone)
            val minuteDifference = Minutes.minutesBetween(now, departure).minutes
            speech += "Tram number ${it.train.num1} leaves in $minuteDifference minutes to '${it.stationTrainEnd.name}'.\n"
        }
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
}
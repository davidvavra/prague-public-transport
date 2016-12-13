package me.vavra.nexttram

import com.google.gson.Gson
import me.vavra.nexttram.model.ApiAiQuery
import me.vavra.nexttram.model.ApiAiResponse
import me.vavra.nexttram.model.ChapsResponse
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Main entry point for the app from API.AI
 *
 * @author David Vávra (david@vavra.me)
 */
class WebHook : HttpServlet() {

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        response.writer.println("This is a webhook for NextTram app")
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        val gson = Gson()
        val apiAiQuery = gson.fromJson(request.inputStream.readToString(), ApiAiQuery::class.java)
        // TODO: log all
        val chapsResponseString = download("https://ext.crws.cz/api/ABCz/departures?from=loc%3A%2050%2C108167%3B%2014%2C485774&remMask=0&ttInfoDetails=0&typeId=3&ttDetails=4128&lang=1")
        val chapsResponse = gson.fromJson(chapsResponseString, ChapsResponse::class.java)
        output(response, gson, "Hello world")
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

    private fun output(resp: HttpServletResponse, gson: Gson, speech: String) {
        resp.addHeader("Content-type", "application/json")
        resp.writer.print(gson.toJson(ApiAiResponse(speech)))
    }
}
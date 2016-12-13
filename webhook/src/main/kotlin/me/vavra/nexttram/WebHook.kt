package me.vavra.nexttram

import com.google.gson.Gson
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Main entry point for the app from API.AI
 *
 * @author David Vávra (david@vavra.me)
 */
class WebHook : HttpServlet() {

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.writer.println("This is a webhook for NextTram app")
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.addHeader("Content-type", "application/json")
        resp.writer.print(Gson().toJson(Response("Hello World")))
    }
}
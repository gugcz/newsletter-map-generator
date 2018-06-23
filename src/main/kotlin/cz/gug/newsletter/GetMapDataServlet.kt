package cz.gug.newsletter

import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import org.json.JSONArray
import org.json.JSONObject
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class GetMapDataServlet : HttpServlet() {

    private val requestFactory = UrlFetchTransport().createRequestFactory()

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        val year = Integer.parseInt(request.getParameter("year"))
        val month = Integer.parseInt(request.getParameter("month"))

        val events = EventReader(requestFactory).readEvents(year, month, false)
        val result = JSONObject()
        for ((key, value) in events) {
            val eventsArray = JSONArray()
            for (event in value) {
                eventsArray.put(event.toJSON())
            }
            result.put(key, eventsArray)
        }
        response.contentType = "application/json; charset=utf-8"
        response.characterEncoding = "utf-8"
        response.writer.write(result.toString())
    }

}

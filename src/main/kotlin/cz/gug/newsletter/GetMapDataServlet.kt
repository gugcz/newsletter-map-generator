package cz.gug.newsletter

import com.google.gson.Gson
import cz.gug.newsletter.reader.EventReader
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class GetMapDataServlet : HttpServlet() {

    private val gson = Gson()

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        val year = Integer.parseInt(request.getParameter("year"))
        val month = Integer.parseInt(request.getParameter("month"))

        val events = EventReader().readEventsByCities(year, month)

        response.contentType = "application/json; charset=utf-8"
        response.characterEncoding = "utf-8"
        response.writer.write(gson.toJson(events))
    }

}

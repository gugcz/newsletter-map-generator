package cz.gug.newsletter

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject
import org.json.JSONTokener

class EventReader(private val requestFactory: HttpRequestFactory) {

    fun readEvents(year: Int, month: Int, publishedOnly: Boolean): Map<String, List<Event>> {
        val dateFrom = DateTime(year, month, 1, 1, 0, 0)
        val dateTo = dateFrom.plusMonths(1)

        val url = GenericUrl(Configuration.getProperty("gug.web.endpoint"))
        url["Token"] = Configuration.getProperty("gug.web.api.key")
        url["date_from_after"] = ISODateTimeFormat.dateTime().print(dateFrom)
        url["date_from_before"] = ISODateTimeFormat.dateTime().print(dateTo)
        url["date_from_status"] = "known"
        val eventsRequest = requestFactory.buildGetRequest(url)
        val eventsInputStream = eventsRequest.execute().content

        val jsonObject = JSONObject(JSONTokener(eventsInputStream))

        val eventsArray = jsonObject.getJSONObject("_embedded").getJSONArray("event_occurrences")
        val events = eventsArray
            .map { Event(it as JSONObject) }
            .filter { !publishedOnly || it.isPublished }
        return events.groupBy { it.city }
    }

}

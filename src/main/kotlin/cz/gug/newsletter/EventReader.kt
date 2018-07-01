package cz.gug.newsletter

import com.google.api.client.http.GenericUrl
import com.google.gson.Gson
import cz.gug.newsletter.model.Event
import cz.gug.newsletter.model.RequestFactoryHolder

class EventReader {

    private val gson = Gson()

    fun readEvents(year: Int, month: Int): Map<String, List<Event>> {
        val url = GenericUrl("https://us-central1-gug-web.cloudfunctions.net/getNewsletterEvents")
        url["year"] = year
        url["month"] = month

        val eventsRequest = RequestFactoryHolder.requestFactory.buildGetRequest(url)
        val eventsInputStream = eventsRequest.execute().content

        val events: List<Event> = gson.fromJson(eventsInputStream.reader())
        return events.groupBy { it.city }
    }

}

package cz.gug.newsletter

import com.google.gson.Gson
import cz.gug.newsletter.model.Event
import java.net.URL


class EventReader {

    private val gson = Gson()

    fun readEvents(year: Int, month: Int): Map<String, List<Event>> {
        val url = URL("https://us-central1-gug-web.cloudfunctions.net/getNewsletterEvents?year=$year&month=$month")
        val eventsInputStream = url.openStream()
        val events: List<Event> = gson.fromJson(eventsInputStream.reader())
        return events.groupBy { it.city }
    }

}

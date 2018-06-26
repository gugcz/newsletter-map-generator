package cz.gug.newsletter

import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import cz.gug.newsletter.model.Event
import cz.gug.newsletter.reader.EventReader
import org.json.JSONObject
import org.json.JSONTokener
import java.text.Collator
import java.util.*
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CreateNewsletterServlet : HttpServlet() {

    private val requestFactory: HttpRequestFactory = UrlFetchTransport().createRequestFactory()

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        val year = Integer.parseInt(request.getParameter("year"))
        val month = Integer.parseInt(request.getParameter("month"))

        val eventsByCity = EventReader().readEventsByCities(year, month)
        if (eventsByCity.isEmpty()) {
            response.writer.print("<h1>No events found for selected month and year.</h1>")
            return
        }
        val requestData = createMailchimpRequestData(eventsByCity, year, month)
        val campaignUrl = createMailchimpCampaign(requestData)
        response.writer.print("<a href = \"$campaignUrl\">$campaignUrl</a>")
    }

    private fun createMailchimpCampaign(requestData: JSONObject): String {
        val url = GenericUrl(Configuration.getProperty("mailchimp.create.campaign.endpoint"))
        val content = ByteArrayContent.fromString("application/json", requestData.toString())
        val httpRequest = requestFactory.buildPostRequest(url, content)
        val inputStream = httpRequest.execute().content
        val jsonObject = JSONObject(JSONTokener(inputStream))
        return jsonObject.getString("archive_url")
    }

    private fun createMailchimpRequestData(eventsByCity: Map<String, List<Event>>, year: Int, month: Int): JSONObject {
        val requestData = JSONObject()
        requestData.put("apikey", Configuration.getProperty("mailchimp.api.key"))
        requestData.put("type", "regular")

        val options = JSONObject()
        options.put("list_id", Configuration.getProperty("mail.list.id"))
        options.put("template_id", Configuration.getProperty("template.id"))
        options.put("from_email", Configuration.getProperty("from.email"))
        options.put("from_name", Configuration.getProperty("from.name"))
        options.put("subject", createSubject(year, month))
        requestData.put("options", options)

        val mailContent = createMailContent(eventsByCity, year, month)
        mailContent.put("text", createPlainText(eventsByCity, year, month))
        requestData.put("content", mailContent)
        return requestData
    }

    private fun createSubject(year: Int, month: Int): String {
        return String.format("Naše akce v %s %d", getMonthName(month), year)
    }

    private fun getMonthName(month: Int): String {
        return Configuration.MONTH_NAMES[month - 1]
    }

    private fun createMailContent(eventsByCity: Map<String, List<Event>>, year: Int, month: Int): JSONObject {
        val sections = JSONObject()
        sections.put("header", createSubject(year, month))

        val cities = ArrayList(eventsByCity.keys)
        sortUsingLocale(cities)
        for (i in cities.indices) {
            val city = cities[i]
            sections.put("repeat_1:$i:city", city)
            val eventsInCity = eventsByCity[city]
            eventsInCity?.forEachIndexed { j, event ->
                val prefix = "repeat_1:$i:repeat_1:$j:"
                addEvent(sections, prefix, event)
            }
        }

        val mailContent = JSONObject()
        mailContent.put("sections", sections)
        return mailContent
    }

    private fun createPlainText(eventsByCity: Map<String, List<Event>>, year: Int, month: Int): String {
        val result = StringBuilder()
        result.append("Otevřít v prohlížeči (*|ARCHIVE|*)\n\n")
            .append("GUG.cz\n" + createSubject(year, month) + "\n")
            .append("------------------------------------------------------------\n\n")

        val cities = ArrayList(eventsByCity.keys)
        sortUsingLocale(cities)
        for (i in cities.indices) {
            val city = cities[i]
            result.append("** $city\n")
                .append("------------------------------------------------------------\n")

            val eventsInCity = eventsByCity[city] ?: continue
            for (j in eventsInCity.indices) {
                val event = eventsInCity[j]
                result.append(event.groupShortcut.toUpperCase() + " - ")
                    .append(event.name)
                result.append(" (" + event.link + ")\n")
                    .append(event.date)
                    .append("\n\n")
            }
            result.append("\n\n")
        }
        return result.toString()
    }

    private fun addEvent(sections: JSONObject, prefix: String, event: Event) {
        sections.put(
            prefix + "chapter_mark",
            String.format(
                "<span class=\"%s_color chapter_mark\">%s</span>",
                event.groupShortcut.toLowerCase(),
                event.groupShortcut.toUpperCase()
            )
        )
        sections.put(
            prefix + "event_name_and_place",
            String.format(
                "<a href=\"%s\">%s</a><br>\n" + "<span class=\"place\">%s</span>",
                event.link,
                event.name,
                event.date
            )
        )
    }

    private fun sortUsingLocale(list: List<*>) {
        val collator = Collator.getInstance(Configuration.locale)
        collator.strength = Collator.PRIMARY
        Collections.sort<Any>(list, collator)
    }
}

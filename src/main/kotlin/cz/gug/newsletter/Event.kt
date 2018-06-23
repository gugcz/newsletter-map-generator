package cz.gug.newsletter

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject

class Event(jsonObject: JSONObject) {

    val isPublished: Boolean
    var groupShortcut: String
    val eventName: String
    var occurrenceName: String? = null
    val fromDate: String
    var address: String? = null
    var city: String
    val link: String
    val day: Int

    init {
        isPublished = jsonObject.getBoolean("is_published")
        eventName = jsonObject.getString("event_name")
        link = jsonObject.getString("event_absolute_link")

        val rawFromDate = jsonObject.getString("date_from")
        val parsedDate = ISODateTimeFormat.dateTimeParser().parseDateTime(rawFromDate)
        fromDate = DateTimeFormat.shortDateTime()
            .withLocale(Configuration.locale)
            .withZone(Configuration.timeZone)
            .print(parsedDate)
        day = parsedDate.dayOfMonth

        val embedded = jsonObject.getJSONObject("_embedded")
        val groups = embedded.getJSONArray("groups")
        if (groups.length() > 0) {
            groupShortcut =
                    groups.getJSONObject(0).getJSONObject("embedded").getJSONObject("section").getString("shortcut")
        } else {
            throw IllegalArgumentException("No asocciated group.")
        }

        if (!embedded.isNull("venue")) {
            val venue = embedded.getJSONObject("venue")
            if (!venue.isNullOrEmpty("address")) {
                address = venue.getString("address")
            } else if (!venue.isNullOrEmpty("name")) {
                address = venue.getString("name")
            }
        }

        city = groups.getJSONObject(0).getString("location")
        if ("Prague" == city) {
            city = "Praha"
        }

        if (address == null) {
            address = city
        }

        if (jsonObject.getBoolean("show_occurence_name")) {
            occurrenceName = jsonObject.getString("occurence_name")
        }
    }

    fun toJSON(): JSONObject {
        val result = JSONObject()
        result.put("published", isPublished)
        result.put("groupShortcut", groupShortcut)
        result.put("eventName", eventName)
        result.put("occurrenceName", occurrenceName)
        result.put("city", city)
        result.put("day", day)
        result.put("fromDate", fromDate)
        return result
    }
}

package cz.gug.newsletter

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject

fun JSONObject.isNullOrEmpty(key: String): Boolean {
    return isNull(key) || getString(key).isEmpty()
}

fun DateTime.toISOString() : String {
    return ISODateTimeFormat.dateTime().print(this)
}
package cz.gug.newsletter

import org.json.JSONObject

fun JSONObject.isNullOrEmpty(key: String): Boolean {
    return isNull(key) || getString(key).isEmpty()
}
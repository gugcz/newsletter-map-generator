package cz.gug.newsletter

import org.joda.time.DateTimeZone
import java.util.*

private const val PROPERTIES_FILE = "/newsletter.properties"

object Configuration {

    private val properties: Properties = Properties().apply {
        load(Configuration::class.java.getResourceAsStream(PROPERTIES_FILE))
    }

    val locale: Locale = Locale.forLanguageTag(getProperty("locale"))

    val timeZone: DateTimeZone = DateTimeZone.forID(getProperty("timezone"))

    val MONTH_NAMES = listOf(
        "lednu",
        "únoru",
        "březnu",
        "dubnu",
        "květnu",
        "červnu",
        "červenci",
        "srpnu",
        "září",
        "říjnu",
        "listopadu",
        "prosinci"
    )

    fun getProperty(key: String): String {
        return properties.getProperty(key)
    }
}

package cz.gug.newsletter.model

data class FirebaseEvent (
    var id: String = "",
    var name: String = "",
    var dates: Dates = Dates(),
    var chapters: Map<String, Boolean> = mapOf()
)

data class Dates(
    val date: String = "",
    val time: String = ""
)
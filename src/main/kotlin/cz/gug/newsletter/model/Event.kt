package cz.gug.newsletter.model

data class Event(
    var name: String = "",
    var date: String = "",
    var time: String = "",
    var url: String = "",
    var groupShortcut: String = "",
    var city: String = ""
)
package cz.gug.newsletter.reader

import cz.gug.newsletter.model.Chapter
import cz.gug.newsletter.model.Event
import cz.gug.newsletter.model.FirebaseEvent

private const val LINK_PREFIX = "https://gug.cz/event/"

class EventReader {

    fun readEventsByCities(year: Int, month: Int) : Map<String, List<Event>> {
        val chapters = ChapterReader().readChapters()
        val firebaseEvents = FirebaseEventReader().readEvents(year, month)

        return firebaseEvents.values
            .map { convertFirebaseEvent(it, chapters) }
            .groupBy { it.city }
    }

    private fun convertFirebaseEvent(firebaseEvent: FirebaseEvent, chapters: Map<String, Chapter>): Event {
        val chapterId = firebaseEvent.chapters.keys.first()
        val chapter = chapters[chapterId] ?: throw IllegalArgumentException("Chapter '$chapterId' not found.")
        return Event(
            name = firebaseEvent.name,
            link = LINK_PREFIX + firebaseEvent.id,
            date = firebaseEvent.dates.date,
            time = firebaseEvent.dates.time,
            groupShortcut = chapter.section,
            city = chapter.location
        )
    }

}

package cz.gug.newsletter.reader

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import cz.gug.newsletter.FirebaseConfig
import cz.gug.newsletter.model.Chapter
import mu.KLogging
import java.util.concurrent.Semaphore

class ChapterReader {

    companion object: KLogging()

    fun readChapters(): Map<String, Chapter> {
        val events = FirebaseConfig.dbReference.child("chapters")

        val semaphore = Semaphore(0)

        val chapters = mutableMapOf<String, Chapter>()

        val valueListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val chapter = it.getValue(Chapter::class.java)
                    chapter.id = it.key
                    chapters[chapter.id] = chapter
                }
                semaphore.release()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                FirebaseEventReader.logger.error("Database error: ${databaseError.message}\n${databaseError.details}")
                semaphore.release()
            }
        }

        events.addListenerForSingleValueEvent(valueListener)
        semaphore.acquire()
        return chapters
    }
}

package cz.gug.newsletter.reader

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import cz.gug.newsletter.FirebaseConfig
import cz.gug.newsletter.model.FirebaseEvent
import cz.gug.newsletter.toISOString
import mu.KLogging
import org.joda.time.DateTime
import java.util.concurrent.Semaphore

class FirebaseEventReader {

    companion object: KLogging()

    fun readEvents(year: Int, month: Int): Map<String, FirebaseEvent> {
        val dateFrom = DateTime(year, month, 1, 1, 0, 0)
        val dateTo = dateFrom.plusMonths(1)

        val events = FirebaseConfig.dbReference
            .child("publishedEvents")
            .orderByChild("datesFilter/start")
            .startAt(dateFrom.toISOString())
            .endAt(dateTo.toISOString())

        val semaphore = Semaphore(0)

        val firebaseEvents = mutableMapOf<String, FirebaseEvent>()

        val valueListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val firebaseEvent = it.getValue(FirebaseEvent::class.java)
                    firebaseEvent.id = it.key
                    firebaseEvents[firebaseEvent.id] = firebaseEvent
                }
                semaphore.release()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logger.error("Database error: ${databaseError.message}\n${databaseError.details}")
                semaphore.release()
            }
        }

        events.addListenerForSingleValueEvent(valueListener)
        semaphore.acquire()
        return firebaseEvents
    }
}

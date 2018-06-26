package cz.gug.newsletter

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import cz.gug.newsletter.reader.FirebaseEventReader

object FirebaseConfig {

    val dbReference: DatabaseReference

    init {
        val serviceAccount = FirebaseEventReader::class.java.getResourceAsStream("/gug-web-firebase.json")
        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://gug-web.firebaseio.com")
            .build()

        FirebaseApp.initializeApp(options)
        dbReference = FirebaseDatabase.getInstance().reference
    }

}
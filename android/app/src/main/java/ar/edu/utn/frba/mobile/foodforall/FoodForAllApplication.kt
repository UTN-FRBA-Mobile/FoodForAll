package ar.edu.utn.frba.mobile.foodforall

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class FoodForAllApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            try {
                Firebase.firestore.useEmulator("10.0.2.2", 9000)
                Firebase.auth.useEmulator("10.0.2.2", 9099)
            } catch (e: Exception) {
            }
        }
    }
}

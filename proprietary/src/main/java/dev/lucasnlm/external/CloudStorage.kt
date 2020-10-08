package dev.lucasnlm.external

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dev.lucasnlm.external.model.CloudSave
import dev.lucasnlm.external.model.cloudSaveOf
import dev.lucasnlm.external.model.toHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException

class CloudStorage : ICloudStorage {
    private val db by lazy { Firebase.firestore }

    fun uploadSave(cloudSave: CloudSave) {
        val data = cloudSave.toHashMap()
        db.collection(SAVES)
            .document(cloudSave.playId)
            .set(data)
            .addOnFailureListener {
                Log.e(TAG, "Fail to save on cloud", it)
            }
            .addOnSuccessListener {
                Log.v(TAG, "Saved on cloud")
            }
    }

    suspend fun getSave(playId: String): CloudSave {
        try {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val result = Tasks.await(
                        db.collection(SAVES)
                            .document(playId)
                            .get()
                    )

                    result.data?.let {
                        cloudSaveOf(playId, it.toMap())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fail to load save on cloud", e)
        }
    }

    companion object {
        const val TAG = "CloudStorage"
        const val SAVES = "saves"
    }
}

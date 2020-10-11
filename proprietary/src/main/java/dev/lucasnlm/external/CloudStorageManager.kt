package dev.lucasnlm.external

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dev.lucasnlm.external.model.CloudSave
import dev.lucasnlm.external.model.cloudSaveOf
import dev.lucasnlm.external.model.toHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CloudStorageManager : ICloudStorageManager {
    private val db by lazy { Firebase.firestore }

    override fun uploadSave(cloudSave: CloudSave) {
        FirebaseFirestore.setLoggingEnabled(true)
        val data = cloudSave.toHashMap()
        Tasks.await(
            db.collection(SAVES)
                .document(cloudSave.playId)
                .set(data)
                .addOnCompleteListener {
                    Log.v(TAG, "Cloud storage complete")
                }
                .addOnCanceledListener {
                    Log.v(TAG, "Cloud storage canceled")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Cloud storage error", it)
                }
                .addOnSuccessListener {
                    Log.v(TAG, "Cloud storage success")
                }
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getSave(playId: String): CloudSave? {
        return runBlocking {
            try {
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
            } catch (e: Exception) {
                Log.e(TAG, "Fail to load save on cloud", e)
                null
            }
        }
    }

    companion object {
        val TAG = CloudStorageManager::class.simpleName
        const val SAVES = "saves"
    }
}

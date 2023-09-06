package dev.lucasnlm.external

import android.text.format.DateUtils
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dev.lucasnlm.antimine.proprietary.BuildConfig
import dev.lucasnlm.external.model.CloudSave
import dev.lucasnlm.external.model.cloudSaveOf
import dev.lucasnlm.external.model.toHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CloudStorageManagerImpl : CloudStorageManager {
    private val db by lazy { Firebase.firestore }
    private var lastSync: Long = 0L

    override fun uploadSave(cloudSave: CloudSave) {
        FirebaseFirestore.setLoggingEnabled(BuildConfig.DEBUG)
        val data = cloudSave.toHashMap()
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
    }

    override suspend fun getSave(playId: String): CloudSave? {
        return if (System.currentTimeMillis() - lastSync > DateUtils.MINUTE_IN_MILLIS) {
            lastSync = System.currentTimeMillis()

            runCatching {
                withContext(Dispatchers.IO) {
                    db.collection(SAVES).document(playId).get().await().data?.let {
                        cloudSaveOf(playId, it.toMap())
                    }
                }
            }.onFailure {
                Log.e(TAG, "Fail to load save on cloud", it)
            }.getOrNull()
        } else {
            null
        }
    }

    companion object {
        val TAG = CloudStorageManagerImpl::class.simpleName
        const val SAVES = "saves"
    }
}

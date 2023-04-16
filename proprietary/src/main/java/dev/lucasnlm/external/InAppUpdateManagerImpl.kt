package dev.lucasnlm.external

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateManagerImpl : InAppUpdateManager {
    private var keepRequesting = true

    override fun checkUpdate(activity: Activity) {
        if (keepRequesting) {
            keepRequesting = false
            val appUpdateManager = AppUpdateManagerFactory.create(activity.applicationContext)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    !activity.isFinishing
                ) {
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                    )
                }
            }
        }
    }
}

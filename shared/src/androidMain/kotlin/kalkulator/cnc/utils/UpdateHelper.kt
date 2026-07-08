package kalkulator.cnc.utils

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class UpdateHelper(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val updateType = AppUpdateType.FLEXIBLE
    private val updateRequestCode = 1001

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Log.d("UpdateHelper", "Aktualizacja pobrana. Rozpoczynam instalację.")
            appUpdateManager.completeUpdate()
        }
    }

    fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                appUpdateManager.startUpdateFlow(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }.addOnFailureListener {
            Log.e("UpdateHelper", "Błąd sprawdzania aktualizacji (może testujesz poza sklepem)", it)
        }
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (updateType == AppUpdateType.IMMEDIATE &&
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                appUpdateManager.startUpdateFlow(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }

            if (updateType == AppUpdateType.FLEXIBLE &&
                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED
            ) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    fun registerListener() {
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.registerListener(installStateUpdatedListener)
        }
    }

    fun unregisterListener() {
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
    }
}
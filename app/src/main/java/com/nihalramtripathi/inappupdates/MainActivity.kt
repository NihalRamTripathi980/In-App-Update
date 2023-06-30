package com.nihalramtripathi.inappupdates

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE
    //private val updateType = AppUpdateType.IMMEDIATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        appUpdateManager=AppUpdateManagerFactory.create(applicationContext)
        checkForAppUpdates()
        if (updateType==AppUpdateType.FLEXIBLE){
            appUpdateManager.registerListener(installStateUpdatedListener)
        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus()==InstallStatus.DOWNLOADED){
            Toast.makeText(
                applicationContext,
                "Downloaded Sucessful . Restarted in 5 seconds ",
                Toast.LENGTH_LONG
            ).show()
        lifecycleScope.launch {
            delay(5.seconds)
            appUpdateManager.completeUpdate()
        }
        }

    }

    private fun checkForAppUpdates(){
        appUpdateManager.appUpdateInfo.addOnSuccessListener {info ->
          val isUpdateAvailable =info.updateAvailability()==UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when(updateType){
                AppUpdateType.FLEXIBLE->info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE->info.isImmediateUpdateAllowed
                else ->false
            }
            if (isUpdateAllowed && isUpdateAvailable){
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    123
                )

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==123){
            if (resultCode!= RESULT_OK){
                println("Something Went Wrong i!")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (updateType==AppUpdateType.IMMEDIATE) {


            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateType,
                        this,
                        123
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (updateType==AppUpdateType.FLEXIBLE){
            appUpdateManager.unregisterListener(installStateUpdatedListener)

        }
    }
}
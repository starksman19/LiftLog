package com.liftlog.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.liftlog.app.core.ui.localization.AppLanguageState
import com.liftlog.app.core.ui.theme.LiftLogTheme
import com.liftlog.app.feature.timer.notification.RestTimerNotificationCoordinator
import com.liftlog.app.navigation.LiftLogApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var restTimerNotificationCoordinator: RestTimerNotificationCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLanguageState.synchronize()
        enableEdgeToEdge()
        setContent {
            LiftLogTheme {
                LiftLogApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        restTimerNotificationCoordinator.onMainActivityResumed()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            restTimerNotificationCoordinator.onMainActivityStopped()
        }
    }
}

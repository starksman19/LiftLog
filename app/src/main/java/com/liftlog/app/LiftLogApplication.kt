package com.liftlog.app

import android.app.Application
import com.liftlog.app.debug.DebugDemoDataSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LiftLogApplication : Application() {
    @Inject
    lateinit var debugDemoDataSeeder: DebugDemoDataSeeder

    override fun onCreate() {
        super.onCreate()
        debugDemoDataSeeder.seedOnEmulatorOnce()
    }
}

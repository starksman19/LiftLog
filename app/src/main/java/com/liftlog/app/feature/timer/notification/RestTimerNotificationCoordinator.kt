package com.liftlog.app.feature.timer.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.liftlog.app.MainActivity
import com.liftlog.app.R
import com.liftlog.app.core.datastore.SettingsRepository
import com.liftlog.app.core.ui.localization.localizedNow
import com.liftlog.app.feature.timer.presentation.RestTimerBubbleActivity
import com.liftlog.app.feature.workout.domain.WorkoutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Singleton
class RestTimerNotificationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainActivityVisible = MutableStateFlow(true)
    private val notificationManager = NotificationManagerCompat.from(context)

    val timerState: StateFlow<RestTimerNotificationState?> = combine(
        workoutRepository.observeActiveWorkout(),
        settingsRepository.settings,
    ) { workout, settings ->
        workout.restTimerNotificationState(settings)
    }.stateIn(
        scope = scope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = null,
    )

    init {
        scope.launch {
            combine(timerState, mainActivityVisible) { timer, isMainActivityVisible ->
                if (isMainActivityVisible) null else timer
            }.collect { timer ->
                if (timer == null) cancel() else show(timer)
            }
        }
    }

    fun onMainActivityResumed() {
        mainActivityVisible.value = true
    }

    fun onMainActivityStopped() {
        mainActivityVisible.value = false
    }

    fun cancel() {
        notificationManager.cancel(NotificationId)
    }

    private fun show(timer: RestTimerNotificationState) {
        if (!canPostNotifications()) {
            cancel()
            return
        }

        ensureChannel()
        val title = localizedNow("Rest timer", "Timer przerwy")
        val message = localizedNow("Rest timer is running", "Timer przerwy działa")
        val timerPerson = Person.Builder().setName(title).build()
        val notification = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.ic_rest_timer_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.MessagingStyle(timerPerson)
                    .setConversationTitle(title)
                    .addMessage(message, System.currentTimeMillis(), timerPerson),
            )
            .setWhen(timer.startedAtEpochMillis - timer.offsetSeconds * 1_000L)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(mainActivityPendingIntent())
            .apply {
                createBubbleMetadata()?.let { metadata ->
                    setShortcutId(BubbleShortcutId)
                    setBubbleMetadata(metadata)
                }
            }
            .build()

        notificationManager.notify(NotificationId, notification)
    }

    private fun createBubbleMetadata(): NotificationCompat.BubbleMetadata? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || timerState.value?.bubbleEnabled != true) return null

        publishBubbleShortcut()
        return NotificationCompat.BubbleMetadata.Builder(
            bubbleActivityPendingIntent(),
            IconCompat.createWithResource(context, R.drawable.ic_rest_timer_notification),
        )
            .setDesiredHeight(280)
            .setAutoExpandBubble(false)
            .setSuppressNotification(false)
            .build()
    }

    private fun publishBubbleShortcut() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

        val shortcut = ShortcutInfoCompat.Builder(context, BubbleShortcutId)
            .setShortLabel(localizedNow("Rest timer", "Timer przerwy"))
            .setLongLived(true)
            .setPerson(Person.Builder().setName(localizedNow("Rest timer", "Timer przerwy")).build())
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_rest_timer_notification))
            .setIntent(
                Intent(context, RestTimerBubbleActivity::class.java)
                    .setAction(BubbleAction)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT),
            )
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    private fun mainActivityPendingIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        MainActivityRequestCode,
        Intent(context, MainActivity::class.java).apply {
            action = MainActivityAction
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun bubbleActivityPendingIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        BubbleRequestCode,
        Intent(context, RestTimerBubbleActivity::class.java).apply {
            action = BubbleAction
            flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
    )

    private fun canPostNotifications(): Boolean =
        notificationManager.areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            ChannelId,
            localizedNow("Rest timer", "Timer przerwy"),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = localizedNow(
                "Shows the active workout rest timer outside LiftLog.",
                "Pokazuje timer przerwy aktywnego treningu poza LiftLog.",
            )
            setShowBadge(false)
            setAllowBubbles(true)
            setSound(null, null)
            enableVibration(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val ChannelId = "rest_timer"
        private const val NotificationId = 301
        private const val BubbleShortcutId = "rest_timer_bubble"
        private const val MainActivityAction = "com.liftlog.app.action.OPEN_REST_TIMER"
        private const val BubbleAction = "com.liftlog.app.action.OPEN_REST_TIMER_BUBBLE"
        private const val MainActivityRequestCode = 301
        private const val BubbleRequestCode = 302
    }
}

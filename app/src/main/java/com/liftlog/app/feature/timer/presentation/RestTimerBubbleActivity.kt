package com.liftlog.app.feature.timer.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liftlog.app.MainActivity
import com.liftlog.app.core.ui.localization.localizedNow
import com.liftlog.app.core.ui.theme.LiftLogTheme
import com.liftlog.app.feature.timer.notification.RestTimerNotificationCoordinator
import com.liftlog.app.feature.timer.notification.RestTimerNotificationState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay

@AndroidEntryPoint
class RestTimerBubbleActivity : androidx.activity.ComponentActivity() {
    @Inject
    lateinit var restTimerNotificationCoordinator: RestTimerNotificationCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val timer by restTimerNotificationCoordinator.timerState.collectAsStateWithLifecycle()
            LiftLogTheme {
                RestTimerBubbleScreen(
                    timer = timer,
                    onOpenLiftLog = ::openLiftLog,
                )
            }
        }
    }

    private fun openLiftLog() {
        TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(Intent(this, MainActivity::class.java))
            .startActivities()
        finish()
    }
}

@Composable
private fun RestTimerBubbleScreen(
    timer: RestTimerNotificationState?,
    onOpenLiftLog: () -> Unit,
) {
    val seconds by produceState(
        initialValue = timer?.elapsedSeconds() ?: 0L,
        timer?.latestSetId,
        timer?.startedAtEpochMillis,
        timer?.offsetSeconds,
    ) {
        while (true) {
            value = timer?.elapsedSeconds() ?: 0L
            delay(1_000L)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Outlined.Timer, contentDescription = null)
                Text(
                    text = localizedNow("Rest timer", "Timer przerwy"),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = seconds.asTimerText(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                Button(onClick = onOpenLiftLog) {
                    Text(localizedNow("Open LiftLog", "Otwórz LiftLog"))
                }
            }
        }
    }
}

private fun RestTimerNotificationState.elapsedSeconds(): Long =
    ((System.currentTimeMillis() - startedAtEpochMillis) / 1_000L + offsetSeconds).coerceAtLeast(0L)

private fun Long.asTimerText(): String = "%d:%02d".format(this / 60, this % 60)

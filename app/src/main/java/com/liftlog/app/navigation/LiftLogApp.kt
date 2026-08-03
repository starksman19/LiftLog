package com.liftlog.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.liftlog.app.feature.exercises.presentation.ExerciseListRoute
import com.liftlog.app.feature.placeholder.PlaceholderScreen
import com.liftlog.app.feature.workout.presentation.WorkoutRoute

@Composable
fun LiftLogApp() {
    val navController = rememberNavController()
    val items = listOf(
        TopLevelDestination.Exercises,
        TopLevelDestination.Workout,
        TopLevelDestination.Progress,
        TopLevelDestination.Settings,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination

                items.forEach { destination ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == destination.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Exercises.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.Exercises.route) {
                ExerciseListRoute()
            }
            composable(TopLevelDestination.Workout.route) {
                WorkoutRoute()
            }
            composable(TopLevelDestination.Progress.route) {
                PlaceholderScreen(title = "Progress")
            }
            composable(TopLevelDestination.Settings.route) {
                PlaceholderScreen(title = "Settings")
            }
        }
    }
}

private sealed class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Exercises : TopLevelDestination("exercises", "Exercises", Icons.Outlined.FitnessCenter)
    data object Workout : TopLevelDestination("workout", "Workout", Icons.Outlined.History)
    data object Progress : TopLevelDestination("progress", "Progress", Icons.Outlined.Analytics)
    data object Settings : TopLevelDestination("settings", "Settings", Icons.Outlined.Settings)
}

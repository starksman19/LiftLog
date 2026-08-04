package com.liftlog.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.LocationOn
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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.liftlog.app.feature.exercises.presentation.ExerciseListRoute
import com.liftlog.app.feature.placeholder.PlaceholderScreen
import com.liftlog.app.feature.progress.presentation.ProgressRoute
import com.liftlog.app.feature.progress.presentation.ExerciseHistoryRoute
import com.liftlog.app.feature.settings.presentation.SettingsRoute
import com.liftlog.app.feature.locations.presentation.GymLocationsRoute
import com.liftlog.app.feature.workout.presentation.WorkoutRoute
import com.liftlog.app.feature.workout.presentation.WorkoutHistoryRoute
import com.liftlog.app.feature.workout.presentation.WorkoutDetailRoute
import com.liftlog.app.feature.workout.presentation.TemplateManagementRoute
import com.liftlog.app.core.ui.localization.t

@Composable
fun LiftLogApp() {
    val navController = rememberNavController()
    val items = listOf(
        TopLevelDestination.Exercises,
        TopLevelDestination.Workout,
        TopLevelDestination.Progress,
        TopLevelDestination.Locations,
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
                                contentDescription = t(destination.label),
                            )
                        },
                        label = { Text(t(destination.label)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Progress.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.Exercises.route) {
                ExerciseListRoute(
                    onExerciseSelected = { exerciseId ->
                        navController.navigate("exercise/$exerciseId")
                    },
                )
            }
            composable(TopLevelDestination.Workout.route) {
                WorkoutRoute(
                    onHistory = { navController.navigate("workout-history") },
                    onManageTemplates = { navController.navigate("templates") },
                )
            }
            composable("workout-history") {
                WorkoutHistoryRoute(
                    onBack = navController::navigateUp,
                    onWorkoutSelected = { workoutId -> navController.navigate("workout/$workoutId") },
                )
            }
            composable("templates") {
                TemplateManagementRoute(onBack = navController::navigateUp)
            }
            composable(
                route = "workout/{workoutId}",
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
            ) { entry ->
                WorkoutDetailRoute(
                    workoutId = entry.arguments?.getLong("workoutId") ?: return@composable,
                    onBack = navController::navigateUp,
                )
            }
            composable(TopLevelDestination.Progress.route) {
                ProgressRoute()
            }
            composable(TopLevelDestination.Settings.route) {
                SettingsRoute()
            }
            composable(TopLevelDestination.Locations.route) {
                GymLocationsRoute()
            }
            composable(
                route = "exercise/{exerciseId}",
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType }),
            ) { entry ->
                ExerciseHistoryRoute(
                    exerciseId = entry.arguments?.getLong("exerciseId") ?: return@composable,
                    onBack = navController::navigateUp,
                )
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
    data object Locations : TopLevelDestination("locations", "Locations", Icons.Outlined.LocationOn)
    data object Settings : TopLevelDestination("settings", "Settings", Icons.Outlined.Settings)
}

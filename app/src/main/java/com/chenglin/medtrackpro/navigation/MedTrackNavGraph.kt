package com.chenglin.medtrackpro.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chenglin.medtrackpro.ui.screens.addmedication.AddMedicationScreen
import com.chenglin.medtrackpro.ui.screens.clinician.ClinicianDashboardScreen
import com.chenglin.medtrackpro.ui.screens.clinician.ClinicianLoginScreen
import com.chenglin.medtrackpro.ui.screens.home.HomeScreen
import com.chenglin.medtrackpro.ui.screens.login.LoginScreen
import com.chenglin.medtrackpro.ui.screens.medcoach.MedCoachScreen
import com.chenglin.medtrackpro.ui.screens.settings.SettingsScreen
import com.chenglin.medtrackpro.ui.screens.signup.SignUpScreen
import com.chenglin.medtrackpro.ui.screens.symptoms.SymptomsScreen
import com.chenglin.medtrackpro.ui.screens.welcome.WelcomeScreen

@Composable
fun MedTrackNavGraph() {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = viewModel()
    val sessionState by sessionViewModel.uiState.collectAsState()

    if (sessionState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (sessionState.hasValidSession) {
        AppDestination.Home.route
    } else {
        AppDestination.Welcome.route
    }

    Scaffold(
        bottomBar = {
            MedTrackBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(AppDestination.Welcome.route) {
                WelcomeScreen(
                    onLoginClick = {
                        navController.navigate(AppDestination.Login.route)
                    },
                    onSignUpClick = {
                        navController.navigate(AppDestination.SignUp.route)
                    }
                )
            }

            composable(AppDestination.Login.route) {
                LoginScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLoginSuccess = {
                        sessionViewModel.refresh()
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.Welcome.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppDestination.Home.route) {
                HomeScreen(
                    onAddMedicationClick = {
                        navController.navigate(AppDestination.AddMedication.route)
                    },
                    onSymptomsClick = {
                        navController.navigate(AppDestination.Symptoms.route)
                    },
                    onLogoutClick = {
                        logoutAndReturnToWelcome(
                            navController = navController,
                            sessionViewModel = sessionViewModel
                        )
                    }
                )
            }

            composable(AppDestination.AddMedication.route) {
                AddMedicationScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AppDestination.Symptoms.route) {
                SymptomsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AppDestination.MedCoach.route) {
                MedCoachScreen()
            }

            composable(AppDestination.Settings.route) {
                SettingsScreen(
                    onLogoutClick = {
                        logoutAndReturnToWelcome(
                            navController = navController,
                            sessionViewModel = sessionViewModel
                        )
                    },
                    onClinicianLoginClick = {
                        navController.navigate(AppDestination.ClinicianLogin.route)
                    }
                )
            }

            composable(AppDestination.ClinicianLogin.route) {
                ClinicianLoginScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAccessGranted = {
                        navController.navigate(AppDestination.ClinicianDashboard.route)
                    }
                )
            }

            composable(AppDestination.ClinicianDashboard.route) {
                ClinicianDashboardScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AppDestination.SignUp.route) {
                SignUpScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignUpComplete = {
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(AppDestination.Welcome.route)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MedTrackBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topLevelDestinations = listOf(
        AppDestination.Home to "Home",
        AppDestination.Symptoms to "Symptoms",
        AppDestination.MedCoach to "MedCoach",
        AppDestination.Settings to "Settings"
    )

    if (currentRoute !in topLevelDestinations.map { it.first.route }) {
        return
    }

    NavigationBar {
        topLevelDestinations.forEach { (destination, label) ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = {
                    Text(label)
                },
                icon = {
                    Text(label.take(1))
                }
            )
        }
    }
}

// A3: Logout clears session and navigation back stack
private fun logoutAndReturnToWelcome(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    sessionViewModel.logout()
    navController.navigate(AppDestination.Welcome.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

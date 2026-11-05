package com.chenglin.medtrackpro.navigation

sealed class AppDestination(val route: String) {
    data object Welcome : AppDestination("welcome")
    data object Login : AppDestination("login")
    data object Home : AppDestination("home")
    data object AddMedication : AppDestination("add_medication")
    data object Symptoms : AppDestination("symptoms")
    data object MedCoach : AppDestination("medcoach")
    data object Settings : AppDestination("settings")
    data object ClinicianLogin : AppDestination("clinician_login")
    data object ClinicianDashboard : AppDestination("clinician_dashboard")
    data object SignUp : AppDestination("sign_up")
}

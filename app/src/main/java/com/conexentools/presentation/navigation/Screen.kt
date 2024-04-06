package com.conexentools.presentation.navigation

sealed class Screen(val route: String) {
  data object Home : Screen(route = "home_screen")
  data object AddEditClient : Screen(route = "add_edit_client_screen")
  data object Settings : Screen(route = "settings_screen")
  data object About : Screen(route = "about_screen")
}
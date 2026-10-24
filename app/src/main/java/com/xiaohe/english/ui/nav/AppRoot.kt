package com.xiaohe.english.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.ui.screens.CatalogueDetailScreen
import com.xiaohe.english.ui.screens.HomeScreen
import com.xiaohe.english.ui.screens.ListeningExerciseScreen
import com.xiaohe.english.ui.screens.LoginScreen
import com.xiaohe.english.ui.screens.SpeakingExerciseScreen
import com.xiaohe.english.ui.screens.StudyScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val SPLASH = "splash"
    const val STUDY = "study"
    const val CATALOGUE_DETAIL = "catalogue/{catalogueId}/{type}"
    const val LISTENING_EXERCISE = "exercise/listening/{exerciseId}"
    const val SPEAKING_EXERCISE = "exercise/speaking/{exerciseId}"
}

@Composable
fun AppRoot(isLoggedIn: Boolean) {
    val nav = rememberNavController()
    var authed by remember { mutableStateOf(isLoggedIn) }

    LaunchedEffect(Unit) {
        ApiClient.onUnauthorized = {
            authed = false
            nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    val start = if (isLoggedIn) Routes.HOME else Routes.LOGIN
    NavHost(navController = nav, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                authed = true
                nav.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    authed = false
                    nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onOpenStudy = { nav.navigate(Routes.STUDY) },
            )
        }
        composable(Routes.STUDY) {
            StudyScreen(
                onBack = { nav.popBackStack() },
                onOpenCatalogueDetail = { id, type ->
                    nav.navigate("catalogue/" + id + "/" + type)
                },
            )
        }
        composable(Routes.CATALOGUE_DETAIL) { entry ->
            val id = entry.arguments?.getString("catalogueId") ?: ""
            val type = entry.arguments?.getString("type") ?: "listening"
            CatalogueDetailScreen(
                catalogueId = id,
                type = type,
                onBack = { nav.popBackStack() },
                onOpenExercise = { exerciseId, t ->
                    nav.navigate("exercise/" + t + "/" + exerciseId)
                },
            )
        }
        composable(Routes.LISTENING_EXERCISE) { entry ->
            val id = entry.arguments?.getString("exerciseId") ?: ""
            ListeningExerciseScreen(
                exerciseId = id,
                onBack = { nav.popBackStack() },
                onFinish = { _, _ -> nav.popBackStack() },
            )
        }
        composable(Routes.SPEAKING_EXERCISE) { entry ->
            val id = entry.arguments?.getString("exerciseId") ?: ""
            SpeakingExerciseScreen(
                exerciseId = id,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
package com.periocompliance.ai.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.periocompliance.ai.ui.auth.forgot.ForgotPasswordScreen
import com.periocompliance.ai.ui.auth.login.LoginScreen
import com.periocompliance.ai.ui.auth.onboarding.OnboardingScreen
import com.periocompliance.ai.ui.auth.register.RegisterScreen
import com.periocompliance.ai.ui.auth.splash.SplashScreen
import com.periocompliance.ai.ui.auth.splash.StartDestination
import com.periocompliance.ai.ui.auth.verify.VerifyEmailScreen
import com.periocompliance.ai.ui.dashboard.HomeDashboardScreen
import com.periocompliance.ai.ui.designsystem.DesignSystemScreen
import com.periocompliance.ai.ui.scan.DailyScanScreen
import com.periocompliance.ai.ui.scanresult.ScanResultScreen
import com.periocompliance.ai.ui.theme.PerioTheme

/**
 * The navigation shell.
 *
 * Module 1 owns the auth graph. Everything in the main graph is still a [NotBuiltYet] placeholder;
 * building a later module means replacing exactly one of those bodies.
 */
@Composable
fun PerioNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onDecided = { destination ->
                    val route = when (destination) {
                        StartDestination.Onboarding -> Routes.ONBOARDING
                        StartDestination.Login -> Routes.LOGIN
                        StartDestination.VerifyEmail -> Routes.VERIFY_EMAIL
                        StartDestination.Home -> Routes.Graph.MAIN
                        StartDestination.Undecided -> return@SplashScreen
                    }
                    // inclusive = true drops the splash itself, so back from login exits the app
                    // rather than showing the logo again.
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = { navController.toMainGraph() },
                onNeedsVerification = { navController.navigate(Routes.VERIFY_EMAIL) },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                // Registration always lands on the gate, never straight in the app.
                onRegistered = {
                    navController.navigate(Routes.VERIFY_EMAIL) {
                        popUpTo(Routes.LOGIN)
                    }
                },
                onBack = { navController.popBackStack() },
                onSignIn = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.VERIFY_EMAIL) {
            VerifyEmailScreen(
                onVerified = { navController.toMainGraph() },
                onSignedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        navigation(startDestination = Routes.HOME_DASHBOARD, route = Routes.Graph.MAIN) {
            composable(Routes.HOME_DASHBOARD) {
                HomeDashboardScreen(
                    onStartScan = { navController.navigate(Routes.DAILY_SCAN) },
                    onOpenHistory = { navController.navigate(Routes.HISTORY) },
                    onOpenProgress = { navController.navigate(Routes.PROGRESS_DASHBOARD) },
                    onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    // Signing out burns the whole stack back to the login screen, the same way the
                    // verification gate does.
                    onSignedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.DAILY_SCAN) {
                DailyScanScreen(
                    // Both routes return to the dashboard; the combined summary flow means the
                    // progress cards already reflect the new scan by the time we land back.
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() },
                )
            }
            composable(Routes.AI_RESULT) { backStackEntry ->
                val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
                ScanResultScreen(
                    scanId = scanId,
                    onClose = { navController.popBackStack() },
                )
            }
            composable(Routes.HISTORY) { NotBuiltYet("History", module = 6) }
            composable(Routes.PROGRESS_DASHBOARD) { NotBuiltYet("Progress Dashboard", module = 7) }
            composable(Routes.NOTIFICATIONS) { NotBuiltYet("Notifications", module = 8) }
            composable(Routes.DENTIST_DASHBOARD) { NotBuiltYet("Dentist Dashboard", module = 9) }
            composable(Routes.PROFILE) { NotBuiltYet("Profile", module = 2) }
        }

        // Kept reachable for eyeballing the tokens against the exported PNGs. Delete once the real
        // screens exist.
        composable(Routes.DESIGN_SYSTEM) { DesignSystemScreen() }
    }
}

/**
 * Enter the app and burn the whole auth stack behind us.
 *
 * popUpTo(0) rather than popUpTo(SPLASH): by the time a user reaches here the splash may already
 * be gone (they came via login -> register -> verify), and popping to a destination that is not on
 * the back stack is a no-op -- which would leave the back button walking back into the login form
 * of an account that is already signed in.
 */
private fun NavHostController.toMainGraph() {
    navigate(Routes.Graph.MAIN) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun NotBuiltYet(screen: String, module: Int) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PerioTheme.spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(PerioTheme.spacing.sm, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = screen,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Module $module",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Not built yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

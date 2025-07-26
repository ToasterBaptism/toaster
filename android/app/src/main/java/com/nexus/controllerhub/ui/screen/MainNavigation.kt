package com.nexus.controllerhub.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nexus.controllerhub.controller.ControllerManager

@Composable
fun MainNavigation(
    controllerManager: ControllerManager,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "onboarding",
        modifier = modifier
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onNavigateToProfiles = { navController.navigate("profiles") },
                onNavigateToConfiguration = { profileId ->
                    navController.navigate("configuration/$profileId")
                },
                onNavigateToMacros = { navController.navigate("macros") },
                onNavigateToTroubleshooting = { navController.navigate("troubleshooting") },
                onNavigateToDeviceSelection = { navController.navigate("device_selection") },
                onNavigateToLiveTest = { navController.navigate("live_test") },
                onNavigateToButtonRemapping = { navController.navigate("button_remapping") }
            )
        }
        
        composable("profiles") {
            ProfilesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToConfiguration = { profileId ->
                    navController.navigate("configuration/$profileId")
                }
            )
        }
        
        composable("configuration/{profileId}") { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")?.toLongOrNull() ?: 0L
            SimpleConfigurationScreen(
                controllerManager = controllerManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("macros") {
            WorkingMacrosScreen(
                controllerManager = controllerManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMacroEditor = { macroId ->
                    navController.navigate("macro_editor/$macroId")
                }
            )
        }
        
        composable("macro_editor/{macroId}") { backStackEntry ->
            val macroId = backStackEntry.arguments?.getString("macroId")?.toLongOrNull() ?: 0L
            MacroEditorScreen(
                macroId = macroId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("troubleshooting") {
            TroubleshootingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("device_selection") {
            DeviceSelectionScreen(
                controllerManager = controllerManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("live_test") {
            RealLiveTestScreen(
                controllerManager = controllerManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("button_remapping") {
            ButtonRemappingScreen(
                controllerManager = controllerManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
package com.nexus.controllerhub.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.controllerhub.R
import com.nexus.controllerhub.service.ControllerAccessibilityService
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var showSkipDialog by remember { mutableStateOf(false) }
    
    // Check accessibility service status periodically
    LaunchedEffect(Unit) {
        while (true) {
            isAccessibilityEnabled = ControllerAccessibilityService.isServiceEnabled()
            delay(1000) // Check every second
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome Section
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Accessibility Service Setup
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isAccessibilityEnabled) 
                    MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isAccessibilityEnabled) Icons.Default.Check else Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (isAccessibilityEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.onboarding_accessibility_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.onboarding_accessibility_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isAccessibilityEnabled) {
                    Text(
                        text = stringResource(R.string.onboarding_complete),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.onboarding_accessibility_button))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Action Buttons
        if (isAccessibilityEnabled) {
            Button(
                onClick = onNavigateToDashboard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.next))
            }
        } else {
            OutlinedButton(
                onClick = { showSkipDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }
    }
    
    // Skip confirmation dialog
    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = { Text("Skip Setup?") },
            text = { 
                Text("Without accessibility permissions, the app won't be able to remap controller inputs globally. You can enable this later in settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSkipDialog = false
                        onNavigateToDashboard()
                    }
                ) {
                    Text("Skip")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSkipDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
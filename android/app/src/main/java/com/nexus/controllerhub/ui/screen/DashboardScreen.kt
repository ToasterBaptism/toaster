package com.nexus.controllerhub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.controllerhub.R
import com.nexus.controllerhub.data.database.ControllerDatabase
import com.nexus.controllerhub.data.repository.ControllerRepository
import com.nexus.controllerhub.service.ControllerAccessibilityService
import com.nexus.controllerhub.ui.viewmodel.DashboardViewModel
import com.nexus.controllerhub.ui.viewmodel.DashboardViewModelFactory
import com.nexus.controllerhub.util.ControllerDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToProfiles: () -> Unit,
    onNavigateToConfiguration: (Long) -> Unit,
    onNavigateToMacros: () -> Unit,
    onNavigateToTroubleshooting: () -> Unit = {},
    onNavigateToDeviceSelection: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = ControllerDatabase.getDatabase(context)
    val repository = ControllerRepository(database.profileDao(), database.macroDao())
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository, context)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val connectedControllers by viewModel.connectedControllers.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState(initial = null)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = { 
                Text(
                    text = stringResource(R.string.dashboard_title),
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Accessibility Service Status
        if (!ControllerAccessibilityService.isServiceEnabled()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.error_accessibility_not_enabled),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Controller Status
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Controller Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (connectedControllers.isEmpty()) {
                    Text(
                        text = stringResource(R.string.dashboard_no_controller),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    connectedControllers.forEach { controller ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = controller.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Device ID: ${controller.deviceId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Connected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (controller != connectedControllers.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Active Profile
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Active Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (activeProfile == null) {
                    Text(
                        text = stringResource(R.string.dashboard_no_active_profile),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeProfile!!.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (activeProfile!!.description.isNotEmpty()) {
                                Text(
                                    text = activeProfile!!.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { onNavigateToConfiguration(activeProfile!!.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile"
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Actions
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                QuickActionCard(
                    title = "Manage Profiles",
                    description = "Create, edit, and organize controller profiles",
                    icon = Icons.Default.AccountBox,
                    onClick = onNavigateToProfiles
                )
            }
            
            item {
                QuickActionCard(
                    title = "Record Macros",
                    description = "Create and manage controller macros",
                    icon = Icons.Default.PlayArrow,
                    onClick = onNavigateToMacros
                )
            }
            
            if (activeProfile != null) {
                item {
                    QuickActionCard(
                        title = "Configure Controller",
                        description = "Customize button mapping and calibration",
                        icon = Icons.Default.Settings,
                        onClick = { onNavigateToConfiguration(activeProfile!!.id) }
                    )
                }
            }
            
            item {
                QuickActionCard(
                    title = "Device Selection",
                    description = "Select specific controller device to use",
                    icon = Icons.Default.Gamepad,
                    onClick = onNavigateToDeviceSelection
                )
            }
            
            item {
                QuickActionCard(
                    title = "Troubleshooting",
                    description = "Diagnose input issues and check device status",
                    icon = Icons.Default.BugReport,
                    onClick = onNavigateToTroubleshooting
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
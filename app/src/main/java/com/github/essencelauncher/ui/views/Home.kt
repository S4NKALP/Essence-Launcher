/*
 * Portions of this file are from George Clensy
 * Copyright (c) 2025 George Clensy
 *
 * Modifications Copyright (c) 2025 Sankalp Tharu
 *
 * Licensed under the MIT License.
 * See the LICENSE file in this project for details.
 */

package com.github.essencelauncher.ui.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue


import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.github.essencelauncher.R
import com.github.essencelauncher.ui.components.BatteryIndicator
import com.github.essencelauncher.utils.AppUtils


import com.github.essencelauncher.utils.AppUtils.getCurrentTime
import com.github.essencelauncher.utils.AppUtils.getCurrentDate
import com.github.essencelauncher.utils.AppUtils.resetHome
import com.github.essencelauncher.utils.AppUtils.openPhoneApp
import com.github.essencelauncher.utils.AppUtils.openMessagingApp
import com.github.essencelauncher.utils.managers.ItemAlignmentManager
import com.github.essencelauncher.utils.getBooleanSetting


import com.github.essencelauncher.utils.getStringSetting
import kotlinx.coroutines.delay
import com.github.essencelauncher.MainAppViewModel as MainAppModel

/**
 * Parent main home screen composable
 */
@Composable
fun HomeScreen(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    onOpenSettings: () -> Unit,
    activity: FragmentActivity
) {
    val scrollState = rememberLazyListState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp, 0.dp)
    ) {
        // Clock at top
        if (getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowClock), true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp)
            ) {
                Clock(
                    itemAlignmentManager = mainAppModel.itemAlignmentManager
                )
            }
        }

        // Battery indicator - positioned independently
        if (getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowBattery), true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = calculateBatteryTopPadding(mainAppModel.getContext()))
            ) {
                BatteryIndicator(
                    itemAlignmentManager = mainAppModel.itemAlignmentManager
                )
            }
        }

        // Favorite apps at center
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.Center, // Hardcoded center alignment for favorite apps
            horizontalAlignment = mainAppModel.itemAlignmentManager.getFavoriteAppsAlignmentAsHorizontal(),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = calculateTopPadding(mainAppModel.getContext()))
        ) {
        // Clock is now positioned separately at the top

        //Apps
        if (homeScreenModel.isLoadingFavorites) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            items(homeScreenModel.favoriteApps) { app ->
                HomeScreenItem(
                    appName = app.displayName,
                    onAppClick = {
                        homeScreenModel.updateSelectedApp(app)

                        AppUtils.openApp(
                            app = app,
                            mainAppModel = mainAppModel,
                            homeScreenModel = homeScreenModel,
                            activity = activity
                        )

                        resetHome(homeScreenModel)
                    },
                    onAppLongClick = {
                        homeScreenModel.showBottomSheet.value = true
                        homeScreenModel.updateSelectedApp(app)
                    },
                    modifier = Modifier,
                    alignment = mainAppModel.itemAlignmentManager.getFavoriteAppsAlignmentAsHorizontal(),
                    isLocked = homeScreenModel.lockedApps.contains(app.packageName)
                )
            }
        }

        //First time help
        if (getBooleanSetting(
                mainAppModel.getContext(),
                mainAppModel.getContext().resources.getString(R.string.FirstTimeAppDrawHelp),
                true
            )
        ) {
            item {
                Spacer(Modifier.height(15.dp))
            }

            item {
                FirstTimeHelp(mainAppModel.getContext())
            }
        }

        }

        // Bottom dock with phone, message, and settings icons
        BottomDock(
            context = mainAppModel.getContext(),
            itemAlignmentManager = mainAppModel.itemAlignmentManager,
            onOpenSettings = onOpenSettings,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
        )
    }
}

/**
 * Clock to be shown on home screen
 */
@Composable
fun Clock(
    itemAlignmentManager: ItemAlignmentManager
) {
    val context = LocalContext.current
    var time by remember { mutableStateOf(getCurrentTime(context)) }
    var date by remember { mutableStateOf(getCurrentDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            time = getCurrentTime(context)
            date = getCurrentDate()
            delay(1000) // Update every second
        }
    }

    val clockAlignment = itemAlignmentManager.getClockAlignmentAsHorizontal()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when (clockAlignment) {
            Alignment.Start -> Alignment.Start
            Alignment.End -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    ) {
        // Time display
        Text(
            text = time,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = when (clockAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )

        // Date display
        Text(
            text = date,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            textAlign = when (clockAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}



/**
 * Block with tips for first time users
 */
@Composable
fun FirstTimeHelp(context: Context) {
    Box(
        Modifier.clip(MaterialTheme.shapes.extraLarge)
    ) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier
                    .padding(25.dp, 25.dp, 25.dp, 15.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.swipe_for_all_apps),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                Modifier
                    .padding(25.dp, 0.dp, 25.dp, 25.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.Settings,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.hold_for_settings),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Bottom dock with phone, message, and settings icons
 */
@Composable
fun BottomDock(
    context: Context,
    itemAlignmentManager: ItemAlignmentManager,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dockAlignment = itemAlignmentManager.getBottomDockAlignmentAsHorizontal()

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.align(
                when (dockAlignment) {
                    Alignment.Start -> Alignment.BottomStart
                    Alignment.End -> Alignment.BottomEnd
                    else -> Alignment.BottomCenter
                }
            ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Phone icon
                IconButton(
                    onClick = { openPhoneApp(context) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Message icon
                IconButton(
                    onClick = { openMessagingApp(context) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Messages",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Settings icon
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Calculate top padding for the battery indicator based on clock visibility
 */
@Composable
private fun calculateBatteryTopPadding(context: Context): androidx.compose.ui.unit.Dp {
    val showClock = getBooleanSetting(context, stringResource(R.string.ShowClock), true)
    return if (showClock) 185.dp else 90.dp // Position below clock and date if shown, otherwise at top
}

/**
 * Calculate top padding for the favorite apps list based on what indicators are shown
 */
@Composable
private fun calculateTopPadding(context: Context): androidx.compose.ui.unit.Dp {
    val showClock = getBooleanSetting(context, stringResource(R.string.ShowClock), true)
    val showBattery = getBooleanSetting(context, stringResource(R.string.ShowBattery), true)

    return when {
        showClock && showBattery -> 220.dp // Both clock and battery (battery below date)
        showClock -> 180.dp // Only clock
        showBattery -> 140.dp // Only battery
        else -> 90.dp // Neither
    }
}

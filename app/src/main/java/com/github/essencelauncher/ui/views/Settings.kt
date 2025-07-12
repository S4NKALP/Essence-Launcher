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

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.essencelauncher.MainAppViewModel
import com.github.essencelauncher.MainHomeScreen
import com.github.essencelauncher.R
import com.github.essencelauncher.ui.theme.AppTheme
import com.github.essencelauncher.ui.theme.refreshTheme
import com.github.essencelauncher.ui.theme.transparentHalf
import com.github.essencelauncher.utils.AppUtils
import com.github.essencelauncher.utils.AppUtils.loadTextFromAssets
import com.github.essencelauncher.utils.BiometricAuthenticationHelper
import com.github.essencelauncher.utils.changeAppsAlignment
import com.github.essencelauncher.utils.managers.ItemAlignmentManager
import com.github.essencelauncher.utils.AccessibilityServiceManager

import com.github.essencelauncher.utils.getAppsAlignmentAsInt
import com.github.essencelauncher.utils.getBooleanSetting


import com.github.essencelauncher.utils.getIntSetting
import com.github.essencelauncher.utils.isDefaultLauncher
import com.github.essencelauncher.utils.resetActivity
import com.github.essencelauncher.utils.setBooleanSetting
import com.github.essencelauncher.utils.setIntSetting
import com.github.essencelauncher.utils.setStringSetting
import com.github.essencelauncher.utils.showLauncherSelector
import com.github.essencelauncher.utils.showLauncherSettingsMenu
import com.github.essencelauncher.utils.toggleBooleanSetting
import com.github.essencelauncher.MainAppViewModel as MainAppModel

/**
 * Settings title header with back button
 *
 * @param title The text shown on the header
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsHeader(goBack: () -> Unit, title: String) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = { goBack() })
            .padding(0.dp, 120.dp, 0.dp, 0.dp)
            .height(70.dp) // Set a fixed height for the header
    ) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Go Back",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontSize = if (title.length > 11) 35.sp else MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

/**
 * Switch for setting with a label on the left
 *
 * @param label The text for the label
 * @param checked Whether the switch is on or not
 * @param onCheckedChange Function with Boolean passed that's executed when the switch is pressed
 */
@Composable
fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(checked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp), // Add space between text and switch
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                onCheckedChange(isChecked)
            }
        )
    }
}


/**
 * Settings navigation item with label and arrow
 *
 * @param label The text to be shown
 * @param diagonalArrow Whether the arrow should be pointed upwards to signal that pressing this will take you out of Essence Launcher
 * @param onClick When composable is clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsNavigationItem(
    label: String, diagonalArrow: Boolean?, onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            Modifier
                .padding(0.dp, 15.dp)
                .fillMaxWidth(0.9f)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Left,
        )
        if (!diagonalArrow!!) {
            Icon(
                Icons.AutoMirrored.Default.KeyboardArrowRight,
                "",
                Modifier
                    .size(48.dp)
                    .fillMaxSize(0.1f)
                    .fillMaxHeight(),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            Icon(
                Icons.AutoMirrored.Default.KeyboardArrowRight,
                "",
                Modifier
                    .size(48.dp)
                    .fillMaxWidth(0.1f)
                    .fillMaxHeight()
                    .rotate(-45f),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/**
 * Main Settings window you see when settings is first opened
 *
 * @param mainAppModel This is needed to get packageManager, context, ect
 * @param goBack When back button is pressed
 * @param activity This is needed for some settings
 */
@Composable
fun Settings(
    mainAppModel: MainAppModel,
    goBack: () -> Unit,
    activity: Activity,
) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(20.dp, 0.dp, 20.dp, 0.dp)
    ) {

        val navController = rememberNavController()

        NavHost(navController = navController, "mainSettingsPage") {
            composable(
                "mainSettingsPage",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                MainSettingsPage(
                    { goBack() },
                    navController,
                    mainAppModel,
                    activity
                )
            }
            composable(
                "alignmentOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                AlignmentOptions(mainAppModel, mainAppModel.getContext()) { navController.popBackStack() }
            }
            composable(
                "hiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                HiddenApps(
                    mainAppModel
                ) { navController.popBackStack() }
            }
            composable(
                "chooseFont",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ChooseFont(mainAppModel.getContext(), activity) { navController.popBackStack() }
            }
            composable(
                "devOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                DevOptions(context = mainAppModel.getContext()) { navController.popBackStack() }
            }
            composable(
                "theme",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ThemeOptions(
                    mainAppModel, mainAppModel.getContext()
                ) { navController.popBackStack() }
            }
            composable(
                "personalization",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                PersonalizationOptions(mainAppModel, navController) { navController.popBackStack() }
            }

        }
    }


}

/**
 * Fist page of settings, contains navigation to all the other pages
 *
 * @param goBack When back button is pressed
 * @param navController Settings nav controller with "personalization", "hiddenApps"
 * @param mainAppModel This is required for settings to be changed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goBack: () -> Unit,
    navController: NavController,
    mainAppModel: MainAppModel,
    activity: Activity
) {

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.settings))

        SettingsNavigationItem(
            label = stringResource(id = R.string.personalization),
            false,
            onClick = { navController.navigate("personalization") })

        SettingsNavigationItem(
            label = stringResource(id = R.string.manage_hidden_apps),
            false,
            onClick = {
                // Require authentication before accessing hidden apps
                if (activity is FragmentActivity) {
                    val biometricHelper = BiometricAuthenticationHelper(activity)
                    biometricHelper.authenticateForHiddenApps(
                        object : BiometricAuthenticationHelper.AuthenticationCallback {
                            override fun onAuthenticationSucceeded() {
                                navController.navigate("hiddenApps")
                            }

                            override fun onAuthenticationFailed() {
                                // Authentication failed, do nothing
                            }

                            override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                                // Authentication error, do nothing
                            }
                        }
                    )
                } else {
                    // Fallback if not FragmentActivity (shouldn't happen)
                    navController.navigate("hiddenApps")
                }
            })







        SettingsNavigationItem(
            label = stringResource(id = R.string.make_default_launcher), true, onClick = {
                if (!isDefaultLauncher(activity)) {
                    activity.showLauncherSelector()
                } else {
                    showLauncherSettingsMenu(activity)
                }
            })

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Text(
            stringResource(id = R.string.essence_launcher) + " " + stringResource(id = R.string.app_version),
            Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {}, onLongClick = {
                    navController.navigate("devOptions")
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(25.dp))
    }
}

/**
 * Personalization options in settings
 *
 * @param mainAppModel This is required for settings to be changed
 * @param navController Settings nav controller with "alignmentOptions", "chooseFont", "theme"
 * @param goBack When back button is pressed
 *
 * @see Settings
 * @see MainSettingsPage
 */
@Composable
fun PersonalizationOptions(
    mainAppModel: MainAppViewModel, navController: NavController, goBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainHomeScreen
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.personalization))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        SettingsSwitch(
            label = stringResource(id = R.string.search_box), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowSearchBox), true
            ), onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ShowSearchBox)
                )
            })

        SettingsSwitch(
            label = stringResource(id = R.string.auto_open), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.SearchAutoOpen)
            ), onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.SearchAutoOpen)
                )
            })

        SettingsSwitch(
            label = stringResource(id = R.string.show_clock), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowClock), true
            ), onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ShowClock)
                )
            })

        SettingsSwitch(
            label = stringResource(id = R.string.show_battery), checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowBattery), true
            ), onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.getContext(),
                    it,
                    mainAppModel.getContext().resources.getString(R.string.ShowBattery)
                )
            })

        SettingsSwitch(
            label = stringResource(id = R.string.use_24_hour_format),
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.Use24HourFormat), false
            ),
            onCheckedChange = {
                setBooleanSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().resources.getString(R.string.Use24HourFormat),
                    it
                )
            })

        // Max Favorite Apps Setting
        Column(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                stringResource(id = R.string.max_favorite_apps),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedMaxApps by remember {
                mutableIntStateOf(
                    getIntSetting(mainAppModel.getContext(), mainAppModel.getContext().getString(R.string.MaxFavoriteApps), 5)
                )
            }
            val maxAppsOptions = listOf("5", "6", "7", "8", "9", "10")
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(350.dp)
            ) {
                maxAppsOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = maxAppsOptions.size
                        ), onClick = {
                            selectedMaxApps = index + 5 // 5, 6, 7, 8, 9, 10
                            setIntSetting(
                                mainAppModel.getContext(),
                                mainAppModel.getContext().resources.getString(R.string.MaxFavoriteApps),
                                selectedMaxApps
                            )
                        }, selected = selectedMaxApps == index + 5
                    ) {
                        Text(label)
                    }
                }
            }
        }

        SettingsSwitch(
            label = stringResource(id = R.string.show_status_bar),
            checked = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ShowStatusBar), true
            ),
            onCheckedChange = {
                setBooleanSetting(
                    mainAppModel.getContext(),
                    mainAppModel.getContext().resources.getString(R.string.ShowStatusBar),
                    it
                )
                // Refresh the status bar visibility immediately
                activity?.refreshStatusBarVisibility()
            })

        // Get the setting key outside of remember block
        val doubleTapSettingKey = stringResource(R.string.DoubleTapLockScreen)

        // State for double tap lock screen toggle
        val isDoubleTapEnabled = remember {
            mutableStateOf(
                getBooleanSetting(mainAppModel.getContext(), doubleTapSettingKey, false) &&
                AccessibilityServiceManager.isAccessibilityServiceEnabled(mainAppModel.getContext())
            )
        }

        // Update state when composition recomposes (e.g., when user returns from accessibility settings)
        LaunchedEffect(Unit) {
            isDoubleTapEnabled.value = getBooleanSetting(mainAppModel.getContext(), doubleTapSettingKey, false) &&
                    AccessibilityServiceManager.isAccessibilityServiceEnabled(mainAppModel.getContext())
        }

        SettingsSwitch(
            label = stringResource(id = R.string.double_tap_lock_screen),
            checked = isDoubleTapEnabled.value,
            onCheckedChange = { enabled ->
                if (enabled) {
                    // Always redirect to accessibility settings when enabling
                    AccessibilityServiceManager.openAccessibilitySettings(mainAppModel.getContext())
                    // Set the setting to true - it will be checked when user returns
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        doubleTapSettingKey,
                        true
                    )
                } else {
                    // Disable the setting
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        doubleTapSettingKey,
                        false
                    )
                    isDoubleTapEnabled.value = false
                }
            })

        // Description text for double tap lock screen
        Text(
            text = stringResource(id = R.string.double_tap_lock_screen_description),
            modifier = Modifier.padding(start = 0.dp, top = 4.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )

        SettingsNavigationItem(
            label = stringResource(id = R.string.theme),
            false,
            onClick = { navController.navigate("theme") })

        SettingsNavigationItem(
            label = stringResource(id = R.string.alignments),
            false,
            onClick = { navController.navigate("alignmentOptions") })

        SettingsNavigationItem(
            label = stringResource(id = R.string.choose_font),
            false,
            onClick = { navController.navigate("chooseFont") })

        Spacer(Modifier.height(120.dp))
    }
}




/**
 * Alignment Options in Settings
 *
 * @param context Context is required by some functions used withing AlignmentOptions
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@Composable
fun AlignmentOptions(mainAppModel: MainAppViewModel, context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.alignments))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        // Home alignment section removed - now using per-item alignment

        Column(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                "Clock Alignment",
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedClockIndex by remember {
                mutableIntStateOf(
                    mainAppModel.itemAlignmentManager.getClockAlignment()
                )
            }
            val clockOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(275.dp)
            ) {
                clockOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = clockOptions.size
                        ), onClick = {
                            selectedClockIndex = index
                            mainAppModel.itemAlignmentManager.setClockAlignment(selectedClockIndex)
                        }, selected = index == selectedClockIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                stringResource(R.string.battery_alignment),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedBatteryIndex by remember {
                mutableIntStateOf(
                    mainAppModel.itemAlignmentManager.getBatteryAlignment()
                )
            }
            val batteryOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(275.dp)
            ) {
                batteryOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = batteryOptions.size
                        ), onClick = {
                            selectedBatteryIndex = index
                            mainAppModel.itemAlignmentManager.setBatteryAlignment(selectedBatteryIndex)
                        }, selected = index == selectedBatteryIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                "Favorite Apps Alignment",
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedFavoriteAppsIndex by remember {
                mutableIntStateOf(
                    mainAppModel.itemAlignmentManager.getFavoriteAppsAlignment()
                )
            }
            val favoriteAppsOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(275.dp)
            ) {
                favoriteAppsOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = favoriteAppsOptions.size
                        ), onClick = {
                            selectedFavoriteAppsIndex = index
                            mainAppModel.itemAlignmentManager.setFavoriteAppsAlignment(selectedFavoriteAppsIndex)
                        }, selected = index == selectedFavoriteAppsIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }

        // Vertical alignment removed - clock is hardcoded at top, favorite apps at center

        Column(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                "Bottom Dock Alignment",
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedBottomDockIndex by remember {
                mutableIntStateOf(
                    mainAppModel.itemAlignmentManager.getBottomDockAlignment()
                )
            }
            val bottomDockOptions = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(275.dp)
            ) {
                bottomDockOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = bottomDockOptions.size
                        ), onClick = {
                            selectedBottomDockIndex = index
                            mainAppModel.itemAlignmentManager.setBottomDockAlignment(selectedBottomDockIndex)
                        }, selected = index == selectedBottomDockIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                stringResource(id = R.string.apps),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedIndex by remember {
                mutableIntStateOf(
                    getAppsAlignmentAsInt(context)
                )
            }
            val options = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(275.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = options.size
                        ), onClick = {
                            selectedIndex = index
                            changeAppsAlignment(context, selectedIndex)
                        }, selected = index == selectedIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

/**
 * Theme select card
 *
 * @param theme The theme ID number (see: Theme.kt)
 *
 * @see com.github.essencelauncher.ui.theme.EssenceTheme
 */
@Composable
fun ThemeCard(
    theme: Int,
    showLightDarkPicker: MutableState<Boolean>,
    isSelected: MutableState<Boolean>,
    isDSelected: MutableState<Boolean>,
    isLSelected: MutableState<Boolean>,
    updateLTheme: (theme: Int) -> Unit,
    updateDTheme: (theme: Int) -> Unit,
    onClick: (theme: Int) -> Unit
) {
    Box(
        Modifier
            .size(120.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onClick(theme)
            }
            .background(AppTheme.fromId(theme).scheme.background)
    ) {
        Text(
            stringResource(AppTheme.nameResFromId(theme)),
            Modifier
                .align(Alignment.Center)
                .padding(5.dp),
            AppTheme.fromId(theme).scheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        AnimatedVisibility(
            isSelected.value && !showLightDarkPicker.value && !showLightDarkPicker.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        2.dp,
                        AppTheme.fromId(theme).scheme.onPrimaryContainer,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        "",
                        tint = AppTheme.fromId(theme).scheme.onPrimaryContainer
                    )
                }
            }
        }

        AnimatedVisibility(
            isSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        2.dp,
                        AppTheme.fromId(theme).scheme.onPrimaryContainer,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        "",
                        tint = AppTheme.fromId(theme).scheme.onPrimaryContainer
                    )
                }
            }
        }

        AnimatedVisibility(
            isDSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        2.dp,
                        AppTheme.fromId(theme).scheme.onPrimaryContainer,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.dark_mode),
                        "",
                        tint = AppTheme.fromId(theme).scheme.onPrimaryContainer
                    )
                }
            }
        }

        AnimatedVisibility(
            isLSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        2.dp,
                        AppTheme.fromId(theme).scheme.onPrimaryContainer,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.light_mode),
                        "",
                        tint = AppTheme.fromId(theme).scheme.onPrimaryContainer
                    )
                }
            }
        }

        AnimatedVisibility(showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(transparentHalf)
            ) {
                Button(
                    onClick = {
                        updateLTheme(theme)
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(10.dp, 5.dp, 10.dp, 2.5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.fromId(theme).scheme.primary,
                        contentColor = AppTheme.fromId(theme).scheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.light))
                }

                Button(
                    onClick = {
                        updateDTheme(theme)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(10.dp, 2.5.dp, 10.dp, 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.fromId(theme).scheme.primary,
                        contentColor = AppTheme.fromId(theme).scheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.dark))
                }

            }
        }
    }
}

/**
 * Theme options in settings
 *
 * @param mainAppModel Main app model for theme updates
 * @param context Needed to run some functions used within ThemeOptions
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeOptions(
    mainAppModel: MainAppModel, context: Context, goBack: () -> Unit
) {
    val settingToChange = stringResource(R.string.theme)
    val autoThemeChange = stringResource(R.string.autoThemeSwitch)
    val dSettingToChange = stringResource(R.string.dTheme)
    val lSettingToChange = stringResource(R.string.lTheme)
    val isSystemDark = isSystemInDarkTheme()

    // Current highlighted theme card
    val currentHighlightedThemeCard = remember { mutableIntStateOf(-1) }

    // Current selected themes
    val currentSelectedTheme = remember {
        mutableIntStateOf(getIntSetting(context, settingToChange, -1))
    }
    val currentSelectedDTheme = remember {
        mutableIntStateOf(getIntSetting(context, dSettingToChange, -1))
    }
    val currentSelectedLTheme = remember {
        mutableIntStateOf(getIntSetting(context, lSettingToChange, -1))
    }

    // Initialize selection states based on settings
    if (!getBooleanSetting(context, autoThemeChange, true)) {
        currentSelectedDTheme.intValue = -1
        currentSelectedLTheme.intValue = -1
    } else {
        currentSelectedTheme.intValue = -1
    }

    val backgroundInteractionSource = remember { MutableInteractionSource() }

    LazyVerticalGrid(
        GridCells.Adaptive(minSize = 128.dp), modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {
                    currentHighlightedThemeCard.intValue = -1
                },
                indication = null,
                onLongClick = {},
                interactionSource = backgroundInteractionSource
            )
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SettingsHeader(goBack, stringResource(R.string.theme))
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            HorizontalDivider(Modifier.padding(0.dp, 15.dp, 0.dp, 0.dp))
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(30.dp))
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            SettingsSwitch(
                stringResource(R.string.syncLightDark), getBooleanSetting(
                    context, context.getString(R.string.autoThemeSwitch), true
                )
            ) { switch ->
                // Disable normal selection box or set it correctly
                if (switch) {
                    currentSelectedTheme.intValue = -1
                } else {
                    currentSelectedTheme.intValue = getIntSetting(context, settingToChange, 0)
                }

                if (switch) {
                    currentSelectedDTheme.intValue = getIntSetting(context, dSettingToChange, -1)
                    currentSelectedLTheme.intValue = getIntSetting(context, lSettingToChange, -1)
                } else {
                    currentSelectedDTheme.intValue = -1
                    currentSelectedLTheme.intValue = -1
                }

                // Remove the light dark button
                currentHighlightedThemeCard.intValue = -1

                setBooleanSetting(
                    context, context.getString(R.string.autoThemeSwitch), switch
                )

                // Reload
                val newTheme = refreshTheme(
                    context = context,
                    settingToChange = context.getString(R.string.theme),
                    autoThemeChange = context.getString(R.string.autoThemeSwitch),
                    dSettingToChange = context.getString(R.string.dTheme),
                    lSettingToChange = context.getString(R.string.lTheme),
                    isSystemDarkTheme = isSystemDark
                )
                mainAppModel.appTheme.value = newTheme
                // Refresh status bar appearance for new theme
                (context as? MainHomeScreen)?.refreshStatusBarVisibility()
            }
        }

        // Create theme cards
        val themeIds = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)

        themeIds.forEach { themeId ->
            item {
                ThemeCard(
                    theme = themeId,
                    showLightDarkPicker = mutableStateOf(currentHighlightedThemeCard.intValue == themeId),
                    isSelected = mutableStateOf(currentSelectedTheme.intValue == themeId),
                    isDSelected = mutableStateOf(currentSelectedDTheme.intValue == themeId),
                    isLSelected = mutableStateOf(currentSelectedLTheme.intValue == themeId),
                    updateLTheme = { theme ->
                        setIntSetting(context, context.getString(R.string.lTheme), theme)
                        val newTheme = refreshTheme(
                            context = context,
                            settingToChange = context.getString(R.string.theme),
                            autoThemeChange = context.getString(R.string.autoThemeSwitch),
                            dSettingToChange = context.getString(R.string.dTheme),
                            lSettingToChange = context.getString(R.string.lTheme),
                            isSystemDarkTheme = isSystemDark
                        )
                        mainAppModel.appTheme.value = newTheme
                        // Refresh status bar appearance for new theme
                        (context as? MainHomeScreen)?.refreshStatusBarVisibility()
                        currentSelectedLTheme.intValue = theme
                        currentHighlightedThemeCard.intValue = -1
                    },
                    updateDTheme = { theme ->
                        setIntSetting(context, context.getString(R.string.dTheme), theme)
                        val newTheme = refreshTheme(
                            context = context,
                            settingToChange = context.getString(R.string.theme),
                            autoThemeChange = context.getString(R.string.autoThemeSwitch),
                            dSettingToChange = context.getString(R.string.dTheme),
                            lSettingToChange = context.getString(R.string.lTheme),
                            isSystemDarkTheme = isSystemDark
                        )
                        mainAppModel.appTheme.value = newTheme
                        // Refresh status bar appearance for new theme
                        (context as? MainHomeScreen)?.refreshStatusBarVisibility()
                        currentSelectedDTheme.intValue = theme
                        currentHighlightedThemeCard.intValue = -1
                    }) { theme ->
                    if (getBooleanSetting(
                            context, context.getString(R.string.autoThemeSwitch), true
                        )
                    ) {
                        // For auto theme mode, show light/dark picker
                        currentHighlightedThemeCard.intValue = theme
                    } else {
                        // For single theme mode, just set the theme
                        setIntSetting(context, context.getString(R.string.theme), theme)
                        val newTheme = refreshTheme(
                            context = context,
                            settingToChange = context.getString(R.string.theme),
                            autoThemeChange = context.getString(R.string.autoThemeSwitch),
                            dSettingToChange = context.getString(R.string.dTheme),
                            lSettingToChange = context.getString(R.string.lTheme),
                            isSystemDarkTheme = isSystemDark
                        )
                        mainAppModel.appTheme.value = newTheme
                        // Refresh status bar appearance for new theme
                        (context as? MainHomeScreen)?.refreshStatusBarVisibility()
                        currentSelectedTheme.intValue = theme
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(128.dp))
        }
    }
}

/**
 * Page that lets you manage hidden apps
 *
 * @param mainAppModel Needed for context & hidden apps manager
 * @param goBack Function run when back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HiddenApps(
    mainAppModel: MainAppModel, goBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.hidden_apps))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))


        val hiddenApps = remember { mutableStateOf(mainAppModel.hiddenAppsManager.getHiddenApps()) }

        for (app in hiddenApps.value) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    AppUtils.getAppNameFromPackageName(mainAppModel.getContext(), app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent =
                                mainAppModel.getContext().packageManager.getLaunchIntentForPackage(
                                    app
                                )
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeBasic()
                                mainAppModel.getContext()
                                    .startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            mainAppModel.hiddenAppsManager.removeHiddenApp(app)
                            hiddenApps.value = mainAppModel.hiddenAppsManager.getHiddenApps()
                        }),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )

                Icon(
                    Icons.Sharp.Close,
                    "",
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .fillMaxSize()
                        .combinedClickable(onClick = {
                            mainAppModel.hiddenAppsManager.removeHiddenApp(app)
                            hiddenApps.value = mainAppModel.hiddenAppsManager.getHiddenApps()
                        }),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}



/**
 * Font options in settings
 *
 * @param context Needed to run some functions used within ThemeOptions
 * @param activity Needed to reload app after changing theme
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChooseFont(context: Context, activity: Activity, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.font))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Text(
            "Jost",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Jost")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Inter",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Inter")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Lexend",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Lexend")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Work Sans",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "Work Sans"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Poppins",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "Poppins"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Roboto",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "Roboto"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Open Sans",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "Open Sans"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Lora",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "Lora"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Outfit",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "Outfit"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "IBM Plex Sans",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "IBM Plex Sans"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "IBM Plex Serif",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context, context.resources.getString(R.string.Font), "IBM Plex Serif"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(128.dp))
    }
}

/**
 * Developer options in settings
 */
@Composable
fun DevOptions(context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, "Developer Options")

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))


    }
}


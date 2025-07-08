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

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalFocusManager

import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.essencelauncher.MainAppViewModel
import com.github.essencelauncher.R
import com.github.essencelauncher.ui.theme.EssenceTheme
import com.github.essencelauncher.ui.theme.offLightScheme
import com.github.essencelauncher.utils.AppUtils
import com.github.essencelauncher.utils.BiometricAuthenticationHelper


import com.github.essencelauncher.utils.AppUtils.resetHome
import com.github.essencelauncher.utils.InstalledApp
import com.github.essencelauncher.utils.getIntSetting
import com.github.essencelauncher.utils.setBooleanSetting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.github.essencelauncher.MainAppViewModel as MainAppModel


/**
 * Home Screen View Model
 */
class HomeScreenModel(application: Application, private val mainAppViewModel: MainAppViewModel) :
    AndroidViewModel(application) {
    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))

    @Suppress("MemberVisibilityCanBePrivate")
    var isCurrentAppHidden = mutableStateOf(false)
    var isCurrentAppFavorite = mutableStateOf(false)
    var isCurrentAppLocked = mutableStateOf(false)
    var showBottomSheet = mutableStateOf(false)
    var showPrivateSpaceSettings = mutableStateOf(false)

    var searchText = mutableStateOf("")
    var searchExpanded = mutableStateOf(false)

    val coroutineScope = viewModelScope
    val interactionSource = MutableInteractionSource()

    val installedApps = mutableStateListOf<InstalledApp>()
    val favoriteApps = mutableStateListOf<InstalledApp>()

    // Reactive state to track locked apps for immediate UI updates
    val lockedApps = mutableStateListOf<String>()

    val appsListScrollState = LazyListState()
    val pagerState = PagerState(1, 0f) { 3 }

    val currentSelectedPrivateApp =
        mutableStateOf(InstalledApp("", "", ComponentName("", ""))) //Only used for the bottom sheet
    var showPrivateBottomSheet = mutableStateOf(false)

    init {
        loadApps()
        reloadFavouriteApps()
        loadLockedApps()
    }

    fun loadApps() {
        coroutineScope.launch {
            installedApps.clear()
            installedApps.addAll(
                AppUtils.getAllInstalledApps(mainAppViewModel.getContext()).sortedBy {
                    it.displayName
                })
        }
    }

    fun reloadFavouriteApps() {
        coroutineScope.launch {
            val maxFavoriteApps = getIntSetting(
                mainAppViewModel.getContext(),
                mainAppViewModel.getContext().getString(R.string.MaxFavoriteApps),
                5
            )

            val newFavoriteApps = mainAppViewModel.favoriteAppsManager.getFavoriteApps()
                .take(maxFavoriteApps) // Limit to max favorite apps setting
                .mapNotNull { packageName ->
                    installedApps.find { it.packageName == packageName }
                }

            favoriteApps.apply {
                clear()
                addAll(newFavoriteApps)
            }
        }
    }

    fun updateSelectedApp(app: InstalledApp) {
        currentSelectedApp.value = app
        isCurrentAppFavorite.value = favoriteApps.contains(app)
        isCurrentAppHidden.value = mainAppViewModel.hiddenAppsManager.isAppHidden(app.packageName)
        isCurrentAppLocked.value = mainAppViewModel.lockedAppsManager.isAppLocked(app.packageName)
    }

    fun loadLockedApps() {
        coroutineScope.launch {
            val currentLockedApps = mainAppViewModel.lockedAppsManager.getLockedApps()
            lockedApps.clear()
            lockedApps.addAll(currentLockedApps)
        }
    }

    /**
     * Update locked apps state for immediate UI updates
     */
    fun updateLockedApps() {
        loadLockedApps()
    }
}

class HomeScreenModelFactory(
    private val application: Application,
    private val mainAppViewModel: MainAppViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeScreenModel(application, mainAppViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 *  Main composable for home screen:
 *  contains a pager with all the pages inside of it, contains bottom sheet
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun HomeScreenPageManager(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    onOpenSettings: () -> Unit,
    activity: FragmentActivity
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Add effect to hide keyboard on page change
    LaunchedEffect(homeScreenModel.pagerState.currentPage) {
        if (homeScreenModel.pagerState.currentPage != 2) {
            focusManager.clearFocus()
            keyboardController?.hide()
            homeScreenModel.searchText.value = ""
            homeScreenModel.searchExpanded.value = false
        }
    }

    // Track if user has interacted (swiped) - hide first time help on first swipe
    LaunchedEffect(homeScreenModel.pagerState.isScrollInProgress) {
        if (homeScreenModel.pagerState.isScrollInProgress) {
            // Hide first time help when user starts swiping
            setBooleanSetting(
                mainAppModel.getContext(),
                mainAppModel.getContext().resources.getString(R.string.FirstTimeAppDrawHelp),
                false
            )
        }
    }

    // Home Screen Pages
    HorizontalPager(
        state = homeScreenModel.pagerState,
        Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {}, onLongClickLabel = {}.toString(),
                onLongClick = {
                    // Hide first time help when user holds the screen
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.FirstTimeAppDrawHelp),
                        false
                    )
                    onOpenSettings()
                },
                indication = null, interactionSource = homeScreenModel.interactionSource
            )
    ) { page ->


        when (page) {
            0 -> WidgetsDashboard(
                context = mainAppModel.getContext(),
                mainAppModel = mainAppModel
            )

            1 -> HomeScreen(
                mainAppModel = mainAppModel,
                homeScreenModel = homeScreenModel,
                onOpenSettings = onOpenSettings,
                activity = activity
            )

            2 -> AppsList(
                mainAppModel = mainAppModel,
                homeScreenModel = homeScreenModel,
                activity = activity
            )
        }
    }

    //Bottom Sheet
    if (homeScreenModel.showBottomSheet.value) {
        val actions = listOf(
            AppAction(
                label = stringResource(id = R.string.uninstall),
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_DELETE,
                        "package:${homeScreenModel.currentSelectedApp.value.packageName}".toUri()
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mainAppModel.getContext().startActivity(intent)
                }
            ),
            AppAction(
                label = stringResource(if (homeScreenModel.isCurrentAppFavorite.value) R.string.rem_from_fav else R.string.add_to_fav),
                onClick = {
                    if (homeScreenModel.isCurrentAppFavorite.value) {
                        mainAppModel.favoriteAppsManager.removeFavoriteApp(
                            homeScreenModel.currentSelectedApp.value.packageName
                        )
                        homeScreenModel.isCurrentAppFavorite.value = false
                        homeScreenModel.showBottomSheet.value = false
                    } else {
                        val added = mainAppModel.favoriteAppsManager.addFavoriteApp(
                            homeScreenModel.currentSelectedApp.value.packageName
                        )
                        if (added) {
                            homeScreenModel.isCurrentAppFavorite.value = true
                            homeScreenModel.showBottomSheet.value = false
                            homeScreenModel.coroutineScope.launch {
                                homeScreenModel.pagerState.scrollToPage(1)
                            }
                        } else {
                            // Limit reached - could show a toast or just close the sheet
                            homeScreenModel.showBottomSheet.value = false
                        }
                    }
                    homeScreenModel.reloadFavouriteApps()
                }
            ),
            AppAction(
                label = stringResource(R.string.hide),
                onClick = {
                    mainAppModel.hiddenAppsManager.addHiddenApp(homeScreenModel.currentSelectedApp.value.packageName)
                    homeScreenModel.showBottomSheet.value = false
                    homeScreenModel.installedApps.remove(homeScreenModel.currentSelectedApp.value)
                    resetHome(homeScreenModel, false)
                }
            ),
            AppAction(
                label = stringResource(if (homeScreenModel.isCurrentAppLocked.value) R.string.unlock_app else R.string.lock_app),
                onClick = {
                    if (homeScreenModel.isCurrentAppLocked.value) {
                        // Unlock app - requires authentication
                        val biometricHelper = BiometricAuthenticationHelper(activity)
                        biometricHelper.authenticateForUnlock(
                            homeScreenModel.currentSelectedApp.value.displayName,
                            object : BiometricAuthenticationHelper.AuthenticationCallback {
                                override fun onAuthenticationSucceeded() {
                                    mainAppModel.lockedAppsManager.removeLockedApp(
                                        homeScreenModel.currentSelectedApp.value.packageName
                                    )
                                    homeScreenModel.isCurrentAppLocked.value = false
                                    homeScreenModel.showBottomSheet.value = false
                                    homeScreenModel.updateLockedApps()
                                }

                                override fun onAuthenticationFailed() {
                                    // Authentication failed, keep the sheet open
                                }

                                override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                                    // Authentication error, keep the sheet open
                                }
                            }
                        )
                    } else {
                        // Lock app - no authentication needed
                        mainAppModel.lockedAppsManager.addLockedApp(
                            homeScreenModel.currentSelectedApp.value.packageName
                        )
                        homeScreenModel.isCurrentAppLocked.value = true
                        homeScreenModel.showBottomSheet.value = false
                        homeScreenModel.updateLockedApps()
                    }
                }
            ),
            AppAction(
                label = stringResource(id = R.string.app_info),
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data =
                            "package:${homeScreenModel.currentSelectedApp.value.packageName}".toUri()
                    }.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    mainAppModel.getContext().startActivity(intent)
                    resetHome(homeScreenModel, false)
                }
            )
        )




        HomeScreenBottomSheet(
            title = homeScreenModel.currentSelectedApp.value.displayName,
            actions = actions,
            onDismissRequest = { homeScreenModel.showBottomSheet.value = false },
            sheetState = rememberModalBottomSheetState()
        )
    }


}

/**
 * An item displayed on the HomeScreen or Apps list
 *
 * If [showScreenTime] is enabled and [screenTime] is not null the screen time is written next to the app name.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenItem(
    modifier: Modifier = Modifier,
    appName: String,
    screenTime: Long? = null,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    showScreenTime: Boolean = false,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    isLocked: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = when(alignment){
            Alignment.Start -> Arrangement.Start
            Alignment.CenterHorizontally -> Arrangement.Center
            Alignment.End -> Arrangement.End
            else -> Arrangement.Center
        },
        modifier = modifier.combinedClickable(
            onClick = onAppClick,
            onLongClick = onAppLongClick
        ).fillMaxWidth()
    ) {
        // App name text with click and long click handlers
        Text(
            appName,
            modifier = Modifier.padding(vertical = 7.dp), // Reduced from 15.dp to 8.dp
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )

        // Lock icon if app is locked
        if (isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun HomeScreeItemPrev(){
    EssenceTheme(remember{mutableStateOf(offLightScheme)}){
        HomeScreenItem(
            modifier = Modifier,
            appName = "App Name",
            screenTime = 1000,
            onAppClick = {},
            onAppLongClick = {},
            showScreenTime = false
        )
    }
}

/**
 * Action that can be shown in the bottom sheet
 * */
data class AppAction(
    val label: String,
    val onClick: () -> Unit
)

/**
 * Bottom Sheet home screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenBottomSheet(
    title: String,
    actions: List<AppAction>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
            // Header
            Row {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "App Options",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 15.dp))

            // Actions
            Column(Modifier.padding(start = 47.dp)) {
                actions.forEach { action ->
                    Text(
                        text = action.label,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .combinedClickable(onClick = action.onClick),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
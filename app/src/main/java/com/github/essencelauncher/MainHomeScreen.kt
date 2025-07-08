package com.github.essencelauncher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import com.github.essencelauncher.ui.theme.AppTheme
import com.github.essencelauncher.ui.theme.EssenceTheme
import com.github.essencelauncher.ui.theme.darkScheme
import com.github.essencelauncher.ui.theme.offLightScheme
import com.github.essencelauncher.ui.views.HomeScreenModel
import com.github.essencelauncher.ui.views.HomeScreenModelFactory
import com.github.essencelauncher.ui.views.HomeScreenPageManager

import com.github.essencelauncher.ui.views.Settings
import com.github.essencelauncher.utils.AppUtils

import com.github.essencelauncher.utils.InstalledApp
import com.github.essencelauncher.utils.PrivateSpaceStateReceiver
import com.github.essencelauncher.utils.getBooleanSetting
import com.github.essencelauncher.utils.getIntSetting

import com.github.essencelauncher.utils.managers.FavoriteAppsManager
import com.github.essencelauncher.utils.managers.HiddenAppsManager
import com.github.essencelauncher.utils.managers.ItemAlignmentManager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainAppViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext: Context = application.applicationContext // The app context

    fun getContext(): Context = appContext // Returns the context

    var appTheme: MutableState<ColorScheme> = mutableStateOf(darkScheme) // App material theme

    // Managers

    val favoriteAppsManager: FavoriteAppsManager =
        FavoriteAppsManager(application) // Favorite apps manager

    val hiddenAppsManager: HiddenAppsManager = HiddenAppsManager(application) // Hidden apps manager

    val itemAlignmentManager: ItemAlignmentManager = ItemAlignmentManager(application) // Item alignment manager



    // Other stuff



    val isPrivateSpaceUnlocked: MutableState<Boolean> =
        mutableStateOf(false) // If the private space is unlocked, set by a registered receiver when the private space is closed or opened

    val shouldGoHomeOnResume: MutableState<Boolean> =
        mutableStateOf(false) // This is to check whether to go back to the first page of the home screen the next time onResume is called, It is only ever used once in AllApps when you come back from signing into private space


}

class MainHomeScreen : ComponentActivity() {
    private lateinit var privateSpaceReceiver: PrivateSpaceStateReceiver
    private lateinit var packageChangeReceiver: BroadcastReceiver

    private val homeScreenModel by viewModels<HomeScreenModel> {
        HomeScreenModelFactory(application, viewModel)
    }
    private val viewModel: MainAppViewModel by viewModels()

    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }

    /**
     * Main Entry point
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure full screen immediately
        enableEdgeToEdge()
        configureFullScreenMode()

        // Set up the application content immediately for instant display
        setContent { SetUpContent() }





        //Private space receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            privateSpaceReceiver = PrivateSpaceStateReceiver { isUnlocked ->
                viewModel.isPrivateSpaceUnlocked.value = isUnlocked
            }
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            }
            registerReceiver(privateSpaceReceiver, intentFilter)
        }

        // Package change receiver
        packageChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED,
                    Intent.ACTION_PACKAGE_REMOVED,
                    Intent.ACTION_PACKAGE_REPLACED -> {
                        Log.i("INFO", "Package changed: ${intent.action}")
                        lifecycleScope.launch(Dispatchers.Default) {
                            homeScreenModel.loadApps()
                            homeScreenModel.reloadFavouriteApps()
                        }
                    }
                }
            }
        }
        val packageFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageChangeReceiver, packageFilter)


    }

    override fun onResume() {
        super.onResume()

        // Refresh status bar appearance in case system theme changed
        configureStatusBarAppearance()

        // Reset home
        try {
            AppUtils.resetHome(homeScreenModel, viewModel.shouldGoHomeOnResume.value)
            viewModel.shouldGoHomeOnResume.value = false
        } catch (ex: Exception) {
            Log.e("ERROR", ex.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the receivers
        if (::privateSpaceReceiver.isInitialized) {
            unregisterReceiver(privateSpaceReceiver)
        }
        if (::packageChangeReceiver.isInitialized) {
            unregisterReceiver(packageChangeReceiver)
        }
    }

    /**
     * Puts the app into full screen or shows status bar based on settings
     */
    @Suppress("DEPRECATION")
    private fun configureFullScreenMode() {
        val showStatusBar = getBooleanSetting(this, getString(R.string.ShowStatusBar), true)

        // Configure status bar appearance based on current theme
        configureStatusBarAppearance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                if (showStatusBar) {
                    // Show status bar but hide navigation bar
                    hide(WindowInsets.Type.navigationBars())
                    show(WindowInsets.Type.statusBars())
                } else {
                    // Hide both status bar and navigation bar
                    hide(WindowInsets.Type.systemBars())
                }
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            if (showStatusBar) {
                // Show status bar but hide navigation bar
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                // Hide both status bar and navigation bar (original behavior)
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }

    /**
     * Configures status bar appearance (color and icon style) based on current theme
     */
    @Suppress("DEPRECATION")
    private fun configureStatusBarAppearance() {
        // Determine if we should use light or dark theme
        var settingToChange = getString(R.string.Theme)

        if (getBooleanSetting(this, getString(R.string.autoThemeSwitch), true)) {
            settingToChange = if (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                getString(R.string.dTheme)
            } else {
                getString(R.string.lTheme)
            }
        }

        val themeId = getIntSetting(this, settingToChange, 0)
        val isLightTheme = isLightTheme(themeId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                if (isLightTheme) {
                    // Light theme: dark status bar icons on light background
                    setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                    window.statusBarColor = android.graphics.Color.parseColor("#F5F5F5")
                } else {
                    // Dark theme: light status bar icons on dark background
                    setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                    window.statusBarColor = android.graphics.Color.parseColor("#1C1C1C")
                }
            }
        } else {
            if (isLightTheme) {
                // Light theme: dark status bar icons
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor = android.graphics.Color.parseColor("#F5F5F5")
            } else {
                // Dark theme: light status bar icons
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                window.statusBarColor = android.graphics.Color.parseColor("#1C1C1C")
            }
        }
    }

    /**
     * Determines if a theme ID represents a light theme
     */
    private fun isLightTheme(themeId: Int): Boolean {
        return when (themeId) {
            1, 3, 5, 7, 9, 11 -> true  // Light themes: LIGHT, LIGHT_RED, LIGHT_GREEN, LIGHT_BLUE, LIGHT_YELLOW, OFF_LIGHT
            else -> false              // Dark themes: DARK, PITCH_DARK, DARK_RED, DARK_GREEN, DARK_BLUE, DARK_YELLOW
        }
    }

    /**
     * Refreshes the status bar visibility and appearance based on current settings
     */
    fun refreshStatusBarVisibility() {
        configureFullScreenMode()
    }

    /**
     * The main parent composable for the app
     * Contains EssenceTheme & SetupNavHost
     */
    @Composable
    private fun SetUpContent() {
        var settingToChange = stringResource(R.string.Theme)

        if (getBooleanSetting(
                this@MainHomeScreen,
                stringResource(R.string.autoThemeSwitch),
                true
            )
        ) {
            settingToChange = if (isSystemInDarkTheme()) {
                stringResource(R.string.dTheme)
            } else {
                stringResource(R.string.lTheme)
            }
        }

        // Set theme immediately
        val colorScheme = AppTheme.fromId(getIntSetting(this@MainHomeScreen, settingToChange, 0)).scheme
        viewModel.appTheme = remember { mutableStateOf(colorScheme) }

        EssenceTheme(viewModel.appTheme) {
            SetupNavHost("home") // Always start at home, no delays
        }
    }



    /**
     * Sets up main navigation host for the app
     *
     * @param startDestination Where to start
     */
    @Composable
    private fun SetupNavHost(startDestination: String) {
        val navController = rememberNavController()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController, startDestination = startDestination) {
                composable(
                    "home",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) {
                    HomeScreenPageManager(
                        viewModel,
                        homeScreenModel
                    ) { navController.navigate("settings") }
                }
                composable(
                    "settings",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) {
                    Settings(
                        viewModel,
                        {
                            navController.navigate("home") {
                                popUpTo("settings") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        this@MainHomeScreen,
                    )
                }

            }
        }
    }
}
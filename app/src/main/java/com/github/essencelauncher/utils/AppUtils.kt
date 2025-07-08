package com.github.essencelauncher.utils

import android.app.ActivityOptions
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Rect
import android.os.Process.myUserHandle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.compose.runtime.MutableState

import androidx.core.graphics.createBitmap

import com.github.essencelauncher.R
import com.github.essencelauncher.ui.views.HomeScreenModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor
import com.github.essencelauncher.MainAppViewModel as MainAppModel



/**
 * Data class representing an app
 */
data class InstalledApp(
    var displayName: String,
    var packageName: String,
    var componentName: ComponentName
)

/**
 * Set of functions used throughout Essence Launcher app
 *
 * @author George Clensy
 */
object AppUtils{
    /**
     * Function to open app.
     *
     * @param app The app info being opened
     * @param mainAppModel Main view model, needed for package manager, context
     * @param homeScreenModel Home screen model for updating selected app
     *
     * @author George Clensy
     */
    fun openApp(
        app: InstalledApp,
        mainAppModel: MainAppModel,
        homeScreenModel: HomeScreenModel
    ) {
        val launcherApps = mainAppModel.getContext()
            .getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val options = ActivityOptions.makeBasic()

        launcherApps.startMainActivity(
            app.componentName,
            myUserHandle(),
            Rect(),
            options.toBundle()
        )

        mainAppModel.shouldGoHomeOnResume.value = true
        homeScreenModel.updateSelectedApp(app)
    }

    fun fuzzyMatch(text: String, pattern: String): Boolean {
        // Case-insensitive contains check (original behavior)
        if (text.contains(pattern, ignoreCase = true)) {
            return true
        }

        val lowerText = text.lowercase()
        val lowerPattern = pattern.lowercase()

        // Check for initials match (e.g., "gm" matches "Google Maps")
        if (pattern.length >= 2) {
            val words = lowerText.split(" ")
            if (words.size > 1) {
                val initials = words.joinToString("") { it.firstOrNull()?.toString() ?: "" }
                if (initials.contains(lowerPattern)) {
                    return true
                }
            }
        }

        // Check for character sequence match with gaps
        var textIndex = 0
        var patternIndex = 0
        while (textIndex < lowerText.length && patternIndex < lowerPattern.length) {
            if (lowerText[textIndex] == lowerPattern[patternIndex]) {
                patternIndex++
            }
            textIndex++
        }

        // If we matched all characters in pattern, it's a fuzzy match
        return patternIndex == lowerPattern.length
    }

    /**
     * Returns a list of all installed apps on the device
     *
     * @param context Context
     *
     * @return InstalledApp list with all installed apps
     */
    fun getAllInstalledApps(context: Context): List<InstalledApp> {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            ?: return emptyList()

        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val launchableActivities = packageManager.queryIntentActivities(mainIntent, 0).associate {
            it.activityInfo.packageName to ComponentName(
                it.activityInfo.packageName,
                it.activityInfo.name
            )
        }

        return launcherApps.getActivityList(null, myUserHandle())
            .filter { launchableActivities.containsKey(it.applicationInfo.packageName) }
            .map {
                val packageName = it.applicationInfo.packageName
                InstalledApp(
                    displayName = it.label?.toString() ?: "Unknown App",
                    packageName = packageName,
                    componentName = launchableActivities[packageName] ?: it.componentName
                )
            }
    }



    /**
     *  Cache to store package name to app name mappings
     */
    private val appNameCache = mutableMapOf<String, String>()

    /**
     * Returns the app name from its package
     *
     * @param context Context is required
     * @param packageName Name of the package that's app name will be returned
     *
     * @return String app name
     */
    fun getAppNameFromPackageName(context: Context, packageName: String): String {
        // Check cache first for instant return
        appNameCache[packageName]?.let { return it }

        // If not in cache, perform the operation directly but still cache the result
        try {
            val packageManager: PackageManager = context.packageManager
            val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()

            // Cache the result for future use
            appNameCache[packageName] = appName

            return appName
        } catch (_: PackageManager.NameNotFoundException) {
            return "null"
        }
    }

    /**
     * Returns the current time as a string
     *
     * @param context Context to access settings for time format preference
     * @return String the time with the format HH:mm (24-hour) or h:mm a (12-hour)
     */
    fun getCurrentTime(context: Context? = null): String {
        val now = LocalTime.now()

        // Check if 24-hour format is enabled (default to false - 12-hour format is default)
        val use24Hour = context?.let {
            getBooleanSetting(it, it.getString(R.string.Use24HourFormat), false)
        } ?: false

        val formatter = if (use24Hour) {
            DateTimeFormatter.ofPattern("HH:mm") // 24-hour format
        } else {
            DateTimeFormatter.ofPattern("h:mm a") // 12-hour format with AM/PM
        }

        return now.format(formatter)
    }

    /**
     * Returns the current date as a string
     *
     * @return String the date with the format "EEE, MMM d" (e.g., "Mon, Jan 15")
     */
    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    /**
     * Loads text from a file in Assets
     *
     * @param context Context
     * @param fileName Name of the file text will be loaded from
     *
     * @return Returns a String? with the text from the file
     */
    fun loadTextFromAssets(context: Context, fileName: String): String? {
        var inputStream: InputStream? = null
        var fileContent: String? = null
        try {
            inputStream = context.assets.open(fileName)
            fileContent = inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return fileContent
    }

    /**
     * Finds out if Essence Launcher is the default launcher
     *
     * @return Boolean which will be true if it is the default launcher
     */
    fun isDefaultLauncher(context: Context): Boolean {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    /**
     * Reset home screen for when app is closed
     */
    fun resetHome(homeScreenModel: HomeScreenModel, shouldGoToFirstPage: Boolean? = true) {
        homeScreenModel.coroutineScope.launch {
            if (shouldGoToFirstPage == true) {
                homeScreenModel.pagerState.scrollToPage(1)
                homeScreenModel.appsListScrollState.scrollToItem(0)
            }
            homeScreenModel.searchExpanded.value = false
            homeScreenModel.searchText.value = ""
            homeScreenModel.showBottomSheet.value = false
            homeScreenModel.reloadFavouriteApps()
        }
    }

    /**
     * Returns the date yesterday as a string
     *
     * @return String formatted yyyy-MM-dd
     */
    fun getYesterday(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return yesterdayDate
    }







    /**
     * Set a solid color as the home screen wallpaper.
     *
     * @param context The context of the application or activity.
     * @param color The color to set as the wallpaper.
     */
    @Suppress("unused")
    fun setSolidColorWallpaperHomeScreen(context: Context, color: ComposeColor) {
        val wallpaperManager = WallpaperManager.getInstance(context)

        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val bitmap = createBitmap(width, height)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint().apply {
            this.color = color.toAndroidColor()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
    }

    /**
     * Convert a Compose Color to an Android Color.
     *
     * @return The Android Color as an integer.
     */
    fun ComposeColor.toAndroidColor(): Int {
        return AndroidColor.argb(
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        )
    }
}

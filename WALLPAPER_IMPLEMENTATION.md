# Android Launcher Wallpaper Implementation

This document explains how the wallpaper display functionality has been implemented in your Android launcher app.

## Overview

The implementation allows your launcher to display the user's current wallpaper as the background, just like the default Android home screen. It uses the system's ability to display the wallpaper behind the app UI without manually fetching or drawing the wallpaper.

## Key Components

### 1. WallpaperManager.kt
A utility class that manages wallpaper display and background overlay functionality.

**Key Features:**
- Makes views transparent by default to show system wallpaper
- Applies optional semi-transparent color overlay with adjustable opacity
- Uses reversed opacity calculation (0% = opaque, 100% = transparent) as per user preference
- Default transparency is 20%

**Main Methods:**
- `applyWallpaperBackground(context, view)` - Applies wallpaper background to any view
- `getHexForOpacity(opacity)` - Converts opacity percentage to hex color with alpha
- `setWallpaperOpacity(context, opacity)` - Saves opacity setting
- `getWallpaperOpacity(context)` - Retrieves current opacity setting

### 2. Theme Configuration

**themes.xml and themes-night.xml:**
```xml
<!-- Enable wallpaper visibility by making window background transparent -->
<item name="android:windowBackground">@android:color/transparent</item>
<item name="android:windowShowWallpaper">true</item>
<item name="android:windowTranslucentStatus">false</item>
<item name="android:windowTranslucentNavigation">false</item>
```

**Key Attributes:**
- `android:windowBackground="@android:color/transparent"` - Makes window background transparent
- `android:windowShowWallpaper="true"` - Tells Android to show wallpaper behind the window
- `android:windowTranslucentStatus/Navigation="false"` - Keeps status/nav bars opaque

### 3. Layout Updates

All main layout files have been updated:
- `activity_main.xml` - Main container made transparent
- `fragment_home.xml` - Home fragment root made transparent with ID
- `fragment_left.xml` - Left fragment root made transparent with ID
- `fragment_right.xml` - Right fragment root made transparent with ID
- `fragment_app_drawer.xml` - App drawer root made transparent with ID
- `fragment_hidden_apps.xml` - Hidden apps root made transparent with ID

### 4. Fragment Updates

All fragments now apply wallpaper background in their `onCreateView` methods:

```kotlin
private fun applyWallpaperBackground(view: View) {
    val rootView = view.findViewById<ViewType>(R.id.rootViewId)
    WallpaperManager.applyWallpaperBackground(requireContext(), rootView)
}
```

### 5. Settings Integration

Added wallpaper opacity control in SettingsActivity:
- Slider control for opacity (0-100%)
- Real-time preview of opacity changes
- Saves settings to SharedPreferences
- Shows current opacity value

## How It Works

### Wallpaper Display Process

1. **Window Configuration**: Theme sets `windowShowWallpaper=true` and `windowBackground=transparent`
2. **Android System**: Automatically displays the current wallpaper behind the transparent window
3. **Overlay Application**: WallpaperManager applies optional semi-transparent overlay based on user settings
4. **Dynamic Updates**: Settings changes are applied immediately and persist across app restarts

### Opacity Calculation

The implementation uses a reversed opacity system as per user preference:
- **0% opacity** = Fully opaque overlay (wallpaper completely hidden)
- **50% opacity** = Semi-transparent overlay (wallpaper partially visible)
- **100% opacity** = Fully transparent overlay (wallpaper fully visible)

### Color Overlay Formula

```kotlin
fun getHexForOpacity(opacity: Int): String {
    val reversedOpacity = 100 - opacity
    val alpha = (reversedOpacity * 255 / 100).coerceIn(0, 255)
    val alphaHex = String.format("%02X", alpha)
    return "#${alphaHex}000000" // Black overlay with calculated alpha
}
```

## Android Concepts Involved

### 1. Window Attributes
- `WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER` (set via theme)
- Transparent window backgrounds
- Window flags for wallpaper display

### 2. View Background Management
- `View.setBackgroundColor()` for applying overlay colors
- `Color.parseColor()` for hex color parsing
- `Color.argb()` for ARGB color creation

### 3. SharedPreferences
- Persistent storage for user settings
- Real-time settings updates
- Cross-fragment data sharing

### 4. Material Design 3
- Maintains MD3 theming compatibility
- Supports both light and dark themes
- Preserves system theme switching

## Usage Examples

### Basic Wallpaper Application
```kotlin
// Apply wallpaper background to any view
WallpaperManager.applyWallpaperBackground(context, myView)
```

### Custom Opacity Setting
```kotlin
// Set 30% opacity (70% wallpaper visibility)
WallpaperManager.setWallpaperOpacity(context, 30)

// Get current opacity
val currentOpacity = WallpaperManager.getWallpaperOpacity(context)
```

### Manual Color Overlay
```kotlin
// Get hex color for 25% opacity
val overlayColor = WallpaperManager.getHexForOpacity(25)
view.setBackgroundColor(Color.parseColor(overlayColor))
```

## Benefits

1. **No Manual Wallpaper Fetching**: Uses Android's built-in wallpaper display system
2. **Performance Efficient**: No bitmap loading or memory management required
3. **System Integration**: Automatically updates when user changes wallpaper
4. **Customizable**: User-controlled opacity for different visibility preferences
5. **Theme Compatible**: Works with both light and dark themes
6. **Consistent**: Same wallpaper display across all launcher screens

## Testing

To test the implementation:
1. Set a custom wallpaper in Android settings
2. Open the launcher - wallpaper should be visible behind UI elements
3. Go to Settings > Wallpaper Opacity
4. Adjust opacity slider and apply changes
5. Verify wallpaper visibility changes according to opacity setting
6. Test across all screens (home, left, right, app drawer, hidden apps)

## Troubleshooting

If wallpaper is not visible:
1. Check that theme includes `windowShowWallpaper=true`
2. Verify window background is transparent
3. Ensure view backgrounds are transparent or use WallpaperManager
4. Check that opacity setting is not 0% (fully opaque)
5. Restart launcher after settings changes

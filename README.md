# Essence Launcher

A modern Android launcher with a focus on simplicity and customization.

## Features

- Clean, minimal interface
- Customizable themes and layouts
- Widget support
- App search and organization
- Private space integration (Android 15+)
- Biometric authentication for locked apps

## GestureManager Pager Implementation

This launcher uses a custom pager implementation powered by `GestureManager` instead of the standard `HorizontalPager`. This provides more control over gesture handling and page transitions.

### How It Works

The pager consists of three main pages:
- **Page 0**: Widgets Dashboard
- **Page 1**: Home Screen (favorite apps)
- **Page 2**: Apps List

### Gesture Navigation

- **Swipe Left**: Navigate to next page
- **Swipe Right**: Navigate to previous page
- **Long Press**: Open settings

### Implementation Details

#### 1. GestureManager Setup

```kotlin
val gestureManager = GestureManager(context, object : GestureAdapter() {
    override fun onSwipeLeft() {
        homeScreenModel.nextPage()
    }
    
    override fun onSwipeRight() {
        homeScreenModel.previousPage()
    }
})
```

#### 2. Page Management

The `HomeScreenModel` manages page state:

```kotlin
var currentPage by mutableIntStateOf(1) // Start at home page
val pageCount = 3

fun nextPage() {
    if (currentPage < pageCount - 1) {
        currentPage++
    }
}

fun previousPage() {
    if (currentPage > 0) {
        currentPage--
    }
}

fun goToPage(page: Int) {
    if (page in 0 until pageCount) {
        currentPage = page
    }
}
```

#### 3. Custom Pager Component

The `CustomPager` composable handles:
- Gesture detection via Android View
- Page content rendering
- Transition animations
- Page indicators

#### 4. Integration Flow

1. **Attach GestureManager** to the view container
2. **Handle swipe callbacks** to update current page index
3. **Redraw content** based on current page
4. **Manage state** for page transitions and animations

### Benefits of Custom Implementation

- **Full Control**: Complete control over gesture detection and response
- **Custom Animations**: Implement custom page transition animations
- **Performance**: Optimized for specific use cases
- **Flexibility**: Easy to extend with additional gesture types
- **Debugging**: Better visibility into gesture handling

### Usage Example

```kotlin
@Composable
fun MyCustomPager() {
    val homeScreenModel = remember { HomeScreenModel() }
    
    CustomPager(
        homeScreenModel = homeScreenModel,
        mainAppModel = mainAppModel,
        onOpenSettings = { /* handle settings */ },
        activity = activity
    )
}
```

### Configuration

The pager can be customized by modifying:
- Gesture thresholds in `GestureManager`
- Page transition animations
- Page indicator styling
- Navigation behavior

## Development

### Building

```bash
./gradlew assembleDebug
```

### Testing

The pager includes visual indicators for debugging:
- Page dots at the top show current page
- Swipe gestures are logged for debugging
- Page transitions are animated for smooth UX

## License

MIT License - see LICENSE file for details.

package com.github.essencelauncher.ui.views

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.essencelauncher.MainAppViewModel
import com.github.essencelauncher.R
import com.github.essencelauncher.utils.CustomWidgetPicker
import com.github.essencelauncher.utils.getStringSetting
import com.github.essencelauncher.utils.getWidgetHeight
import com.github.essencelauncher.utils.getWidgetOffset
import com.github.essencelauncher.utils.getWidgetWidth
import com.github.essencelauncher.utils.isWidgetConfigurable
import com.github.essencelauncher.utils.launchWidgetConfiguration
import com.github.essencelauncher.utils.setWidgetHeight
import com.github.essencelauncher.utils.setWidgetOffset
import com.github.essencelauncher.utils.setWidgetWidth

// Data class for individual widget configuration
data class WidgetConfig(
    val id: Int,
    val offsetX: Float = 0f,
    val width: Float = 150f,
    val height: Float = 125f,
    val createdAt: Long = System.currentTimeMillis() // Add timestamp for uniqueness
)

// Functions for managing multiple widgets
fun saveWidgetConfigs(context: Context, widgets: List<WidgetConfig>) {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    val editor = prefs.edit()

    // Clear only widget-related keys, not all preferences
    val oldCount = prefs.getInt("widget_count", 0)
    for (i in 0 until oldCount) {
        editor.remove("widget_${i}_id")
        editor.remove("widget_${i}_offset")
        editor.remove("widget_${i}_width")
        editor.remove("widget_${i}_height")
        editor.remove("widget_${i}_created")
    }

    // Save widget count
    editor.putInt("widget_count", widgets.size)

    // Save each widget configuration
    widgets.forEachIndexed { index, widget ->
        editor.putInt("widget_${index}_id", widget.id)
        editor.putFloat("widget_${index}_offset", widget.offsetX)
        editor.putFloat("widget_${index}_width", widget.width)
        editor.putFloat("widget_${index}_height", widget.height)
        editor.putLong("widget_${index}_created", widget.createdAt)
    }

    editor.apply()
}

fun getSavedWidgetConfigs(context: Context): List<WidgetConfig> {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    val count = prefs.getInt("widget_count", 0)

    return (0 until count).mapNotNull { index ->
        val id = prefs.getInt("widget_${index}_id", -1)
        if (id != -1) {
            WidgetConfig(
                id = id,
                offsetX = prefs.getFloat("widget_${index}_offset", 0f),
                width = prefs.getFloat("widget_${index}_width", 150f),
                height = prefs.getFloat("widget_${index}_height", 125f),
                createdAt = prefs.getLong("widget_${index}_created", System.currentTimeMillis())
            )
        } else null
    }
}

fun removeWidgetConfig(context: Context, widgetId: Int) {
    val currentWidgets = getSavedWidgetConfigs(context).filter { it.id != widgetId }
    saveWidgetConfigs(context, currentWidgets)
}

/**
 * Parent UI for WidgetsDashboard
 * Displays widgets in a dedicated dashboard screen with support for multiple widgets
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WidgetsDashboard(context: Context, mainAppModel: MainAppViewModel) {
    // State for managing widgets
    val widgetConfigs = remember { mutableStateListOf<WidgetConfig>() }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var showWidgetBottomSheet by remember { mutableStateOf(false) }
    var selectedWidgetConfig by remember { mutableStateOf<WidgetConfig?>(null) }

    // Widget management
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = remember { AppWidgetHost(context, 1) }

    // Load saved widgets on first composition
    LaunchedEffect(Unit) {
        widgetConfigs.clear()
        widgetConfigs.addAll(getSavedWidgetConfigs(context))
    }

    // Activity result launcher for binding widget permission
    val bindWidgetPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (newWidgetId != -1) {
                val newConfig = WidgetConfig(id = newWidgetId)
                widgetConfigs.add(newConfig)
                saveWidgetConfigs(context, widgetConfigs.toList())

                // Check if widget needs configuration
                if (isWidgetConfigurable(context, newWidgetId)) {
                    launchWidgetConfiguration(context, newWidgetId)
                }
            }
        }
    }

    // Widget picker dialog
    if (showWidgetPicker) {
        CustomWidgetPicker(
            onWidgetSelected = { widgetProviderInfo ->
                // Allocate widget ID
                val newWidgetId = appWidgetHost.allocateAppWidgetId()

                // Try to bind widget
                val allocated = appWidgetManager.bindAppWidgetIdIfAllowed(
                    newWidgetId,
                    widgetProviderInfo.provider
                )

                if (allocated) {
                    // Widget successfully bound
                    val newConfig = WidgetConfig(id = newWidgetId)
                    widgetConfigs.add(newConfig)
                    saveWidgetConfigs(context, widgetConfigs.toList())

                    // Check if widget needs configuration
                    if (isWidgetConfigurable(context, newWidgetId)) {
                        launchWidgetConfiguration(context, newWidgetId)
                    }
                } else {
                    // Request bind widget permission
                    val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newWidgetId)
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, widgetProviderInfo.provider)
                    }
                    bindWidgetPermissionLauncher.launch(bindIntent)
                }

                showWidgetPicker = false
            },
            onDismiss = { showWidgetPicker = false }
        )
    }

    // Widget configuration bottom sheet
    if (showWidgetBottomSheet && selectedWidgetConfig != null) {
        // Verify the selected widget still exists in our list
        val currentSelectedWidget = widgetConfigs.find { it.id == selectedWidgetConfig!!.id }
        if (currentSelectedWidget == null) {
            // Widget no longer exists, close the bottom sheet
            showWidgetBottomSheet = false
            selectedWidgetConfig = null
        } else {
        val sheetState = rememberModalBottomSheetState()
        var tempOffset by remember { mutableFloatStateOf(selectedWidgetConfig!!.offsetX) }
        var tempWidth by remember { mutableFloatStateOf(selectedWidgetConfig!!.width) }
        var tempHeight by remember { mutableFloatStateOf(selectedWidgetConfig!!.height) }

        ModalBottomSheet(
            onDismissRequest = { showWidgetBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Widget Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Remove Widget Button
                Button(
                    onClick = {
                        selectedWidgetConfig?.let { config ->
                            val widgetIdToRemove = config.id

                            // Remove from app widget host
                            appWidgetHost.deleteAppWidgetId(widgetIdToRemove)

                            // Create a new list without the widget to remove
                            val newList = widgetConfigs.toList().filter { widget ->
                                widget.id != widgetIdToRemove
                            }

                            if (newList.size < widgetConfigs.size) {
                                // Clear and rebuild the list
                                widgetConfigs.clear()
                                widgetConfigs.addAll(newList)

                                // Save updated list
                                saveWidgetConfigs(context, newList)
                            }
                        }
                        // Reset selected widget and close bottom sheet
                        selectedWidgetConfig = null
                        showWidgetBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remove Widget")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Offset Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Offset: ${tempOffset.toInt()}",
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { tempOffset = 0f },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Reset")
                    }
                }
                Slider(
                    value = tempOffset,
                    onValueChange = { tempOffset = it },
                    valueRange = -20f..20f,
                    steps = 20,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Width Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Width: ${tempWidth.toInt()}",
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { tempWidth = 250f },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Reset")
                    }
                }
                Slider(
                    value = tempWidth,
                    onValueChange = { tempWidth = it },
                    valueRange = 100f..400f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Height Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Height: ${tempHeight.toInt()}",
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { tempHeight = 125f },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Reset")
                    }
                }
                Slider(
                    value = tempHeight,
                    onValueChange = { tempHeight = it },
                    valueRange = 100f..400f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Apply Changes Button
                Button(
                    onClick = {
                        selectedWidgetConfig?.let { config ->
                            val updatedConfig = config.copy(
                                offsetX = tempOffset,
                                width = tempWidth,
                                height = tempHeight
                            )
                            val index = widgetConfigs.indexOfFirst { it.id == config.id }
                            if (index != -1) {
                                widgetConfigs[index] = updatedConfig
                                saveWidgetConfigs(context, widgetConfigs.toList())
                            }
                        }
                        showWidgetBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Changes")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        }
    }

    // Main UI
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(15.dp, 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(120.dp))

            // Title for the widgets dashboard
            Text(
                text = "Widgets",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
            )

            // Display widgets or empty state
            if (widgetConfigs.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No widgets added yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add your first widget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Display widgets using key to ensure proper Compose tracking
                widgetConfigs.forEach { config ->
                    key(config.id) { // Use widget ID as key for proper Compose tracking
                        WidgetItem(
                            context = context,
                            widgetConfig = config,
                            appWidgetHost = appWidgetHost,
                            onLongPress = {
                                selectedWidgetConfig = config
                                showWidgetBottomSheet = true
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(80.dp)) // Extra space for FAB
        }

        // Floating Action Button for adding widgets
        FloatingActionButton(
            onClick = { showWidgetPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Widget")
        }
    }
}

/**
 * Individual widget item with long press support
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetItem(
    context: Context,
    widgetConfig: WidgetConfig,
    appWidgetHost: AppWidgetHost,
    onLongPress: () -> Unit
) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    var appWidgetHostView by remember { mutableStateOf<AppWidgetHostView?>(null) }

    // Create widget view
    LaunchedEffect(widgetConfig.id) {
        try {
            if (widgetConfig.id != -1) {
                val widgetInfo = appWidgetManager.getAppWidgetInfo(widgetConfig.id)
                if (widgetInfo != null) {
                    appWidgetHostView = appWidgetHost.createView(context, widgetConfig.id, widgetInfo).apply {
                        setAppWidget(widgetConfig.id, widgetInfo)
                    }
                }
            }
        } catch (e: Exception) {
            // Widget might have been removed or is invalid
            appWidgetHostView = null
        }
    }

    // Calculate base alignment offset
    val baseOffset = when (getStringSetting(context, "HomeAlignment", "Center")) {
        "Left" -> -8
        "Right" -> 8
        else -> 0
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        appWidgetHostView?.let { hostView ->
            Card(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            ((baseOffset + widgetConfig.offsetX).dp)
                                .toPx()
                                .toInt(), 0
                        )
                    }
                    .size(
                        widgetConfig.width.dp,
                        widgetConfig.height.dp
                    )
                    .padding(0.dp, 7.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { hostView },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Transparent overlay for long press detection
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                onClick = { /* Allow normal widget clicks to pass through */ },
                                onLongClick = onLongPress,
                                indication = null, // No visual indication
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )
                }
            }
        } ?: run {
            // Fallback for invalid widgets
            Card(
                modifier = Modifier
                    .size(widgetConfig.width.dp, widgetConfig.height.dp)
                    .combinedClickable(
                        onClick = { },
                        onLongClick = onLongPress
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Widget unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

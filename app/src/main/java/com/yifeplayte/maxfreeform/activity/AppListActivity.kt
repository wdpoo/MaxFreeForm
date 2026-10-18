package com.yifeplayte.maxfreeform.activity

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.LruCache
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.yifeplayte.maxfreeform.App
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.ui.miuixBlurEffect
import com.yifeplayte.maxfreeform.activity.ui.rememberMiuixBlurBackdrop
import com.yifeplayte.maxfreeform.utils.SharedPreferences
import com.yifeplayte.maxfreeform.utils.reloadAllTargets
import io.github.ranlee1.jpinyin.PinyinFormat.WITHOUT_TONE
import io.github.ranlee1.jpinyin.PinyinHelper.convertToPinyinString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowListPopup

class AppListActivity : ComponentActivity() {
    companion object {
        const val EXTRA_KEY = "key"
        const val EXTRA_TITLE_RES = "title_res"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val key = intent.getStringExtra(EXTRA_KEY) ?: run {
            finish()
            return
        }
        val titleRes = intent.getIntExtra(EXTRA_TITLE_RES, R.string.app_name)
            .takeIf { it != 0 }
            ?: R.string.app_name
        enableEdgeToEdge()
        setContent {
            MiuixTheme(
                colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                AppListScreen(key, titleRes, onClose = { finish() })
            }
        }
    }
}

private data class AppEntry(
    val packageName: String,
    val label: String,
    val sortKey: String,
    val firstInstallTime: Long,
    val isSystem: Boolean,
    val isAvailable: Boolean,
)

private enum class AppOrderType {
    Label,
    PackageName,
    FirstInstallTime,
}

private const val UI_PREFERENCES = "app_list_ui"
private const val UI_KEY_ORDER_TYPE = "order_type"
private const val UI_KEY_ORDER_REVERSED = "order_reversed"
private const val UI_KEY_SELECTED_FIRST = "selected_first"
private const val UI_KEY_SHOW_SYSTEM_APPS = "show_system_apps"
private const val UI_KEY_SHOW_PACKAGE_NAME = "show_package_name"

@Composable
private fun AppListScreen(
    key: String,
    titleRes: Int,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val iconSize = with(LocalDensity.current) { 48.dp.roundToPx() }
    val scope = rememberCoroutineScope()
    val uiPreferences = remember(context) {
        context.getSharedPreferences(UI_PREFERENCES, Context.MODE_PRIVATE)
    }
    val iconCache = remember {
        object : LruCache<String, ImageBitmap>(6 * 1024 * 1024) {
            override fun sizeOf(key: String, value: ImageBitmap): Int =
                value.width * value.height * 4
        }
    }
    var query by rememberSaveable { mutableStateOf("") }
    var orderTypeName by rememberSaveable {
        mutableStateOf(
            uiPreferences.getString(UI_KEY_ORDER_TYPE, AppOrderType.Label.name)
                ?: AppOrderType.Label.name
        )
    }
    var orderReversed by rememberSaveable {
        mutableStateOf(uiPreferences.getBoolean(UI_KEY_ORDER_REVERSED, false))
    }
    var selectedFirst by rememberSaveable {
        mutableStateOf(uiPreferences.getBoolean(UI_KEY_SELECTED_FIRST, true))
    }
    var showSystemApps by rememberSaveable {
        mutableStateOf(uiPreferences.getBoolean(UI_KEY_SHOW_SYSTEM_APPS, false))
    }
    var showPackageName by rememberSaveable {
        mutableStateOf(uiPreferences.getBoolean(UI_KEY_SHOW_PACKAGE_NAME, false))
    }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var serviceReady by remember { mutableStateOf(App.mService != null) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    DisposableEffect(Unit) {
        val listener = object : App.ServiceStateListener {
            override fun onServiceStateChanged(service: io.github.libxposed.service.XposedService?) {
                serviceReady = service != null
            }
        }
        App.addServiceStateListener(listener, notifyImmediately = true)
        onDispose { App.removeServiceStateListener(listener) }
    }
    LaunchedEffect(serviceReady, key) {
        if (serviceReady) {
            selected = SharedPreferences.mSP?.getStringSet(key, emptySet()) ?: emptySet()
        }
    }
    val lazyListState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val showFloating by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 ||
                    lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val layoutDirection = LocalLayoutDirection.current
    val horizontalSafeInsets = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Horizontal)
        .asPaddingValues()
    val topBarBackdrop = rememberMiuixBlurBackdrop()

    LaunchedEffect(Unit) {
        loading = true
        loadFailed = false
        runCatching {
            withContext(Dispatchers.IO) {
                loadApps(context.packageManager, context.packageName)
            }
        }.onSuccess {
            apps = it
        }.onFailure {
            loadFailed = true
        }
        loading = false
    }

    val orderType = remember(orderTypeName) {
        runCatching { AppOrderType.valueOf(orderTypeName) }
            .getOrDefault(AppOrderType.Label)
    }
    val visibleApps = remember(
        apps,
        selected,
        query,
        showSystemApps,
        selectedFirst,
        orderType,
        orderReversed,
    ) {
        val entriesByPackage = LinkedHashMap<String, AppEntry>()
        apps.forEach { app -> entriesByPackage.putIfAbsent(app.packageName, app) }
        selected.forEach { packageName ->
            entriesByPackage.putIfAbsent(
                packageName,
                AppEntry(
                    packageName = packageName,
                    label = packageName,
                    sortKey = packageName.lowercase(),
                    firstInstallTime = 0L,
                    isSystem = false,
                    isAvailable = false,
                ),
            )
        }
        val filteredApps = entriesByPackage.values
            .asSequence()
            .filter { app -> showSystemApps || !app.isSystem }
            .filter { app ->
                query.isEmpty() ||
                        app.label.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
            }
            .toList()

        val comparators = mutableListOf<(AppEntry) -> Comparable<*>?>()
        if (selectedFirst) {
            comparators.add { app -> if (app.packageName in selected) 0 else 1 }
        }
        val property: (AppEntry, AppOrderType) -> Comparable<*> = { app, type ->
            when (type) {
                AppOrderType.Label -> app.sortKey
                AppOrderType.PackageName -> app.packageName
                AppOrderType.FirstInstallTime -> app.firstInstallTime
            }
        }
        comparators.add { app -> property(app, orderType) }
        AppOrderType.entries.filter { it != orderType }.forEach { fallbackType ->
            comparators.add { app -> property(app, fallbackType) }
        }
        var comparator = compareBy(*comparators.toTypedArray())
        if (orderReversed) {
            comparator = comparator.reversed()
        }
        filteredApps.sortedWith(comparator)
    }

    val persistSelection: (Set<String>) -> Unit = { next ->
        selected = next
        SharedPreferences.mSP?.edit()
            ?.putStringSet(key, HashSet(next))
            ?.apply()
        reloadAllTargets()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .miuixBlurEffect(topBarBackdrop)
                    .background(Color.Transparent),
            ) {
                TopAppBar(
                    color = Color.Transparent,
                    scrollBehavior = scrollBehavior,
                    title = stringResource(titleRes),
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = MiuixIcons.Regular.Back,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    },
                    actions = {
                        AppListTopAppBarActions(
                            orderReversed = orderReversed,
                            selectedFirst = selectedFirst,
                            showSystemApps = showSystemApps,
                            showPackageName = showPackageName,
                            onOrderReversedChanged = { enabled ->
                                orderReversed = enabled
                                uiPreferences.edit()
                                    .putBoolean(UI_KEY_ORDER_REVERSED, enabled)
                                    .apply()
                            },
                            onSelectedFirstChanged = { enabled ->
                                selectedFirst = enabled
                                uiPreferences.edit()
                                    .putBoolean(UI_KEY_SELECTED_FIRST, enabled)
                                    .apply()
                            },
                            onShowSystemAppsChanged = { enabled ->
                                showSystemApps = enabled
                                uiPreferences.edit()
                                    .putBoolean(UI_KEY_SHOW_SYSTEM_APPS, enabled)
                                    .apply()
                            },
                            onShowPackageNameChanged = { enabled ->
                                showPackageName = enabled
                                uiPreferences.edit()
                                    .putBoolean(UI_KEY_SHOW_PACKAGE_NAME, enabled)
                                    .apply()
                            },
                        )
                    },
                )
                Spacer(modifier = Modifier.size(6.dp))
                InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = {},
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp + horizontalSafeInsets.calculateStartPadding(
                                layoutDirection,
                            ),
                            end = 16.dp + horizontalSafeInsets.calculateEndPadding(
                                layoutDirection,
                            ),
                        )
                        .padding(bottom = 8.dp),
                    label = stringResource(R.string.search),
                )
                val orderOptions = remember {
                    listOf(
                        R.string.app_list_sort_by_label to AppOrderType.Label,
                        R.string.app_list_sort_by_package_name to AppOrderType.PackageName,
                        R.string.app_list_sort_by_install_time to AppOrderType.FirstInstallTime,
                    )
                }
                val dropdownItems = orderOptions.map { stringResource(it.first) }
                val selectedIndex = orderOptions.indexOfFirst { it.second == orderType }
                    .coerceAtLeast(0)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 6.dp + horizontalSafeInsets.calculateStartPadding(
                                layoutDirection,
                            ),
                            end = 6.dp + horizontalSafeInsets.calculateEndPadding(
                                layoutDirection,
                            ),
                        )
                        .padding(bottom = 6.dp),
                ) {
                    AppListDropdown(
                        items = dropdownItems,
                        selectedIndex = selectedIndex,
                        onSelectedIndexChange = { newIndex ->
                            val newOrderType = orderOptions[newIndex].second
                            orderTypeName = newOrderType.name
                            uiPreferences.edit()
                                .putString(UI_KEY_ORDER_TYPE, newOrderType.name)
                                .apply()
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFloating,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                FloatingActionButton(
                    modifier = Modifier.padding(end = 16.dp),
                    containerColor = MiuixTheme.colorScheme.surface,
                    onClick = {
                        scope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                ) {
                    Icon(
                        imageVector = ArrowUpIcon,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        },
    ) { paddingValues ->
        when {
            loading && apps.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        InfiniteProgressIndicator()
                        Text(
                            text = stringResource(R.string.app_list_loading),
                            style = MiuixTheme.textStyles.main,
                        )
                    }
                }
            }

            loadFailed -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.app_list_load_failed),
                        color = MiuixTheme.colorScheme.error,
                        style = MiuixTheme.textStyles.main,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues)
                        .layerBackdrop(topBarBackdrop)
                        .scrollEndHaptic()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        start = horizontalSafeInsets.calculateStartPadding(layoutDirection),
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        end = horizontalSafeInsets.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding(),
                    ),
                    overscrollEffect = null,
                ) {
                    if (visibleApps.isEmpty()) {
                        item(key = "empty") {
                            EmptyRow()
                        }
                    } else {
                        itemsIndexed(
                            items = visibleApps,
                            key = { _, app -> app.packageName },
                            contentType = { _, _ -> "app_item" },
                        ) { index, app ->
                            val cardRadius = CardDefaults.CornerRadius
                            val shape = when {
                                visibleApps.size == 1 -> RoundedCornerShape(cardRadius)
                                index == 0 -> RoundedCornerShape(
                                    topStart = cardRadius,
                                    topEnd = cardRadius,
                                    bottomStart = 0.dp,
                                    bottomEnd = 0.dp,
                                )

                                index == visibleApps.lastIndex -> RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 0.dp,
                                    bottomStart = cardRadius,
                                    bottomEnd = cardRadius,
                                )

                                else -> RoundedCornerShape(0.dp)
                            }
                            val checked = app.packageName in selected
                            AppListItem(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .zIndex(-index.toFloat())
                                    .animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = spring(
                                            stiffness = Spring.StiffnessMediumLow,
                                            visibilityThreshold = IntOffset.VisibilityThreshold,
                                        ),
                                    ),
                                app = app,
                                iconCache = iconCache,
                                iconSize = iconSize,
                                checked = checked,
                                shape = shape,
                                showPackageName = showPackageName,
                                enabled = serviceReady,
                                onToggle = { isChecked ->
                                    persistSelection(
                                        if (isChecked) {
                                            selected + app.packageName
                                        } else {
                                            selected - app.packageName
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppListTopAppBarActions(
    orderReversed: Boolean,
    selectedFirst: Boolean,
    showSystemApps: Boolean,
    showPackageName: Boolean,
    onOrderReversedChanged: (Boolean) -> Unit,
    onSelectedFirstChanged: (Boolean) -> Unit,
    onShowSystemAppsChanged: (Boolean) -> Unit,
    onShowPackageNameChanged: (Boolean) -> Unit,
) {
    val showMenu = remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val menuOptions = remember(
        orderReversed,
        selectedFirst,
        showSystemApps,
        showPackageName,
    ) {
        listOf(
            R.string.app_list_reverse_order to orderReversed,
            R.string.app_list_selected_first to selectedFirst,
            R.string.show_system_applications to showSystemApps,
            R.string.app_list_show_package_name to showPackageName,
        )
    }

    WindowListPopup(
        show = showMenu.value,
        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
        alignment = PopupPositionProvider.Align.TopEnd,
        onDismissRequest = { showMenu.value = false },
    ) {
        ListPopupColumn {
            menuOptions.forEachIndexed { index, (labelResId, isSelected) ->
                DropdownImpl(
                    text = stringResource(labelResId),
                    optionSize = menuOptions.size,
                    isSelected = isSelected,
                    onSelectedIndexChange = { selectedIndex ->
                        when (selectedIndex) {
                            0 -> onOrderReversedChanged(!orderReversed)
                            1 -> onSelectedFirstChanged(!selectedFirst)
                            2 -> onShowSystemAppsChanged(!showSystemApps)
                            3 -> onShowPackageNameChanged(!showPackageName)
                        }
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    },
                    index = index,
                )
            }
        }
    }

    IconButton(
        onClick = { showMenu.value = true },
        holdDownState = showMenu.value,
    ) {
        Icon(
            imageVector = MiuixIcons.Regular.More,
            tint = MiuixTheme.colorScheme.onBackground,
            contentDescription = stringResource(R.string.more_options),
        )
    }
}

@Composable
private fun AppListDropdown(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expanded = remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        expanded.value = !expanded.value
                        if (expanded.value) {
                            hapticFeedback.performHapticFeedback(
                                HapticFeedbackType.ContextClick,
                            )
                        }
                    },
                )
                .padding(horizontal = 28.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = items[selectedIndex],
                fontSize = MiuixTheme.textStyles.subtitle.fontSize,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onBackgroundVariant,
            )
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = MiuixIcons.Basic.ArrowUpDown,
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                contentDescription = stringResource(R.string.toggle_dropdown),
            )
        }

        WindowListPopup(
            show = expanded.value,
            alignment = PopupPositionProvider.Align.Start,
            popupPositionProvider = DropdownWithStartMarginProvider,
            onDismissRequest = { expanded.value = false },
        ) {
            ListPopupColumn {
                items.forEachIndexed { index, text ->
                    DropdownImpl(
                        text = text,
                        optionSize = items.size,
                        isSelected = selectedIndex == index,
                        index = index,
                        onSelectedIndexChange = { newIndex ->
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onSelectedIndexChange(newIndex)
                            expanded.value = false
                        },
                    )
                }
            }
        }
    }
}

private val DropdownWithStartMarginProvider =
    ListPopupDefaults.dropdownPositionProvider(12.dp, 8.dp)

private val ArrowUpIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ArrowUpward",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4f, 12f)
            lineTo(5.41f, 13.41f)
            lineTo(11f, 7.83f)
            verticalLineTo(20f)
            horizontalLineTo(13f)
            verticalLineTo(7.83f)
            lineTo(18.59f, 13.41f)
            lineTo(20f, 12f)
            lineTo(12f, 4f)
            close()
        }
    }.build()
}

@Composable
private fun EmptyRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.app_list_empty),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            style = MiuixTheme.textStyles.main,
        )
    }
}

@Composable
private fun AppListItem(
    modifier: Modifier = Modifier,
    app: AppEntry,
    iconCache: LruCache<String, ImageBitmap>,
    iconSize: Int,
    checked: Boolean,
    shape: Shape,
    showPackageName: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardDefaults.defaultColors().color),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = { onToggle(!checked) })
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppIcon(
                app = app,
                iconCache = iconCache,
                iconSize = iconSize,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
            ) {
                Text(
                    text = app.label,
                    style = MiuixTheme.textStyles.title4,
                )
                AnimatedVisibility(showPackageName) {
                    Text(
                        text = app.packageName,
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    )
                }
                if (!app.isAvailable) {
                    Text(
                        text = stringResource(R.string.app_list_unavailable),
                        color = MiuixTheme.colorScheme.error,
                        style = MiuixTheme.textStyles.footnote2,
                    )
                }
            }
            Switch(
                modifier = Modifier.align(Alignment.CenterVertically),
                checked = checked,
                enabled = enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun AppIcon(
    app: AppEntry,
    iconCache: LruCache<String, ImageBitmap>,
    iconSize: Int,
    modifier: Modifier = Modifier,
) {
    val packageManager = LocalContext.current.packageManager
    val cacheKey = remember(app.packageName, iconSize) { "${app.packageName}:$iconSize" }
    val icon by produceState<ImageBitmap?>(
        initialValue = iconCache.get(cacheKey),
        cacheKey,
        app.isAvailable,
    ) {
        if (value == null && app.isAvailable) {
            val loaded = withContext(Dispatchers.IO) {
                loadAppIcon(
                    packageManager = packageManager,
                    packageName = app.packageName,
                    iconSize = iconSize.coerceAtLeast(1),
                )
            }
            if (loaded != null) {
                iconCache.put(cacheKey, loaded)
            }
            value = loaded
        }
    }
    val loadedIcon = icon
    if (loadedIcon != null) {
        androidx.compose.foundation.Image(
            bitmap = loadedIcon,
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier)
    }
}

@Suppress("DEPRECATION")
private fun loadApps(
    packageManager: PackageManager,
    ownPackageName: String,
): List<AppEntry> {
    return packageManager.getInstalledApplications(PackageManager.MATCH_ALL)
        .asSequence()
        .filter { it.packageName != ownPackageName }
        .mapNotNull { info ->
            runCatching {
                val label = info.loadLabel(packageManager).toString()
                    .ifBlank { info.packageName }
                val sortKey = runCatching {
                    convertToPinyinString(label, "", WITHOUT_TONE).lowercase()
                }.getOrDefault(label.lowercase())
                AppEntry(
                    packageName = info.packageName,
                    label = label,
                    sortKey = sortKey,
                    firstInstallTime = runCatching {
                        packageManager.getPackageInfo(info.packageName, 0).firstInstallTime
                    }.getOrDefault(0L),
                    isSystem = info.flags and (
                            ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
                            ) != 0,
                    isAvailable = true,
                )
            }.getOrNull()
        }
        .sortedBy { it.sortKey }
        .toList()
}

private fun loadAppIcon(
    packageManager: PackageManager,
    packageName: String,
    iconSize: Int,
): ImageBitmap? {
    val drawable = runCatching {
        packageManager.getApplicationIcon(packageName)
    }.getOrNull()
    return runCatching {
        drawable?.toBitmap(iconSize, iconSize)?.asImageBitmap()
    }.getOrNull()
}

package com.example.simplelauncher

import android.app.Activity
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.net.Uri
import androidx.compose.foundation.Image
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.text.format.DateUtils
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.WindowInsets as layoutWindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.simplelauncher.ui.theme.SimpleLauncherTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextClock
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.core.DataStore
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.util.prefs.Preferences
import androidx.core.net.toUri

//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

    private var homeScreenTrigger = mutableStateOf(0)
    private var stateOfPage = mutableStateOf<LauncherPage>(LauncherPage.MainPage)

//    fun onPageChange(LauncherPage:LauncherPage) {
//        stateOfPage.value = LauncherPage
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setFullScreen(window)
//        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContent {
            if(!MyNotificationListener.isEnabled(this)){
                MyNotificationListener.openNotificationAccessSettings(this)
            }
            SimpleLauncherTheme {
                val triggerValue by remember {homeScreenTrigger}
                var currentPage by remember {stateOfPage}
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LauncherScreen(
                        currentPage = currentPage,
                        onPageChange = { launcherPage:LauncherPage -> currentPage = launcherPage },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
//        WindowInsetsControllerCompat(window, window.decorView).apply {
//            hide(WindowInsetsCompat.Type.statusBars())
//            hide(WindowInsetsCompat.Type.navigationBars())
//            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)){
            stateOfPage.value = LauncherPage.MainPage
        }

    }
}

sealed class LauncherPage {
    object NotificationsPage : LauncherPage()
    object MainPage : LauncherPage()
    object AppsPage : LauncherPage()
}

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

fun vibrate(context:Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26){
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(50)
    }
}

@Composable
fun LauncherScreen(currentPage: LauncherPage, onPageChange: (LauncherPage) -> Unit, modifier: Modifier) {
    val focusRequester = remember { FocusRequester() }
    var isKeyEventProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val apps by remember { mutableStateOf(GetAllInstalledApps(context)) }
    var savedApps by remember{ mutableStateOf(emptyList<AppInfo>()) }
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

//    LaunchedEffect(homeScreenSignal) {
//        if (homeScreenSignal > 0) {
//            currentPage = LauncherPage.MainPage
//        }
//    }
//    LaunchedEffect(currentPage) {
//        if (currentPage == LauncherPage.AppsPage) {
//            delay(100)
//            focusRequester.requestFocus()
//        }
//    }

    BackHandler(enabled = true) {
        when (currentPage) {
            LauncherPage.MainPage -> {
                println("on main page. Consuming press")
            }
            else -> {
                onPageChange(LauncherPage.MainPage)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        Image(
            painter = painterResource(R.drawable.glow),
            contentDescription = null, // Or provide a meaningful description
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop // Or use other content scales as needed
        )
        Column(modifier = modifier
            .fillMaxSize()
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (isKeyEventProcessing) {
                    return@onPreviewKeyEvent true
                }
//            println(keyEvent.nativeKeyEvent.keyCode)
                when (keyEvent.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN && !isKeyEventProcessing) {
                            isKeyEventProcessing = true
                            when (currentPage) {
                                LauncherPage.MainPage -> onPageChange(LauncherPage.NotificationsPage)
                                LauncherPage.AppsPage -> onPageChange(LauncherPage.MainPage)
                                else -> true
                            }
                            isKeyEventProcessing = false
                            true
                        } else if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                            isKeyEventProcessing = false
                            true
                        } else {
                            false
                        }
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN && !isKeyEventProcessing) {
                            isKeyEventProcessing = true
                            when (currentPage) {
                                LauncherPage.MainPage -> onPageChange(LauncherPage.AppsPage)
                                LauncherPage.NotificationsPage -> onPageChange(LauncherPage.MainPage)
                                else -> true
                            }
                            isKeyEventProcessing = false
                            true
                        } else if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                            isKeyEventProcessing = false
                            true
                        } else {
                            false
                        }
                    }

                    else -> false
                }

            }
        ) {
            when (currentPage) {
                LauncherPage.NotificationsPage -> NotificationsPage(onNavigateBack = {onPageChange(LauncherPage.MainPage)})
                LauncherPage.MainPage -> MainPage(modifier, savedApps)
                LauncherPage.AppsPage -> {
                    AppsPage(modifier, apps, lazyListState, focusManager, savedApps, onSavedAppsChange = {newSavedApps -> savedApps = newSavedApps}, onNavigateBack = {onPageChange(LauncherPage.MainPage)})
//                focusManager.moveFocus(FocusDirection.Next)
                }
            }
        }
    }

}

@Composable
fun NotificationsPage(onNavigateBack: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var selectedIndex by remember { mutableStateOf(0) }
    val currentNotifications = MyNotificationListener.currentNotifications
    if (currentNotifications.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Notifications",
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Notifications",
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .focusable()
                .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionDown -> {
                            if (selectedIndex < currentNotifications.size - 1) {
                                selectedIndex++
//                                Toast.makeText(context, "Down", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }

                        Key.DirectionUp -> {
                            if (selectedIndex > 0) {
                                selectedIndex--
                                println(selectedIndex)
//                                Toast.makeText(context, "Up", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }

                        Key.DirectionCenter -> {
                            if (currentNotifications.isNotEmpty()) {
                                try {
                                    currentNotifications[selectedIndex].contentIntent?.send()
                                } catch (e: Exception) {
                                    println(e)
                                }
                            }
                            true
                        }

                        Key.Back -> {
//                            focusManager.clearFocus()
                            onNavigateBack()
                            true
                        }

                        else -> false
                    }
                } else {
                    false
                }
            },
                state = listState,
            ) {
                itemsIndexed(currentNotifications
                ) { index, notification ->
                    Column(
                        modifier = Modifier
                                        .background(
                                            if (index == selectedIndex) Color.DarkGray.copy(0.6f) else
                                                Color.Transparent
                                        )
//                            .focusRequester(focusRequester)
//                            .focusable()
//                            .clickable {
//                                try {
//                                    notification.contentIntent?.send()
//                                } catch (e: Exception) {
//                                    println(e)
//                                }
//                            }
                    ){
                        Text(
                            modifier = Modifier
                                .padding(top = 4.dp),
                            text = notification.title.toString(),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = notification.text.toString(),
                            fontWeight = FontWeight.Light,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                        )
                        if (index != currentNotifications.lastIndex){
                            HorizontalDivider()
                        }

                    }

                }
            }
        }
    }

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    LaunchedEffect(Unit) {
        if (currentNotifications.isNotEmpty()) {
            focusRequester.requestFocus()
        }
    }


}

@Composable
fun MainPage(modifier: Modifier, apps: List<AppInfo>) {
    val context = LocalContext.current
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var selectedIndex by remember { mutableStateOf(0) }
    val localDensity  = LocalDensity.current
    var tabWidth by remember { mutableStateOf(0.dp) }
    val mediaList = MyNotificationListener.currentMedia
    val mediaItem = MyNotificationListener.currentPlayback
//    Log.d(mediaList.toString(), "MainPage: ")
//    if (mediaList.isNotEmpty()) {
//        Log.d("MediaList", "MainPage: $mediaList")
//    }


    LaunchedEffect(key1 = currentTimeMillis) {
//        while(true) {
//            currentTimeMillis = System.currentTimeMillis()
//        }
        currentTimeMillis = System.currentTimeMillis()
        delay(6000)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusable(false)
            .padding(16.dp, 36.dp),
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        TimeDateCard()
        MediaItem(mediaItem.value, modifier = Modifier.focusable())
//        LazyColumn(){
//            items(mediaList, key = {it.packageName}) { mediaData ->
//                key(mediaData.packageName) {
//                    MediaItem(mediaData = mediaData)
//                }
//            }
//        }
//        Text(
//            text = DateUtils.formatDateTime(LocalContext.current, currentTimeMillis, DateUtils.FORMAT_SHOW_TIME),
//            modifier = Modifier.focusable(false),
//        )
//        Text(
//            text = DateUtils.formatDateTime(LocalContext.current, currentTimeMillis,DateUtils.FORMAT_SHOW_DATE),
//            modifier = Modifier.focusable(false)
//        )

//        for (app in apps) {
//            Text(
//                text = app.name
//            )
//        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if(mediaItem.value.isPlaying) 0.dp else 36.dp)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionDown -> {
                                if (selectedIndex < apps.size - 1) {
                                    selectedIndex++
//                                Toast.makeText(context, "Down", Toast.LENGTH_SHORT).show()
                                }
                                true
                            }

                            Key.DirectionUp -> {
                                if (selectedIndex > 0) {
                                    selectedIndex--
                                    println(selectedIndex)
//                                Toast.makeText(context, "Up", Toast.LENGTH_SHORT).show()
                                }
                                true
                            }

                            Key.DirectionCenter -> {
                                if (apps.isNotEmpty()) {
                                    onAppClick(context, apps[selectedIndex])
                                }
                                true
                            }

                            else ->  {
                                when (keyEvent.nativeKeyEvent.keyCode) {
                                    KeyEvent.KEYCODE_0 -> {
//                                        vibrate(context)
                                        launchDialer("0", context)
                                    }
                                    KeyEvent.KEYCODE_1 -> {
//                                        vibrate(context)
                                        launchDialer("1", context)
                                    }
                                    KeyEvent.KEYCODE_2 -> {
//                                        vibrate(context)
                                        launchDialer("2", context)
                                    }
                                    KeyEvent.KEYCODE_3 -> {
//                                        vibrate(context)
                                        launchDialer("3", context)
                                    }
                                    KeyEvent.KEYCODE_4 -> {
//                                        vibrate(context)
                                        launchDialer("4", context)
                                    }
                                    KeyEvent.KEYCODE_5 -> {
//                                        vibrate(context)
                                        launchDialer("5", context)
                                    }
                                    KeyEvent.KEYCODE_6 -> {
//                                        vibrate(context)
                                        launchDialer("6", context)
                                    }
                                    KeyEvent.KEYCODE_7 -> {
//                                        vibrate(context)
                                        launchDialer("7", context)
                                    }
                                    KeyEvent.KEYCODE_8 -> {
//                                        vibrate(context)
                                        launchDialer("8", context)
                                    }
                                    KeyEvent.KEYCODE_9 -> {
//                                        vibrate(context)
                                        launchDialer("9", context)
                                    }

//                                    else -> {
//                                        false
//                                    }
                                }
                                false
                            }
                        }
                    } else {
                        false
                    }
                },
        ) {
            itemsIndexed(apps) { index, app ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.End
//                        .background(
//                            if (index == selectedIndex) Color.Blue.copy(alpha = 0.3f) else
//                                Color.Transparent
//                        )
//                        .clickable {
//                            onAppClick(context, apps[selectedIndex])
//                        }

//                        .focusRequester(focusRequesters[index])
//                        .focusable()
                ) {
//                    Text(
//                        text = app.name,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .align(Alignment.Center)
//                            .padding(8.dp)
//                    )
//                    Icon(
//                        painter = rememberDrawablePainter(drawable = app.icon),
//                        contentDescription = app.name,
//                        modifier = Modifier.size(48.dp),
//                        tint= Color.Unspecified
//                    )
//                    Spacer(modifier = Modifier.size(16.dp))
//                    Box(
//                        modifier = Modifier.onGloballyPositioned {
//                            with(localDensity) {
//                                tabWidth = it.size.width.toDp()
//                            }
//                        }
//                    ){
//
//                    }
//                    if (index == selectedIndex) {
//                        HorizontalDivider(
//                            modifier = Modifier
//                                .width(tabWidth)
//                        )
//                    }
                    Text(
                        text = app.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light,
                        textDecoration = if (index == selectedIndex) TextDecoration.Underline else TextDecoration.None
                    )
                }
            }
        }
    }


//    val items = (1..20).map { "Item $it" }
//    val focusRequester = remember { FocusRequester() }
//    val listState = rememberLazyListState()
//    var selectedIndex by remember { mutableStateOf(0) }

//
//    Column (
//        modifier = Modifier.fillMaxSize(),
//
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        LazyColumn(
//            state = listState,
//            modifier = Modifier
//                .fillMaxWidth()
//                .focusRequester(focusRequester)
//                .focusable()
//                .onKeyEvent { KeyEvent ->
//                    if (KeyEvent.type == KeyEventType.KeyDown) {
//                        when (KeyEvent.key) {
//                            Key.DirectionDown -> {
//                                if (selectedIndex < apps.size - 1) {
//                                    selectedIndex++
////                                Toast.makeText(context, "Down", Toast.LENGTH_SHORT).show()
//                                }
//                                true
//                            }
//
//                            Key.DirectionUp -> {
//                                if (selectedIndex > 0) {
//                                    selectedIndex--
//                                    println(selectedIndex)
////                                Toast.makeText(context, "Up", Toast.LENGTH_SHORT).show()
//                                }
//                                true
//                            }
//
//                            Key.DirectionCenter -> {
//                                onAppClick(context, apps[selectedIndex])
//                                true
//                            }
//
//                            else -> false
//                        }
//                    } else {
//                        false
//                    }
//                },
//        ) {
//            itemsIndexed(apps) { index, app ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .background(
//                            if (index == selectedIndex) Color.Blue.copy(alpha = 0.3f) else
//                                Color.Transparent
//                        )
////                        .clickable {
////                            onAppClick(context, apps[selectedIndex])
////                        }
//
////                        .focusRequester(focusRequesters[index])
////                        .focusable()
//                ) {
////                    Text(
////                        text = app.name,
////                        textAlign = TextAlign.Center,
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .align(Alignment.Center)
////                            .padding(8.dp)
////                    )
//                    Icon(
//                        painter = rememberDrawablePainter(drawable = app.icon),
//                        contentDescription = app.name,
//                        modifier = Modifier.size(48.dp),
//                        tint= Color.Unspecified
//                    )
//                    Spacer(modifier = Modifier.size(16.dp))
//                    Text(
//                        text = app.name,
//                        fontSize = 18.sp,
//                    )
//                }
//            }
//        }
//    }
//
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
//@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalMaterial3Api::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsPage(modifier: Modifier, apps: List<AppInfo>, lazyListState: LazyListState, focusManager: FocusManager, savedApps: List<AppInfo>, onSavedAppsChange: (List<AppInfo>) -> Unit, onNavigateBack: ()->Unit) {
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var selectedIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    var isHoldingDown by remember { mutableStateOf(false) }
    var isLongPress by remember { mutableStateOf(false) }

    var isMenuExpanded by remember { mutableStateOf(false) }
    val options = listOf(if (savedApps.contains(apps[selectedIndex])) "Remove from Home Screen" else "Add to Home Screen", "Uninstall", "App Info")

//    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    BackHandler(enabled = true) {
        focusManager.clearFocus()
        onNavigateBack()
    }

    Box (
        modifier = Modifier.fillMaxSize(),

//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 4.dp, end = 4.dp)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { KeyEvent ->
                    if (KeyEvent.type == KeyEventType.KeyDown) {
                        when (KeyEvent.key) {
                            Key.DirectionDown -> {
                                if (selectedIndex < apps.size - 1) {
                                    selectedIndex++
//                                Toast.makeText(context, "Down", Toast.LENGTH_SHORT).show()
//                                    return@onKeyEvent true
                                }
                                true
                            }

                            Key.DirectionUp -> {
                                if (selectedIndex > 0) {
                                    selectedIndex--
//                                    println(selectedIndex)
//                                Toast.makeText(context, "Up", Toast.LENGTH_SHORT).show()
//                                    return@onKeyEvent true
                                }
                                true
                            }

                            Key.DirectionCenter -> {
//                                onAppClick(context, apps[selectedIndex])
                                isHoldingDown = true
//                                isLongPress = false
                                true
                            }

                            Key.Back -> {
                                if (!isMenuExpanded) {
                                    focusManager.clearFocus()
                                    onNavigateBack()
                                }
                                true
                            }

                            else -> false
                        }
                    } else if (KeyEvent.type == KeyEventType.KeyUp) {
                        when (KeyEvent.key) {
                            Key.DirectionCenter -> {
                                isHoldingDown = false
                                if (!isLongPress) {
                                    onAppClick(context, apps[selectedIndex])
//                                    return@onKeyEvent true
                                } else {
//                                    onAppLongClick()
                                    isLongPress = false
                                    println("Long Click")
                                    isMenuExpanded = true
//                                    scope.launch {
//                                        sheetState.show()
//                                    }
//                                    return@onKeyEvent true
                                }
                            }
                        }
                        true
                    } else {
                        false
                    }
                },
        ) {
            itemsIndexed(apps) { index, app ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .padding(16.dp)
                        .background(
                            if (index == selectedIndex) Color.DarkGray.copy(0.6f) else
                                Color.Transparent
                        )
//                        .clickable {
//                            onAppClick(context, apps[selectedIndex])
//                        }

//                        .focusRequester(focusRequesters[index])
//                        .focusable()
                ) {
//                    Text(
//                        text = app.name,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .align(Alignment.Center)
//                            .padding(8.dp)
//                    )
                    Row(
                        modifier = Modifier
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            painter = rememberDrawablePainter(drawable = app.icon),
                            contentDescription = app.name,
                            modifier = Modifier.size(48.dp),
                            tint= Color.Unspecified
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = app.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
//
        DropdownMenu (
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
//                alignment = Alignment.BottomStart,
//                offset = DpOffset(x = 0.dp, y = 500.dp),
            modifier = Modifier
                .fillMaxWidth()
//                    .background(Color.DarkGray)
//                    .offset(y = 16.dp)
//                    .focusRequester(focusRequester)
//                    .focusable()
        ) {
            Column(
//                modifier = Modifier
//                    .focusRequester(focusRequester)
//                    .focusable()
//                    .background(Color.DarkGray)
            ) {
                Text(
                    text = "Menu Title"
                )
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            println("option clicked! $option")
                            when (option) {
                                "Add to Home Screen" -> {
                                    if (!savedApps.contains(apps[selectedIndex])) {
                                        onSavedAppsChange(savedApps + apps[selectedIndex])
                                        println("app added to home screen " + apps[selectedIndex].name)
                                    }
                                }
                                "Remove from Home Screen" -> {
                                    onSavedAppsChange(savedApps.filter({it != apps[selectedIndex]}))
                                }
                                "Uninstall" -> {
                                    println("app uninstalled " + apps[selectedIndex].name)
                                    uninstallApp(apps[selectedIndex].packageName, context)
                                }
                                "App Info" -> {
                                    println("app info " + apps[selectedIndex].name)
                                    openAppInfo(apps[selectedIndex].packageName, context)
                                }
                            }
                            isMenuExpanded = false
                        },
//                        modifier = Modifier.focusRequester(focusRequester).focusable()
                    )
                }
            }
        }

//        if(sheetState.isVisible) {
//            ModalBottomSheet(
//                onDismissRequest = {
//                    scope.launch {
//                        sheetState.hide()
//                    }
//                },
//                sheetState = sheetState,
//                dragHandle = {},
////                tonalElevation = (-4).dp,
//                contentWindowInsets = { layoutWindowInsets.navigationBars},
//                modifier = Modifier
//                    .windowInsetsPadding(layoutWindowInsets.safeDrawing)
//                    .navigationBarsPadding()
//                    .imePadding()
//                    .padding(bottom = 16.dp)
//                    .offset(y = 16.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .padding(4.dp)
//                ) {
//                    Text(
//                        text = "Menu",
//                    )
//                    options.forEach { option ->
//                        Text(
//                            text = option,
//                        )
//                    }
//                }
//            }
//        }
    }

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(isHoldingDown) {
        if(isHoldingDown) {
            delay(500)
            if(isHoldingDown) {
                isLongPress = true
                println("long press")
                vibrate(context)
//                delay(100)
//                isMenuExpanded = true
            }
        }
    }
}

fun GetAllInstalledApps (context: Context): List<AppInfo> {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val packages = packageManager.queryIntentActivities(intent, 0)
    val apps = packages.map {
        val appName = it.loadLabel(packageManager).toString()
        val packageName = it.activityInfo.packageName
        val icon = it.loadIcon(packageManager)
        AppInfo(appName, packageName, icon)
    }.toMutableList()

    apps.sortBy {it.name}
    return apps
}

fun onAppClick(context: Context, app:AppInfo) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(app.packageName)
    if (intent != null) {
        context.startActivity(intent)
    }
}

fun uninstallApp(packageName: String, context: Context) {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = "package:$packageName".toUri()
    }
    context.startActivity(intent)
}

fun openAppInfo(packageName: String, context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    context.startActivity(intent)
}

fun setFullScreen(window: android.view.Window) {
    // Hide the system bars.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(false)
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.systemBars())
//            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

//fun getCurrentTimeAsString(): String {
//    val currentMoment: Instant = Clock.System.now()
//}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    SimpleLauncherTheme {
//        Greeting("Android")
//    }
//}

//@Composable
//fun BottomNavMenu(
//    expanded: Boolean,
//    onDismissRequest: () -> Unit,
//    content: @Composable () -> Unit,
////    modifier: Modifier
//){
//    if(expanded){
//        BackHandler (enabled = expanded) {
//            onDismissRequest()
//        }
//
//        Box {
//            content()
//        }
//    }
//}

//@Preview
@Composable
fun TimeDateCard() {
    val context = LocalContext.current
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(key1 = currentTimeMillis) {
//        while(true) {
//            currentTimeMillis = System.currentTimeMillis()
//        }
        currentTimeMillis = System.currentTimeMillis()
        delay(6000)
    }
    Column(
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = DateUtils.formatDateTime(LocalContext.current, currentTimeMillis,DateUtils.FORMAT_SHOW_DATE),
            modifier = Modifier.focusable(false),
            fontSize = 20.sp,
        )
        Text(
            text = DateUtils.formatDateTime(LocalContext.current, currentTimeMillis, DateUtils.FORMAT_SHOW_TIME),
            modifier = Modifier.focusable(false),
            fontSize = 48.sp,
        )
    }
}

@Composable
fun MediaItem(mediaData: MediaData, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if (mediaData.isPlaying) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row (
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .clickable {
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(mediaData.packageName)
                        if (launchIntent != null)
                            context.startActivity(launchIntent)
                    },
            ) {
                if (mediaData.albumArt != null) {
                    Image(
                        painter = rememberDrawablePainter(drawable = mediaData.albumArt),
                        contentDescription = mediaData.title,
                        modifier = Modifier.size(88.dp).padding(4.dp)
                    )
                }
                Column {
                    Text(text = "Title: ${mediaData.title ?: "N/A"}", modifier = Modifier.basicMarquee())
                    Text(text = "Artist: ${mediaData.artist ?: "N/A"}")
//                Text(text = "Is Playing: ${mediaData.isPlaying}")
                }
            }
        }

    }
//        Card(
//            modifier = modifier
//                .fillMaxWidth()
//                .padding(8.dp)
//                .clickable {
//                    val launchIntent =
//                        context.packageManager.getLaunchIntentForPackage(mediaData.packageName)
//                    if (launchIntent != null)
//                        context.startActivity(launchIntent)
//                }
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(text = "Package: ${mediaData.packageName}")
//                Text(text = "Title: ${mediaData.title ?: "N/A"}")
//                Text(text = "Artist: ${mediaData.artist ?: "N/A"}")
//                Text(text = "Is Playing: ${mediaData.isPlaying}")
//            }
//        }
//    }
}

fun launchDialer(number: String, context: Context){
    val intent = Intent(Intent.ACTION_DIAL, "tel:$number".toUri())
    context.startActivity(intent)
}
package com.naicha.diary.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.naicha.diary.app
import com.naicha.diary.data.AppSettings
import com.naicha.diary.data.Drink
import com.naicha.diary.notify.LiveUpdateNotifier
import com.naicha.diary.ui.components.BouncyBox
import com.naicha.diary.ui.screens.HomeScreen
import com.naicha.diary.ui.screens.RecordSheet
import com.naicha.diary.ui.screens.SettingsSheet
import com.naicha.diary.ui.screens.StatsScreen
import com.naicha.diary.ui.screens.DrinkDetailSheet
import com.naicha.diary.ui.screens.WallScreen
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.util.TimeUtil
import java.io.File

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("首页", Icons.Filled.Home),
    TabItem("饮品墙", Icons.Filled.Favorite),
    TabItem("统计", Icons.Filled.DateRange),
    TabItem("我的", Icons.Filled.Person),
)

const val dailyGoal = 1

@Composable
fun DrinkRoot() {
    val repository = app.repository
    val items by repository.items.collectAsState()
    val context = LocalContext.current

    var tab by remember { mutableStateOf(0) }
    var showRecord by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Drink?>(null) }
    var detail by remember { mutableStateOf<Drink?>(null) }

    var aiImage by remember { mutableStateOf<Uri?>(null) }
    var photoImage by remember { mutableStateOf<Uri?>(null) }
    var pickForAi by remember { mutableStateOf(true) }
    var openSheetAfterPick by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val target = pendingCameraUri
        pendingCameraUri = null
        if (success) {
            if (openSheetAfterPick) {
                editing = null
                showRecord = true
            }
        } else {
            if (pickForAi) aiImage = null else photoImage = null
            target?.let { uri ->
                runCatching { context.contentResolver.delete(uri, null, null) }
            }
        }
        openSheetAfterPick = false
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            if (pickForAi) aiImage = uri else photoImage = uri
            if (openSheetAfterPick) {
                editing = null
                showRecord = true
            }
        }
        openSheetAfterPick = false
    }

    fun requestPick(forAi: Boolean, fromCamera: Boolean, openSheet: Boolean = false) {
        pickForAi = forAi
        openSheetAfterPick = openSheet
        if (fromCamera) {
            val uri = createCameraUri(context)
            if (uri == null) {
                openSheetAfterPick = false
                return
            }
            pendingCameraUri = uri
            if (forAi) aiImage = uri else photoImage = uri
            runCatching { cameraLauncher.launch(uri) }
        } else {
            runCatching {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val todayCount = items.count { TimeUtil.isToday(it.timestamp) }
    val latest = items.maxByOrNull { it.timestamp }

    LaunchedEffect(todayCount, latest?.id) {
        if (todayCount > 0) {
            LiveUpdateNotifier.show(context, todayCount, dailyGoal, latest)
        } else {
            LiveUpdateNotifier.dismiss(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Crossfade(targetState = tab, label = "tab") { current ->
            when (current) {
                0 -> HomeScreen(
                    items = items,
                    onRecord = {
                        editing = null
                        aiImage = null
                        showRecord = true
                    },
                    onAiRecord = {
                        if (AppSettings.hasApiKey) {
                            requestPick(forAi = true, fromCamera = true, openSheet = true)
                        } else {
                            showSettings = true
                        }
                    },
                    onOpenWall = { tab = 1 },
                    onOpenDetail = { detail = it },
                )
                1 -> WallScreen(
                    items = items,
                    onOpenDetail = { detail = it },
                )
                2 -> StatsScreen(items = items)
                else -> HomeScreen(
                    items = items,
                    onRecord = {
                        editing = null
                        aiImage = null
                        showRecord = true
                    },
                    onAiRecord = {
                        if (AppSettings.hasApiKey) {
                            requestPick(forAi = true, fromCamera = true, openSheet = true)
                        } else {
                            showSettings = true
                        }
                    },
                    onOpenWall = { tab = 1 },
                    onOpenDetail = { detail = it },
                )
            }
        }

        MilkBottomBar(
            current = tab,
            onSelect = { index ->
                if (index == 3) showSettings = true else tab = index
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp),
        )

        RecordFab(
            onClick = {
                editing = null
                aiImage = null
                showRecord = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 96.dp),
        )
    }

    if (showRecord) {
        RecordSheet(
            initial = editing,
            aiImage = aiImage,
            photoImage = photoImage,
            onDismiss = {
                showRecord = false
                editing = null
                aiImage = null
                photoImage = null
            },
            onOpenSettings = {
                showRecord = false
                editing = null
                aiImage = null
                showSettings = true
            },
            onPick = { forAi, fromCamera -> requestPick(forAi, fromCamera, openSheet = false) },
            onAiImageConsumed = { aiImage = null },
            onPhotoImageConsumed = { photoImage = null },
            onSave = { drink ->
                if (editing == null) repository.add(drink) else repository.update(drink)
                showRecord = false
                editing = null
                aiImage = null
                photoImage = null
            },
            onDelete = { drink ->
                repository.remove(drink.id)
                showRecord = false
                editing = null
                aiImage = null
                photoImage = null
            },
        )
    }

    if (showSettings) {
        SettingsSheet(
            items = items,
            onDismiss = { showSettings = false },
            onClearAll = { repository.clearAll() },
        )
    }

    val current = detail
    if (current != null) {
        DrinkDetailSheet(
            tea = current,
            onDismiss = { detail = null },
            onEdit = {
                editing = current
                detail = null
                showRecord = true
            },
            onDelete = {
                repository.remove(current.id)
                detail = null
            },
        )
    }
}

@Composable
private fun RecordFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "fab")
    transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "fabPulse",
    )

    BouncyBox(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Caramel.copy(alpha = 0.65f),
                    spotColor = Caramel.copy(alpha = 0.65f),
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFD89B69), Caramel, Color(0xFFB0724A))
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "记一杯",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun MilkBottomBar(
    current: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Caramel.copy(alpha = 0.45f),
                spotColor = Caramel.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.97f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        tabs.forEachIndexed { index, item ->
            val selected = current == index
            BouncyBox(onClick = { onSelect(index) }) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(Caramel, Color(0xFFE0A574)))
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color.Transparent)
                                )
                            }
                        )
                        .padding(horizontal = if (selected) 15.dp else 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) Color.White else PearlSoft,
                        modifier = Modifier.size(20.dp),
                    )
                    if (selected) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

private fun createCameraUri(context: Context): Uri? = runCatching {
    val name = "drink_${System.currentTimeMillis()}.jpg"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/饮品日记",
            )
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    } else {
        val dir = File(context.cacheDir, "capture").apply { mkdirs() }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(dir, name))
    }
}.getOrNull()

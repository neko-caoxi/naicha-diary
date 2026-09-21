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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeTint
import androidx.core.content.FileProvider
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import com.naicha.diary.app
import com.naicha.diary.data.AppSettings
import com.naicha.diary.data.Drink
import com.naicha.diary.notify.LiveUpdateNotifier
import com.naicha.diary.ui.components.BouncyBox
import com.naicha.diary.ui.screens.HomeScreen
import com.naicha.diary.ui.screens.RecordSheet
import com.naicha.diary.ui.screens.SettingsContent
import com.naicha.diary.ui.screens.StatsScreen
import com.naicha.diary.ui.screens.DrinkDetailSheet
import com.naicha.diary.ui.screens.WallScreen
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.util.TimeUtil
import kotlinx.coroutines.launch
import java.io.File

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("首页", Icons.Filled.Home),
    TabItem("饮品墙", Icons.Filled.Favorite),
    TabItem("统计", Icons.Filled.DateRange),
    TabItem("我的", Icons.Filled.Person),
)

private const val RECORD_SLOT = 2

const val dailyGoal = 1

@Composable
fun DrinkRoot() {
    val repository = app.repository
    val items by repository.items.collectAsState()
    val context = LocalContext.current
    val hazeState = remember { HazeState() }

    var tab by remember { mutableStateOf(0) }
    var showRecord by remember { mutableStateOf(false) }
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

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState),
            beyondViewportPageCount = 1,
        ) { page ->
            when (page) {
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
                            scope.launch { pagerState.animateScrollToPage(3) }
                        }
                    },
                    onOpenWall = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onOpenDetail = { detail = it },
                )
                1 -> WallScreen(
                    items = items,
                    onOpenDetail = { detail = it },
                )
                2 -> StatsScreen(items = items)
                else -> SettingsContent(
                    items = items,
                    onClearAll = { repository.clearAll() },
                )
            }
        }

        MilkBottomBar(
            hazeState = hazeState,
            pagerState = pagerState,
            onSelect = { index ->
                scope.launch { pagerState.animateScrollToPage(index) }
            },
            onRecord = {
                editing = null
                aiImage = null
                showRecord = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 16.dp),
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
                scope.launch { pagerState.animateScrollToPage(3) }
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

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun MilkBottomBar(
    hazeState: HazeState,
    pagerState: PagerState,
    onSelect: (Int) -> Unit,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barShape = RoundedCornerShape(42.dp)
    val slotCount = tabs.size + 1
    val gap = 4.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = barShape,
                ambientColor = Caramel.copy(alpha = 0.40f),
                spotColor = Caramel.copy(alpha = 0.40f),
            )
            .clip(barShape)
            .hazeChild(state = hazeState) {
                blurRadius = 48.dp
                noiseFactor = 0.03f
                backgroundColor = Color.Transparent
                tints = listOf(HazeTint(Color.White.copy(alpha = 0.09f)))
            }
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.92f),
                        Color.White.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.10f),
                    )
                ),
                shape = barShape,
            )
            .padding(horizontal = 9.dp, vertical = 10.dp),
    ) {
        val slotWidth = (maxWidth - gap * (slotCount - 1)) / slotCount

        // 连续位置：跳过中间的加号槽位
        val continuous = pagerState.currentPage + pagerState.currentPageOffsetFraction
        val slotPos = if (continuous < RECORD_SLOT - 0.5f) continuous else continuous + 1f

        Box {
            // 滑动指示器
            Box(
                modifier = Modifier
                    .offset(x = (slotWidth + gap) * slotPos)
                    .width(slotWidth)
                    .height(62.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Caramel, Color(0xFFE0A574)))
                    ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                tabs.forEachIndexed { index, item ->
                    if (index == RECORD_SLOT) {
                        RecordButton(onClick = onRecord, modifier = Modifier.width(slotWidth))
                    }
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .width(slotWidth)
                            .height(62.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSelect(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) Color.White else PearlSoft,
                                modifier = Modifier.size(25.dp),
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else PearlSoft,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
                if (RECORD_SLOT >= tabs.size) {
                    RecordButton(onClick = onRecord, modifier = Modifier.width(slotWidth))
                }
            }
        }
    }
}

@Composable
private fun RecordButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    BouncyBox(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = CircleShape,
                        ambientColor = Caramel.copy(alpha = 0.7f),
                        spotColor = Caramel.copy(alpha = 0.7f),
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE2A776), Caramel, Color(0xFFB0724A))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "记一杯",
                    tint = Color.White,
                    modifier = Modifier.size(27.dp),
                )
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

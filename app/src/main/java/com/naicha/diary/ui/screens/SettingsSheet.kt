package com.naicha.diary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naicha.diary.DrinkApp
import com.naicha.diary.data.AppSettings
import com.naicha.diary.data.Drink
import com.naicha.diary.net.DeepSeekClient
import com.naicha.diary.ui.components.GradientButton
import com.naicha.diary.ui.components.SectionHeader
import com.naicha.diary.ui.components.SoftCard
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.Matcha
import com.naicha.diary.ui.theme.Outline
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.ui.theme.Strawberry
import com.naicha.diary.ui.theme.Taro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    items: List<Drink>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf(AppSettings.apiKey) }
    var reveal by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var autoRecognize by remember { mutableStateOf(AppSettings.autoRecognize) }
    var confirmClear by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Outline)
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "我的",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            SoftCard {
                SectionHeader(title = "AI 识图录入", subtitle = "拍小票自动填表")
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "填入 DeepSeek API Key 后，在记录页拍照或选图，" +
                        "会自动识别品牌、品名、规格和价格。" +
                        "Key 只保存在本机，不会上传到别处。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PearlSoft,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it.trim() },
                    placeholder = { Text("sk-...", color = Outline) },
                    singleLine = true,
                    visualTransformation = if (reveal) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Caramel,
                        unfocusedBorderColor = Outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        cursorColor = Caramel,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallButton(text = if (reveal) "隐藏" else "显示") { reveal = !reveal }
                    Spacer(Modifier.width(8.dp))
                    SmallButton(text = "保存") {
                        AppSettings.apiKey = apiKey
                        testResult = "已保存"
                    }
                    Spacer(Modifier.width(8.dp))
                    SmallButton(
                        text = if (testing) "测试中…" else "测试连接",
                        enabled = !testing,
                    ) {
                        AppSettings.apiKey = apiKey
                        testing = true
                        testResult = null
                        scope.launch {
                            testResult = DeepSeekClient.testConnection(apiKey)
                                .fold(
                                    onSuccess = { "连接正常：$it" },
                                    onFailure = { it.message ?: "连接失败" },
                                )
                            testing = false
                        }
                    }
                    if (testing) {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Caramel,
                        )
                    }
                }
                if (testResult != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = testResult!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (testResult!!.contains("正常") || testResult == "已保存") {
                            Matcha
                        } else {
                            Strawberry
                        },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "记录时优先用 AI 填表",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "开启后，选完图片直接进识别流程",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = autoRecognize,
                        onCheckedChange = {
                            autoRecognize = it
                            AppSettings.autoRecognize = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Caramel,
                        ),
                    )
                }
            }

            SoftCard {
                SectionHeader(title = "我的数据", subtitle = "全部存在本机")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox("记录", "${items.size}", Caramel, Modifier.weight(1f))
                    StatBox(
                        "照片",
                        "${items.count { it.photoPath != null }}",
                        Taro,
                        Modifier.weight(1f),
                    )
                    StatBox(
                        "品牌",
                        "${items.map { it.brand }.distinct().size}",
                        Matcha,
                        Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(14.dp))
                GradientButton(
                    text = if (confirmClear) "再点一次确认清空" else "清空所有记录",
                    onClick = {
                        if (confirmClear) {
                            onClearAll()
                            confirmClear = false
                        } else {
                            confirmClear = true
                        }
                    },
                    colors = if (confirmClear) {
                        listOf(Color(0xFFE53935), Color(0xFFEF5350))
                    } else {
                        listOf(Strawberry, Color(0xFFFFB3C6))
                    },
                )
            }

            val crash = remember { DrinkApp.lastCrash(context) }
            if (crash != null) {
                SoftCard(shadowColor = Strawberry) {
                    SectionHeader(title = "上次出错记录", subtitle = "反馈问题时可以复制这段")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = crash.take(1200),
                        style = MaterialTheme.typography.labelMedium,
                        color = PearlSoft,
                    )
                    Spacer(Modifier.height(12.dp))
                    SmallButton(text = "清除") { DrinkApp.clearCrash(context) }
                }
            }

            SoftCard {
                SectionHeader(title = "关于", subtitle = "饮品日记")
                Spacer(Modifier.height(10.dp))
                InfoLine("版本", "1.0.0")
                InfoLine("AI 模型", "deepseek-flash（视觉）")
                InfoLine("数据存储", "本机文件，不上传")
                InfoLine("体积", "约 1.2 MB")
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SmallButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) PearlSoft else Outline,
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = accent,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

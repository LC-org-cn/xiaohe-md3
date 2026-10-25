package com.xiaohe.english.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/** 学习统计：近 7 天数据 + 累计指标 + 周柱状图。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyStatsScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var stats by remember { mutableStateOf<com.xiaohe.english.network.model.StudyStatistics?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            loading = true; error = null
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            val end = fmt.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, -6)
            val start = fmt.format(cal.time)
            runCatching { ApiClient.study.getStudyStatistics(start, end) }
                .onSuccess { stats = it.data }
                .onFailure { error = it.message }
            loading = false
        }
    }

    fun fmtTime(sec: Long): String {
        return if (sec >= 3600) "${sec / 3600}小时${(sec % 3600) / 60}分"
        else if (sec >= 60) "${sec / 60}分钟"
        else "${sec}秒"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学习统计") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$error", color = MaterialTheme.colorScheme.error) }
            stats == null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("暂无统计数据") }
            else -> {
                val s = stats!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 概览卡
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("累计学习", style = MaterialTheme.typography.titleMedium)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    StatCell("连续天数", "${s.continuousDays} 天")
                                    StatCell("今日", fmtTime(s.todayStudyTime / 1000))
                                    StatCell("总计", fmtTime(s.totalStudyTime / 1000))
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    StatCell("学过课文", "${s.totalTexts} 篇")
                                    StatCell("学过单词", "${s.totalWords} 个")
                                }
                            }
                        }
                    }
                    // 周学习时长柱状图
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text("近 7 天学习时长", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(16.dp))
                                val days = s.weeklyData ?: emptyList()
                                if (days.isEmpty()) {
                                    Text("暂无每日数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    val maxSec = (days.maxOf { it.studyTime } / 1000).coerceAtLeast(1L).toFloat()
                                    Row(
                                        Modifier.fillMaxWidth().height(160.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        days.forEach { d ->
                                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                val ratio = (d.studyTime / 1000).toFloat() / maxSec
                                                val barColor = MaterialTheme.colorScheme.primary
                                                Box(
                                                    Modifier.fillMaxWidth(0.6f).height((ratio * 110f).dp.coerceAtLeast(4.dp)),
                                                ) {
                                                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                                                        drawRoundRect(
                                                            color = barColor,
                                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    (d.date?.takeLast(5) ?: "").replace("-", "/"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
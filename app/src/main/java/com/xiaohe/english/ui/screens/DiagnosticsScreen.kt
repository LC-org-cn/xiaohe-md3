package com.xiaohe.english.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

/** 诊断页：展示版本、设备与最近崩溃日志，便于用户截图上报。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var crashText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val dir = File(context.filesDir, "crash_logs")
        val files = dir.listFiles { f -> f.name.endsWith(".json") }?.sortedByDescending { it.lastModified() } ?: emptyList()
        crashText = if (files.isEmpty()) "暂无崩溃记录" else files.take(3).joinToString("\n\n────\n\n") { it.name + "\n" + it.readText().take(1500) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("诊断信息") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val pi = runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
                    Text("版本：${pi?.versionName ?: "?"} (${com.xiaohe.english.BuildConfig.OSS_AK_ID.take(6)}…)".replace(" (", " / "), style = MaterialTheme.typography.bodySmall)
                    Text("设备：${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}", style = MaterialTheme.typography.bodySmall)
                    Text("Android：${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("最近崩溃（最多 3 条）：", style = MaterialTheme.typography.titleSmall)
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    crashText.ifEmpty { "读取中…" },
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
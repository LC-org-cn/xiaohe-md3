package com.xiaohe.english.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.network.model.BookFunDubbingInfo
import com.xiaohe.english.network.model.BookFunDubbingSentence
import com.xiaohe.english.speech.EvalEvent
import com.xiaohe.english.speech.EvalResultParser
import com.xiaohe.english.speech.EvaluationSession
import com.xiaohe.english.utils.EvaluationLogUploader
import kotlinx.coroutines.launch

/** 趣配音：配音列表 → 逐句跟读（原声播放 + MscEngine 评测打分）。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunDubbingScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf(listOf<BookFunDubbingInfo>()) }
    var selected by remember { mutableStateOf<BookFunDubbingInfo?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            loading = true; error = null
            runCatching { ApiClient.material.getBookFunDubbingPageWithDetails(null, null, 1, 50) }
                .onSuccess { items = it.data?.list ?: emptyList() }
                .onFailure { error = it.message }
            loading = false
        }
    }

    selected?.let { d -> FunDubbingDetail(d, onBack = { selected = null }); return }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("趣配音") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$error", color = MaterialTheme.colorScheme.error) }
            items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("暂无配音内容") }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items) { d ->
                    Card(Modifier.fillMaxWidth().clickable { selected = d }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(d.title ?: "配音", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text("${d.bookFunDubbingSentenceDOList?.size ?: 0} 句", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FunDubbingDetail(info: BookFunDubbingInfo, onBack: () -> Unit) {
    val context = LocalContext.current
    val sentences = info.bookFunDubbingSentenceDOList ?: emptyList()
    var idx by remember { mutableIntStateOf(0) }
    var recording by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf<EvalResultParser.Scores?>(null) }
    var evalError by remember { mutableStateOf<String?>(null) }
    var hasMic by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMic = it }
    val session = remember { EvaluationSession() }
    DisposableEffect(Unit) { onDispose { session.cancel() } }

    val finishedCount = remember { mutableIntStateOf(0) }

    if (sentences.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text("该配音没有句子数据") }
        return
    }
    val s = sentences[idx.coerceIn(0, sentences.size - 1)]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(info.title ?: "趣配音") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!hasMic) { permLauncher.launch(Manifest.permission.RECORD_AUDIO); return@FloatingActionButton }
                    if (recording) { session.stop() } else {
                        score = null; evalError = null
                        val text = s.sentenceContent ?: ""
                        session.start(text) { ev ->
                            when (ev) {
                                is EvalEvent.Volume -> volume = ev.volume
                                is EvalEvent.FinalResult -> {
                                    val sc = EvalResultParser.parse(ev.xml)
                                    score = sc
                                    if ((sc.total ?: 0f) == 0f) {
                                        EvaluationLogUploader.uploadZeroScoreLog(
                                            params = mapOf("xml" to ev.xml.take(2000)),
                                            text = text,
                                            score = 0.0,
                                            deviceInfo = EvaluationLogUploader.getDeviceInfo(),
                                            extraInfo = EvaluationLogUploader.ExtraInfo(exerciseId = info.id.toString()),
                                        )
                                    }
                                    recording = false
                                }
                                is EvalEvent.Error -> { evalError = "${ev.code}: ${ev.message}"; recording = false }
                                is EvalEvent.State -> if (!ev.running) recording = false
                                else -> {}
                            }
                        }
                        recording = true
                    }
                },
                containerColor = if (recording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(if (recording) "停" else "录", color = if (recording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinearProgressIndicator(progress = { (idx + 1f) / sentences.size }, modifier = Modifier.fillMaxWidth())
            Text("第 ${idx + 1} / ${sentences.size} 句", style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text(s.sentenceContent ?: "", style = MaterialTheme.typography.bodyLarge)
                    s.sentenceContentChs?.let { chs ->
                        Spacer(Modifier.height(8.dp))
                        Text(chs, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            // 原声
            if (!s.sentenceAudio.isNullOrEmpty()) {
                Text("原声试听", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AudioPlayerCard(s.sentenceAudio)
            }
            // 评分
            score?.let { sc ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("总分 ${sc.total ?: 0f}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.height(4.dp))
                        Text("发音 ${sc.pronunciation ?: 0f} · 流利度 ${sc.fluency ?: 0f} · 完整度 ${sc.integrity ?: 0f}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            evalError?.let { e -> Text("评测失败：$e", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            if (recording) {
                Text("录音中… 音量 $volume", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (idx > 0) OutlinedButton(onClick = { idx--; score = null }, modifier = Modifier.weight(1f)) { Text("上一句") }
                Button(
                    onClick = {
                        if (idx < sentences.size - 1) { idx++; score = null }
                        else finishedCount.intValue = sentences.size
                    },
                    modifier = Modifier.weight(1f),
                ) { Text(if (idx < sentences.size - 1) "下一句" else "完成") }
            }
            if (finishedCount.intValue > 0) {
                SpeakingResultView(0, sentences.size, sentences.size, 1f, onBack = onBack)
            }
        }
    }
}
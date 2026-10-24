package com.xiaohe.english.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.network.model.SpeakingAnswerItem
import com.xiaohe.english.network.model.SubmitSpeakingAnswersRequest
import com.xiaohe.english.speech.EvalEvent
import com.xiaohe.english.speech.EvalResultParser
import com.xiaohe.english.speech.EvaluationSession
import com.xiaohe.english.utils.EvaluationLogUploader
import kotlinx.coroutines.launch

/** 口语专项：逐题跟读，MscEngine（讯飞本地评测）打分，交卷上报。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingExerciseScreen(
    exerciseId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var questions by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.QuestionSpecialItem>()) }
    var recordId by remember { mutableStateOf<String?>(null) }
    var exerciseTitle by remember { mutableStateOf("") }
    var idx by remember { mutableIntStateOf(0) }
    var submitting by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<com.xiaohe.english.network.model.SubmitAnswersResponse?>(null) }

    // per-question eval state
    var recording by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(0) }
    var evalScore by remember { mutableStateOf<EvalResultParser.Scores?>(null) }
    var evalError by remember { mutableStateOf<String?>(null) }
    val session = remember { EvaluationSession() }
    // questionId -> recorded score
    val scored = remember { mutableStateMapOf<String, EvalResultParser.Scores>() }

    // mic permission
    var hasMic by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMic = it }

    LaunchedEffect(exerciseId) {
        scope.launch {
            loading = true; error = null
            runCatching { ApiClient.study.getSpeakingQuestions(exerciseId, "") }
                .onSuccess { r ->
                    questions = r.data?.questions ?: emptyList()
                    recordId = r.data?.recordId
                    exerciseTitle = r.data?.exerciseTitle ?: "口语练习"
                }
                .onFailure { error = it.message }
            loading = false
        }
    }

    DisposableEffect(Unit) { onDispose { session.cancel() } }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    error?.let { e ->
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$e", color = MaterialTheme.colorScheme.error) }
        return
    }
    result?.let { r ->
        ResultView(r.userScore, r.totalCount, r.correctCount, r.accuracy, onBack = onBack)
        return
    }

    val q = questions.getOrNull(idx) ?: run {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text("没有题目") }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exerciseTitle) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(progress = { (idx + 1f) / questions.size }, modifier = Modifier.fillMaxWidth())
            Text("第 ${idx + 1} / ${questions.size} 题", style = MaterialTheme.typography.titleMedium)

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("请朗读", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        q.referenceText ?: q.questionContent ?: q.questionTitle ?: ""
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // 评测结果卡
            evalScore?.let { s ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("评测结果", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.height(8.dp))
                        ScoreRow("总分", s.total)
                        ScoreRow("发音", s.pronunciation)
                        ScoreRow("流利度", s.fluency)
                        ScoreRow("完整度", s.integrity)
                    }
                }
            }
            evalError?.let { e ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("评测失败：$e", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            Spacer(Modifier.weight(1f))

            // 录音按钮
            FloatingActionButton(
                onClick = {
                    if (!hasMic) { permLauncher.launch(Manifest.permission.RECORD_AUDIO); return@FloatingActionButton }
                    if (recording) {
                        session.stop()
                    } else {
                        evalScore = null; evalError = null
                        val text = q.referenceText ?: q.questionContent ?: q.questionTitle ?: ""
                        session.start(text) { ev ->
                            when (ev) {
                                is EvalEvent.Volume -> volume = ev.volume
                                is EvalEvent.PartialResult -> {}
                                is EvalEvent.FinalResult -> {
                                    val s = EvalResultParser.parse(ev.xml)
                                    evalScore = s
                                    q.questionId?.let { qid -> scored[qid] = s }
                                    // 零分诊断日志（与原版行为一致）
                                    if ((s.total ?: 0f) == 0f) {
                                        EvaluationLogUploader.uploadZeroScoreLog(
                                            params = mapOf("xml" to ev.xml.take(2000)),
                                            text = text,
                                            score = 0.0,
                                            deviceInfo = EvaluationLogUploader.getDeviceInfo(),
                                            extraInfo = EvaluationLogUploader.ExtraInfo(exerciseId = exerciseId),
                                        )
                                    }
                                    recording = false
                                }
                                is EvalEvent.Error -> { evalError = "${ev.code}: ${ev.message}"; recording = false }
                                is EvalEvent.State -> if (!ev.running) recording = false
                            }
                        }
                        recording = true
                    }
                },
                modifier = Modifier.size(80.dp),
                containerColor = if (recording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    if (recording) Icons.Filled.Stop else Icons.Filled.Mic,
                    if (recording) "停止" else "录音",
                    tint = if (recording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (recording) {
                Text("录音中… 点击方块停止", style = MaterialTheme.typography.bodySmall)
            } else if (evalScore == null && evalError == null) {
                Text("点击麦克风开始朗读", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                if (idx > 0) OutlinedButton(onClick = { idx--; evalScore = null; evalError = null }, modifier = Modifier.weight(1f)) { Text("上一题") }
                Button(
                    onClick = {
                        if (idx < questions.size - 1) { idx++; evalScore = null; evalError = null }
                        else if (!submitting) {
                            submitting = true
                            scope.launch {
                                runCatching {
                                    ApiClient.study.submitSpeakingAnswers(SubmitSpeakingAnswersRequest(
                                        answers = questions.mapNotNull { item ->
                                            item.questionId?.let { qid ->
                                                SpeakingAnswerItem(
                                                    questionId = qid,
                                                    score = ((scored[qid]?.total ?: 0f) * 10).toInt().coerceIn(0, 100),
                                                    duration = 0, timeSpent = 0,
                                                    evaluation = null, audioUrl = null,
                                                )
                                            }
                                        },
                                        exerciseId = exerciseId,
                                        recordId = recordId,
                                        totalTime = 0,
                                        finishTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.CHINA).format(java.util.Date()),
                                    ))
                                }.onSuccess { result = it.data }
                                 .onFailure { error = it.message }
                                submitting = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !submitting,
                ) { Text(if (idx < questions.size - 1) "下一题" else if (submitting) "提交中…" else "交卷") }
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, v: Float?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
        Text(if (v != null) String.format("%.1f", v) else "—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}
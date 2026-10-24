package com.xiaohe.english.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.network.model.AnswerItem
import com.xiaohe.english.network.model.QuestionSpecialItem
import com.xiaohe.english.network.model.SubmitListeningAnswersRequest
import com.xiaohe.english.utils.OssUtils
import kotlinx.coroutines.launch

/** 听力专项做题：逐题播放音频 + 选项作答 + 交卷。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningExerciseScreen(
    exerciseId: String,
    onBack: () -> Unit,
    onFinish: (score: Int, total: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var questions by remember { mutableStateOf(listOf<QuestionSpecialItem>()) }
    var recordId by remember { mutableStateOf<String?>(null) }
    var exerciseTitle by remember { mutableStateOf("") }
    var idx by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateListOf<Pair<String, String>>() } // questionId -> userAnswer
    var submitting by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<com.xiaohe.english.network.model.SubmitAnswersResponse?>(null) }

    LaunchedEffect(exerciseId) {
        scope.launch {
            loading = true; error = null
            runCatching { ApiClient.study.getListeningQuestions(exerciseId, "") }
                .onSuccess { r ->
                    questions = r.data?.questions ?: emptyList()
                    recordId = r.data?.recordId
                    exerciseTitle = r.data?.exerciseTitle ?: "听力练习"
                }
                .onFailure { error = it.message }
            loading = false
        }
    }

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

    val q = questions.getOrNull(idx)
    if (q == null) {
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
        ) {
            LinearProgressIndicator(
                progress = { (idx + 1f) / questions.size },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("第 ${idx + 1} / ${questions.size} 题", style = MaterialTheme.typography.titleMedium)

            AudioPlayerCard(q.audioUrl ?: q.exercisesQuestionAudioList?.firstOrNull()?.audioUrl)

            q.questionTitle?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            q.questionContent?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            val opts = q.options ?: emptyList()
            opts.forEach { opt ->
                val selected = answers.any { it.first == q.questionId && it.second == opt.code }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected, role = Role.RadioButton, onClick = {
                            answers.removeAll { it.first == q.questionId }
                            answers.add((q.questionId ?: "") to (opt.code ?: ""))
                        }),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(opt.content ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                if (idx > 0) {
                    OutlinedButton(onClick = { idx-- }, modifier = Modifier.weight(1f)) { Text("上一题") }
                }
                Button(
                    onClick = {
                        if (idx < questions.size - 1) idx++
                        else if (!submitting) {
                            submitting = true
                            scope.launch {
                                runCatching {
                                    ApiClient.study.submitListeningAnswers(SubmitListeningAnswersRequest(
                                        answers = answers.map { (qid, ans) ->
                                            AnswerItem(questionId = qid, userAnswer = ans, isCorrect = false, timeSpent = 0)
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
                    enabled = answers.any { it.first == q.questionId } && !submitting,
                ) {
                    Text(if (idx < questions.size - 1) "下一题" else if (submitting) "提交中…" else "交卷")
                }
            }
        }
    }
}

/** 音频播放卡片（修复 OSS 旧域后播放）。 */
@Composable
fun AudioPlayerCard(rawUrl: String?) {
    val url = OssUtils.fixOss(rawUrl)
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember { mutableStateOf(false) }
    DisposableEffect(url) {
        onDispose { player?.release(); player = null; playing = false }
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (url.isNullOrEmpty()) {
                Text("本题无音频", color = MaterialTheme.colorScheme.onSecondaryContainer)
            } else {
                FloatingActionButton(
                    onClick = {
                        try {
                            if (playing) { player?.pause(); playing = false }
                            else {
                                if (player == null) {
                                    val mp = MediaPlayer()
                                    mp.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).setUsage(AudioAttributes.USAGE_MEDIA).build())
                                    mp.setDataSource(url)
                                    mp.prepare()
                                    mp.setOnCompletionListener { playing = false }
                                    player = mp
                                }
                                player?.start(); playing = true
                            }
                        } catch (e: Exception) {
                            playing = false
                        }
                    },
                ) {
                    Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, "播放")
                }
                Text("点击播放", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

/** 交卷结果页。 */
@Composable
fun ResultView(score: Int, totalCount: Int, correctCount: Int, accuracy: Float, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("练习完成", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        Text("$score", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text("总分", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(24.dp))
        Text("答对 $correctCount / $totalCount 题 · 正确率 ${(accuracy * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onBack) { Text("返回") }
    }
}
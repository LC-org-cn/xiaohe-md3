package com.xiaohe.english.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.network.model.QuestionSpecialItem
import kotlinx.coroutines.launch

/** 作业单元做题：题目来自 getCatalogueDetailQuestionInfoList，逐题 submitAnswer，交卷 finishExercises。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkUnitScreen(
    studentRecordCatalogueId: String,
    unitTitle: String,
    studentRecordInfoId: String?,
    onBack: () -> Unit,
    onFinished: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var questions by remember { mutableStateOf(listOf<QuestionSpecialItem>()) }
    var idx by remember { mutableIntStateOf(0) }
    var submitting by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    val answers = remember { mutableStateMapOf<String, String>() } // questionId -> selected option code

    LaunchedEffect(studentRecordCatalogueId) {
        scope.launch {
            loading = true; error = null
            runCatching { ApiClient.work.getCatalogueDetailQuestionInfoList(studentRecordCatalogueId) }
                .onSuccess { r -> questions = r.data?.list ?: emptyList() }
                .onFailure { error = it.message }
            loading = false
        }
    }

    if (finished) {
        SpeakingResultView(0, questions.size, 0, 0f, onBack = onFinished)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(unitTitle) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("加载失败：$error", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onBack) { Text("返回") }
                }
            }
            questions.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("本单元暂无题目") }
            else -> {
                val q = questions[idx.coerceIn(0, questions.size - 1)]
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    LinearProgressIndicator(progress = { (idx + 1f) / questions.size }, modifier = Modifier.fillMaxWidth())
                    Text("第 ${idx + 1} / ${questions.size} 题", style = MaterialTheme.typography.titleMedium)

                    q.questionTitle?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                    q.questionContent?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }

                    // 听力题音频（复用听力模块的播放卡）
                    if (!q.audioUrl.isNullOrEmpty() || !q.exercisesQuestionAudioList.isNullOrEmpty()) {
                        AudioPlayerCard(q.audioUrl ?: q.exercisesQuestionAudioList?.firstOrNull()?.audioUrl)
                    }

                    val opts = q.options ?: emptyList()
                    opts.forEach { opt ->
                        val selected = answers[q.questionId ?: ""] == opt.code
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = selected, role = Role.RadioButton, onClick = {
                                    answers[q.questionId ?: ""] = opt.code ?: ""
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
                                if (idx < questions.size - 1) {
                                    idx++
                                } else if (!submitting) {
                                    submitting = true
                                    scope.launch {
                                        // 逐题提交（与原版 submitAnswer 相同的 body 键）
                                        var submitError: String? = null
                                        for (item in questions) {
                                            val ans = answers[item.questionId ?: ""] ?: continue
                                            val body = buildMap {
                                                put("id", item.questionId ?: "")
                                                put("questionId", item.questionId ?: "")
                                                put("answer", ans)
                                                put("favorite", false)
                                                put("progress", (idx + 1) * 100 / questions.size)
                                                studentRecordInfoId?.let { put("studentRecordInfoId", it) }
                                                put("studentRecordCatalogueId", studentRecordCatalogueId)
                                            }
                                            runCatching { ApiClient.work.submitHomeworkAnswer(body) }
                                                .onFailure { submitError = it.message }
                                        }
                                        if (submitError == null) {
                                            // finishExercises（原版 finishExercises 键：exercisesStudentRecordInfoId / needSave / takeTime / list）
                                            val finBody = buildMap<String, Any> {
                                                studentRecordInfoId?.let { put("exercisesStudentRecordInfoId", it) }
                                                put("needSave", true)
                                                put("takeTime", 0L)
                                                put("exercisesFinishQuestionReqVOList", emptyList<Any>())
                                            }
                                            runCatching { ApiClient.work.finishExercises(finBody) }
                                                .onSuccess { finished = true }
                                                .onFailure { error = it.message }
                                        } else {
                                            error = submitError
                                        }
                                        submitting = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = answers[q.questionId ?: ""] != null && !submitting,
                        ) {
                            Text(if (idx < questions.size - 1) "下一题" else if (submitting) "提交中…" else "交卷")
                        }
                    }
                }
            }
        }
    }
}
package com.xiaohe.english.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import kotlinx.coroutines.launch

/** 作业详情：拉 exercises 记录，展示单元（catalogue）列表。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkDetailScreen(
    homeworkId: Long,
    onBack: () -> Unit,
    onOpenUnit: (studentRecordCatalogueId: String, title: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("作业") }
    var recordInfo by remember { mutableStateOf<com.xiaohe.english.network.model.ExercisesStudentRecordInfo?>(null) }

    LaunchedEffect(homeworkId) {
        scope.launch {
            loading = true; error = null
            // 先取作业标题
            runCatching { ApiClient.work.getHomeworkPublishStudent(homeworkId.toString()) }
                .onSuccess { title = it.data?.homeworkTitle ?: "作业" }
            // 原版流：getExercisesById 创建/取学生练习记录
            runCatching { ApiClient.study.getExercisesById(homeworkId.toString()) }
                .onSuccess { r -> recordInfo = r.data?.exercisesStudentRecordInfo }
                .onFailure { error = it.message }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            else -> {
                val units = recordInfo?.exercisesStudentRecordCatalogueList ?: emptyList()
                if (units.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("该作业没有练习单元") }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(recordInfo?.exercisesName ?: title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "共 ${recordInfo?.exercisesNum ?: 0} 题 · 已完成 ${recordInfo?.finishedExercisesNum ?: 0}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                        items(units) { u ->
                            val done = u.studentFinished == "1"
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onOpenUnit(u.id ?: "", u.exercisesCatalogueTitle ?: "单元") },
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(u.exercisesCatalogueTitle ?: "单元", style = MaterialTheme.typography.titleSmall)
                                        Text("${u.questionNum} 题", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (done) {
                                        AssistChip(onClick = {}, label = { Text("已完成") })
                                    } else {
                                        AssistChip(onClick = {}, label = { Text("待完成") })
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
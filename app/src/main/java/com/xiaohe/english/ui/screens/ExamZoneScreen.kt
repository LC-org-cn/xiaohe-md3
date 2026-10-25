package com.xiaohe.english.ui.screens

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
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import kotlinx.coroutines.launch

/** 考试专区：专区 Tab + 试卷列表（exercisesSpecialType=examination_zone，与原版一致）。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamZoneScreen(
    onBack: () -> Unit,
    onOpenPaper: (exercisesId: String, title: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var zones by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.ExercisesSpecialCatalogue>()) }
    var selectedZone by remember { mutableStateOf(0) }
    var papers by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.ExercisesSpecialCatalogueDetail>()) }
    var papersLoading by remember { mutableStateOf(false) }
    var studentId by remember { mutableStateOf<String?>(null) }
    var starting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            runCatching { ApiClient.user.getStudentInfo() }.onSuccess { studentId = it.data?.id }
            runCatching { ApiClient.material.getExercisesSpecialCatalogue("examination_zone", null, null, 1, 100, "") }
                .onSuccess { zones = it.data?.list ?: emptyList(); if (zones.isNotEmpty()) selectedZone = 0 }
                .onFailure { error = it.message }
            loading = false
        }
    }

    LaunchedEffect(selectedZone, zones.size) {
        val zone = zones.getOrNull(selectedZone) ?: return@LaunchedEffect
        scope.launch {
            papersLoading = true
            runCatching { ApiClient.material.getExercisesSpecialCatalogueDetail(zone.id.toString(), 1, 100, null, null, "", "") }
                .onSuccess { papers = it.data?.list ?: emptyList() }
                .onFailure { papers = emptyList() }
            papersLoading = false
        }
    }

    fun startPaper(studyExercisesInfoId: String, title: String) {
        if (starting || studentId == null) return
        starting = true
        scope.launch {
            runCatching {
                ApiClient.work.createPractice(
                    mapOf("studentId" to (studentId ?: ""), "studyExercisesInfoId" to studyExercisesInfoId),
                )
            }.onSuccess { r ->
                val eid = r.data
                if (!eid.isNullOrEmpty()) onOpenPaper(eid, title)
            }.onFailure { error = it.message }
            starting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("考试专区") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$error", color = MaterialTheme.colorScheme.error) }
            zones.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("暂无考试专区内容") }
            else -> Column(Modifier.fillMaxSize().padding(padding)) {
                ScrollableTabRow(selectedTabIndex = selectedZone) {
                    zones.forEachIndexed { i, z ->
                        Tab(selected = selectedZone == i, onClick = { selectedZone = i }, text = { Text(z.exercisesSpecialName ?: "专区") })
                    }
                }
                if (papersLoading) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (papers.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text("该专区暂无试卷") }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(papers) { p ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { startPaper(p.id ?: "", p.title ?: "试卷") },
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(p.title ?: "试卷", style = MaterialTheme.typography.titleSmall)
                                    p.description?.let { d ->
                                        Spacer(Modifier.height(4.dp))
                                        Text(d.take(80), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("${p.questionCount} 题", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
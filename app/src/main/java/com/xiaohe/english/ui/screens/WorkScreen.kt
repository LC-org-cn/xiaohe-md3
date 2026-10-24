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

/** 作业 Tab：进行中/已完成的作业列表。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScreen(
    onBack: () -> Unit,
    onOpenHomework: (homeworkId: Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.HomeworkPublishStudent>()) }
    var filterDone by remember { mutableStateOf(false) }

    LaunchedEffect(filterDone) {
        scope.launch {
            loading = true; error = null
            runCatching {
                ApiClient.work.getHomeworkPublishStudentPage(
                    1, 50, null,
                    if (filterDone) "completed" else "under_way",
                )
            }.onSuccess { items = it.data?.list ?: emptyList() }
             .onFailure { error = it.message }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的作业") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
                actions = {
                    FilterChip(
                        selected = filterDone,
                        onClick = { filterDone = !filterDone },
                        label = { Text(if (filterDone) "已完成" else "进行中") },
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
            )
        },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$error", color = MaterialTheme.colorScheme.error) }
            items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("暂无作业") }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items) { h ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !filterDone) { onOpenHomework(h.homeworkId) },
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(h.homeworkTitle ?: "作业", style = MaterialTheme.typography.titleMedium)
                            h.endTime?.let { e ->
                                Spacer(Modifier.height(4.dp))
                                Text("截止：$e", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
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

/** 专项练习详情：单元列表 → 点单元进入做题。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueDetailScreen(
    catalogueId: String,
    type: String, // listening | speaking
    onBack: () -> Unit,
    onOpenExercise: (exerciseId: String, type: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.ExercisesSpecialCatalogueDetail>()) }
    var title by remember { mutableStateOf("") }

    LaunchedEffect(catalogueId) {
        scope.launch {
            loading = true; error = null
            runCatching { ApiClient.study.getExercisesSpecialCatalogueDetail(catalogueId, 1, 50, null, null, "", "") }
                .onSuccess { r ->
                    details = r.data?.list ?: emptyList()
                    title = details.firstOrNull()?.title ?: (if (type == "listening") "专项听力" else "专项口语")
                }
                .onFailure { error = it.message }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifEmpty { if (type == "listening") "专项听力" else "专项口语" }) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$error", color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(details) { d ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenExercise(d.id ?: "", type) },
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(d.title ?: "未命名单元", style = MaterialTheme.typography.titleSmall)
                                d.description?.let { t -> Spacer(Modifier.height(2.dp)); Text(t, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            AssistChip(onClick = {}, label = { Text("${d.questionCount} 题") })
                        }
                    }
                }
            }
        }
    }
}
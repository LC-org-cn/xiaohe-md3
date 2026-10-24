package com.xiaohe.english.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import kotlinx.coroutines.launch

/** 自主学习首页：专项练习（听力/口语）。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    onBack: () -> Unit,
    onOpenCatalogueDetail: (detailId: String, type: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var listeningItems by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.ExercisesSpecialCatalogue>()) }
    var speakingItems by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.ExercisesSpecialCatalogue>()) }

    LaunchedEffect(Unit) {
        scope.launch {
            loading = true
            error = null
            val results = mapOf(
                "listening" to runCatching { ApiClient.material.getExercisesSpecialCatalogue("listening", null, null, 1, 50, "") },
                "speaking" to runCatching { ApiClient.material.getExercisesSpecialCatalogue("speaking", null, null, 1, 50, "") },
            )
            results["listening"]?.onSuccess { listeningItems = it.data?.list ?: emptyList() }
            results["listening"]?.onFailure { error = it.message }
            results["speaking"]?.onSuccess { speakingItems = it.data?.list ?: emptyList() }
            results["speaking"]?.onFailure { error = it.message }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自主学习") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
            )
        },
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        error?.let { e ->
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("加载失败：$e", color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (listeningItems.isNotEmpty()) {
                item { SectionHeader("专项听力", Icons.Filled.Headphones) }
                items(listeningItems) { c ->
                    CatalogueCard(c) { onOpenCatalogueDetail(c.id.toString(), "listening") }
                }
            }
            if (speakingItems.isNotEmpty()) {
                item { SectionHeader("专项口语", Icons.Filled.Mic) }
                items(speakingItems) { c ->
                    CatalogueCard(c) { onOpenCatalogueDetail(c.id.toString(), "speaking") }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CatalogueCard(
    c: com.xiaohe.english.network.model.ExercisesSpecialCatalogue,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(c.exercisesSpecialName ?: "未命名", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            c.description?.let { d ->
                Spacer(Modifier.height(4.dp))
                Text(d, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
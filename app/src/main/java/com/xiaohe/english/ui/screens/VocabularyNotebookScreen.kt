package com.xiaohe.english.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import kotlinx.coroutines.launch

/** 单词本：生词列表（分页加载），支持发音播放。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyNotebookScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.VocabularyNotebookItem>()) }
    var page by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }

    fun load(p: Int) {
        scope.launch {
            if (p == 1) loading = true
            runCatching { ApiClient.work.getVocabularyNotebookPage(p, 20) }
                .onSuccess { r ->
                    val list = r.data?.list ?: emptyList()
                    items = if (p == 1) list else items + list
                    hasMore = list.size >= 20
                    page = p
                }
                .onFailure { error = it.message }
            loading = false
        }
    }
    LaunchedEffect(Unit) { load(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单词本") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$error", color = MaterialTheme.colorScheme.error) }
            items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("单词本还是空的：做题时点「收藏」即可收录") }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(items) { _, w ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(w.wordContent ?: "", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.width(8.dp))
                                w.wordPhonetic?.let { ph ->
                                    Text(ph, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.weight(1f))
                                if (w.isStar == "1") {
                                    Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.tertiary)
                                } else {
                                    Icon(Icons.Outlined.StarBorder, null, tint = MaterialTheme.colorScheme.outline)
                                }
                            }
                            w.wordParaphrase?.let { pa ->
                                Spacer(Modifier.height(4.dp))
                                Text(pa, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!w.wordAudioUrl.isNullOrEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                AudioPlayerCard(w.wordAudioUrl)
                            }
                        }
                    }
                }
                if (hasMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            OutlinedButton(onClick = { load(page + 1) }) { Text("加载更多") }
                        }
                    }
                }
            }
        }
    }
}
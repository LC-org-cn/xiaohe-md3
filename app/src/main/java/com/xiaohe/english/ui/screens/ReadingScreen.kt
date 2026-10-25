package com.xiaohe.english.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.network.model.ReadingArticle
import kotlinx.coroutines.launch

/** 拓展阅读：推荐文章列表 → 正文阅读。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var articles by remember { mutableStateOf(listOf<ReadingArticle>()) }
    var selected by remember { mutableStateOf<ReadingArticle?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            loading = true; error = null
            runCatching { ApiClient.material.getReadingArticlePageRecommend(1) }
                .onSuccess { articles = it.data?.list ?: emptyList() }
                .onFailure { error = it.message }
            loading = false
        }
    }

    selected?.let { a ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(a.title ?: "阅读", maxLines = 1) },
                    navigationIcon = { IconButton(onClick = { selected = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
                )
            },
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row {
                    a.category?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                    Spacer(Modifier.width(8.dp))
                    a.difficulty?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                }
                HorizontalDivider()
                Text(a.content ?: "（正文为空）", style = MaterialTheme.typography.bodyLarge, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拓展阅读") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$error", color = MaterialTheme.colorScheme.error) }
            articles.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("暂无推荐阅读") }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(articles) { a ->
                    Card(Modifier.fillMaxWidth().clickable { selected = a }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(a.title ?: "文章", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text((a.content ?: "").take(60) + "…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            Spacer(Modifier.height(6.dp))
                            Row {
                                a.category?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                                Spacer(Modifier.width(8.dp))
                                a.difficulty?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary) }
                            }
                        }
                    }
                }
            }
        }
    }
}
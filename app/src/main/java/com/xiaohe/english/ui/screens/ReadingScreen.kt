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
import com.xiaohe.english.network.model.CurrentTextReadInfo
import com.xiaohe.english.network.model.CurrentTextReadCatalogue
import kotlinx.coroutines.launch

/** 拓展阅读：推荐书集 → 目录 → 正文段落（与原版 Extend 流程一致）。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var books by remember { mutableStateOf(listOf<CurrentTextReadInfo>()) }
    var selectedBook by remember { mutableStateOf<CurrentTextReadInfo?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            loading = true; error = null
            runCatching { ApiClient.material.getReadingArticlePageRecommend(1) }
                .onSuccess { books = it.data?.list ?: emptyList() }
                .onFailure { error = it.message }
            loading = false
        }
    }

    selectedBook?.let { b ->
        ReadingCatalogue(b, onBack = { selectedBook = null })
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
            books.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("暂无推荐阅读") }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(books) { b ->
                    Card(Modifier.fillMaxWidth().clickable { selectedBook = b }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(b.title ?: "阅读", style = MaterialTheme.typography.titleSmall)
                            b.description?.let { d ->
                                Spacer(Modifier.height(4.dp))
                                Text(d.take(80), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row {
                                b.difficultyLevel?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                                Spacer(Modifier.width(8.dp))
                                Text("${b.includeArticleNum ?: 0} 篇", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 书集目录 → 段落正文。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingCatalogue(book: CurrentTextReadInfo, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var catalogues by remember { mutableStateOf(listOf<CurrentTextReadCatalogue>()) }
    var selectedCat by remember { mutableStateOf<CurrentTextReadCatalogue?>(null) }
    var paragraphs by remember { mutableStateOf(listOf<com.xiaohe.english.network.model.CurrentTextReadArticle>()) }
    var articleLoading by remember { mutableStateOf(false) }

    LaunchedEffect(book.id) {
        scope.launch {
            loading = true
            runCatching { ApiClient.material.getReadingArticleCataloguePage(book.id ?: "", 1, 50) }
                .onSuccess { catalogues = it.data?.list ?: emptyList() }
                .onFailure { catalogues = emptyList() }
            loading = false
        }
    }

    if (selectedCat != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedCat?.title ?: "正文", maxLines = 1) },
                    navigationIcon = { IconButton(onClick = { selectedCat = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
                )
            },
        ) { padding ->
            if (articleLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    paragraphs.forEach { p ->
                        Text(p.content ?: "", style = MaterialTheme.typography.bodyLarge)
                        HorizontalDivider()
                    }
                    if (paragraphs.isEmpty()) Text("（正文为空）", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book.title ?: "阅读") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            )
        },
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (catalogues.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text("暂无目录") }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(catalogues) { c ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedCat = c
                            articleLoading = true
                            scope.launch {
                                runCatching { ApiClient.material.getReadingArticleContentByParagraph(c.id ?: "", 1, 50) }
                                    .onSuccess { paragraphs = it.data?.list ?: emptyList() }
                                    .onFailure { paragraphs = emptyList() }
                                articleLoading = false
                            }
                        },
                    ) {
                        Text((c.title ?: "章节") + " · " + c.showNum, Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}
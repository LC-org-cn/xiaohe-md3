package com.xiaohe.english.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.network.model.NotifyMessage
import kotlinx.coroutines.launch

/** 消息中心：全部/未读筛选 + 一键已读。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    onBack: () -> Unit,
    onUnreadCountChange: (Int) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var onlyUnread by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf(listOf<NotifyMessage>()) }
    var selected by remember { mutableStateOf<NotifyMessage?>(null) }

    LaunchedEffect(onlyUnread) {
        scope.launch {
            loading = true; error = null
            runCatching { ApiClient.common.getNotifyMessagePage(if (onlyUnread) false else null, 1, 50) }
                .onSuccess { items = it.data?.list ?: emptyList() }
                .onFailure { error = it.message }
            loading = false
        }
    }

    fun refreshUnread() {
        scope.launch {
            runCatching { ApiClient.common.getUnreadMessageCount() }.onSuccess { onUnreadCountChange(it.data ?: 0) }
        }
    }
    LaunchedEffect(Unit) { refreshUnread() }

    selected?.let { msg ->
        MessageDetailDialog(msg) { selected = null; if (onlyUnread) { items = items.filter { it.id != msg.id } } }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("消息中心") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            runCatching { ApiClient.common.markAllMessagesAsRead() }.onSuccess {
                                items = items.map { it.copy(readStatus = true) }
                                onUnreadCountChange(0)
                            }
                        }
                    }) { Icon(Icons.Filled.DoneAll, "全部已读") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FilterChip(selected = !onlyUnread, onClick = { onlyUnread = false }, label = { Text("全部") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = onlyUnread, onClick = { onlyUnread = true }, label = { Text("未读") })
            }
            when {
                loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                error != null -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text("加载失败：$error", color = MaterialTheme.colorScheme.error) }
                items.isEmpty() -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text(if (onlyUnread) "没有未读消息" else "暂无消息") }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items) { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selected = msg },
                            colors = CardDefaults.cardColors(
                                containerColor = if (!msg.readStatus) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            ),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(msg.title ?: msg.templateTitle ?: "通知", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                Text((msg.content ?: msg.templateContent ?: "").take(60), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageDetailDialog(msg: NotifyMessage, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(msg.title ?: msg.templateTitle ?: "通知") },
        text = {
            Text((msg.content ?: msg.templateContent ?: "") + "\n\n" + ((msg.createTime ?: 0L) / 1000).let { ts ->
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ts * 1000))
            })
        },
        confirmButton = { TextButton(onClick = onClose) { Text("知道了") } },
    )
}
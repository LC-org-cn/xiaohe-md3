package com.xiaohe.english.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xiaohe.english.data.SessionStore
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.network.model.IndexDataResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit, onOpenStudy: () -> Unit = {}, onOpenWork: () -> Unit = {}, onOpenDiagnostics: () -> Unit = {}) {
    var tab by remember { mutableIntStateOf(0) }
    var indexData by remember { mutableStateOf<IndexDataResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            indexData = ApiClient.common.getIndexData("app", 0, "").data
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("小禾英语通") },
                actions = {
                    BadgedBox(badge = { if ((indexData?.unreadMessage) == true) Badge() }) {
                        IconButton(onClick = { /* messages tab */ }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "消息")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("首页") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Filled.School, null) }, label = { Text("学习") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Filled.Person, null) }, label = { Text("我的") })
            }
        },
    ) { padding ->
        when (tab) {
            0 -> HomeTab(indexData, loading, error, padding, onLogout, onOpenWork)
            1 -> StudyTab(padding, onOpenStudy)
            2 -> ProfileTab(padding, onLogout, onOpenDiagnostics)
        }
    }
}

@Composable
private fun HomeTab(
    data: IndexDataResponse?,
    loading: Boolean,
    error: String?,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onLogout: () -> Unit,
    onOpenWork: () -> Unit = {},
) {
    if (loading) {
        Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (error != null) {
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("加载失败", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onLogout) { Text("退出登录") }
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val student = data?.appStudentInfo
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text(student?.name ?: "同学", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        (student?.currentGradeName ?: "") + " · " + (student?.currentBookInfoName ?: "未选择教材"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(12.dp))
                    if (student?.isVip == "1") {
                        AssistChip(onClick = {}, label = { Text("VIP 至 " + (student.vipEndTime ?: "")) })
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenWork() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("我的作业", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.height(4.dp))
                        Text("待完成 " + (data?.homeworkTotal ?: 0) + " 份", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Icon(Icons.Filled.School, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("待完成作业", (data?.homeworkTotal ?: 0).toString(), Modifier.weight(1f))
                StatCard("未读消息", if (data?.unreadMessage == true) "有" else "无", Modifier.weight(1f))
            }
        }
        val chapters = data?.chapterTreeByTypeData ?: emptyList()
        if (chapters.isNotEmpty()) {
            item { SectionHeader("教材章节") }
            items(chapters) { ch ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(ch.chapterTitle ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "词汇 " + ch.chapterWordNum + " · 测验 " + ch.chapterWordExamNumAll,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun StudyTab(padding: androidx.compose.foundation.layout.PaddingValues, onOpenStudy: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("自主学习", style = MaterialTheme.typography.titleLarge)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("专项练习", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(4.dp))
                Text("听力 · 口语（跟读评测）", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Button(onClick = onOpenStudy, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Text("进入自主学习")
        }
    }
}

@Composable
private fun ProfileTab(padding: androidx.compose.foundation.layout.PaddingValues, onLogout: () -> Unit, onOpenDiagnostics: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var username by remember { mutableStateOf<String?>(null) }

    // 拉取用户信息（同原版 Profile 页数据源）
    androidx.compose.runtime.LaunchedEffect(Unit) {
        scope.launch {
            runCatching {
                ApiClient.user.getStudentInfo()
            }.onSuccess { resp ->
                username = resp.data?.username ?: resp.data?.id
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(innerPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("我的", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text(username ?: "同学", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("小禾英语通 · MD3 版", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 续费/购买 —— 与原版 ProfileViewModel.goRenewal 一致：微信分享购买链接
            Button(
                onClick = {
                    val user = username ?: ""
                    val buyUrl = com.xiaohe.english.utils.ShareConfig.BUY_URL + "?username=" + java.net.URLEncoder.encode(user, "UTF-8")
                    com.xiaohe.english.wechat.WeChatManager.shareWebPage(
                        context = context,
                        title = com.xiaohe.english.utils.ShareConfig.SHARE_TITLE,
                        description = com.xiaohe.english.utils.ShareConfig.SHARE_SUMMARY,
                        url = buyUrl,
                        imageUrl = com.xiaohe.english.utils.ShareConfig.SHARE_IMAGE_URL,
                    ) { ok ->
                        scope.launch {
                            snackbar.showSnackbar(if (ok) "已唤起微信" else "分享失败：请确认已安装微信")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("续费 · 微信分享给家长")
            }

            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onOpenDiagnostics, modifier = Modifier.fillMaxWidth()) {
                Text("诊断信息（上报错误用）")
            }
            Button(
                onClick = {
                    com.xiaohe.english.data.SessionStore.clear()
                    onLogout()
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
            ) {
                Text("退出登录")
            }
        }
    }
}

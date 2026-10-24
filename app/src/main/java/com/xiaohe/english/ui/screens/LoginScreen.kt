package com.xiaohe.english.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiaohe.english.data.SessionStore
import com.xiaohe.english.network.ApiClient
import com.xiaohe.english.network.model.LoginRequest
import com.xiaohe.english.network.model.PhoneLoginRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var mode by remember { mutableStateOf("account") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var codeSent by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown -= 1
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(96.dp))
            Text("小禾英语通", style = MaterialTheme.typography.displaySmall)
            Text(
                "Material Design 3 版",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(48.dp))

            if (mode == "account") {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
            } else {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("手机号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = smsCode,
                        onValueChange = { smsCode = it },
                        label = { Text("验证码") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                runCatching { ApiClient.auth.sendSmsCode(mobile, 1) }
                                codeSent = true
                                countdown = 60
                            }
                        },
                        enabled = mobile.length == 11 && countdown == 0,
                    ) {
                        Text(if (countdown > 0) countdown.toString() + " s" else if (codeSent) "重新发送" else "获取验证码")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    loading = true
                    scope.launch {
                        try {
                            val resp = if (mode == "account") {
                                ApiClient.auth.loginByUsername(LoginRequest(username, password))
                            } else {
                                ApiClient.auth.loginByMobile(PhoneLoginRequest(mobile, smsCode))
                            }
                            val token = resp.data
                            if (resp.code != 0) {
                                snackbar.showSnackbar("登录失败：" + (resp.msg ?: "错误码 " + resp.code))
                            } else if (token != null && !token.accessToken.isNullOrEmpty()) {
                                SessionStore.save(token)
                                onLoginSuccess()
                            } else {
                                snackbar.showSnackbar("登录失败：令牌为空")
                            }
                        } catch (e: retrofit2.HttpException) {
                            val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                            snackbar.showSnackbar("登录失败：HTTP " + e.code() + (body?.take(120) ?: ""))
                        } catch (e: Exception) {
                            snackbar.showSnackbar("登录失败：" + (e.message ?: "网络错误"))
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !loading && (if (mode == "account") username.isNotBlank() && password.isNotBlank() else mobile.length == 11 && smsCode.isNotBlank()),
                shape = MaterialTheme.shapes.large,
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("登录", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { mode = if (mode == "account") "sms" else "account" }) {
                Text(if (mode == "account") "使用手机验证码登录" else "使用账号密码登录")
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "继续登录即代表同意《用户协议》与《隐私政策》",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

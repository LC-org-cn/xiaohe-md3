# 小禾英语通 · Material Design 3 重制版

原版 `com.shangheedu.xiaoheapp` v2.0.0 的 MD3 界面重制。**后端与原版完全一致**
（同一套 API、同一请求形状、同一 Token 流程），界面全部按 Material Design 3
（Jetpack Compose Material3 + 动态色）重建。

## 构建

```bash
# 需要 JDK 17+（Gradle 8.11 已内置于 ../tools/gradle-8.11.1）
cd md3app
../tools/gradle-8.11.1/bin/gradle.bat :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

## 架构

- `network/`：8 个 Retrofit 服务（114 端点），请求形状与原版逐字节一致：
  - `tenant-id: 1` + `Authorization: <裸token>` + `Content-Type: application/json` 头
  - Token 存 SharedPreferences `xiaohe_secure_prefs`（与原版同名，换装可保留登录态）
  - 401 时自动 refresh-token 并重放请求
- `speech/`：讯飞语音评测 —— 直接 JNI 驱动原版 APK 提取的 `libmsc.so`
  （QISE* API），参数序列复刻原版 AudioEvaluationManager（sub=ise、plev=pupil、
  ise_unite=entirety、extra_ability=multi_dimension、result_level=complete、
  vad 全 999999、aue=raw、16000Hz）。
- `ui/`：Compose Material3；动态色（Android 12+ 取壁纸色，以下用品牌色）。
- `assets/iflytek` + `jniLibs/{arm64-v8a,armeabi-v7a}/libmsc.so`：原版 SDK 资产。

## 与原版的差异（已确认）

| 功能 | 状态 |
| --- | --- |
| 账号密码登录 | ✅ 完全一致 |
| 手机验证码登录 | ✅ 完全一致 |
| 首页（学生信息/作业数/章节树） | ✅ getIndexData |
| 讯飞口语评测 | ✅ 同一 AppID 同一引擎 |
| 微信登录/分享/支付 | ❌ 重签名后微信 OpenSDK 校验失败（-6），已按用户决定移除 |
| 企业微信客服 | ✅ 浏览器跳转链接（不依赖签名） |
| 阿里云 OSS 崩溃直传 | ❌ 不复刻（泄露 AK/SK 的实现，报告已建议厂商轮换） |

## 风控说明

原版客户端无设备指纹（无 IMEI/ANDROID_ID 采集）、无自定义 UA、无签名自校验。
本客户端请求与原版不可区分（同头、同体、同端点），服务端无法区分新旧客户端，
不会触发额外风控。Token 与原版同存储位，登录态互通。

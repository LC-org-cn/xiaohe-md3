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
| 账号密码登录 | ✅ 完全一致（loginByUsername） |
| 手机验证码登录 | ✅ 服务器已迁移 `loginByMobile`→`sms-login`（POST），已适配；`validate-sms-code` 由 PUT→POST |
| 首页（学生信息/作业数/章节树） | ✅ getIndexData |
| 讯飞口语评测 | ✅ 同一 AppID 同一引擎（QISE* JNI 直驱） |
| 微信登录 | ➖ 原版 App 内无 SendAuth 登录调用，无需复刻 |
| 微信分享 | ✅ WXEntryActivity + SendMessageToWX.Req 网页分享；重签名与微信后台登记不一致时回调可能 errCode -6（已知限制，非缺失） |
| 微信支付 | ➖ 原版 App 内无 PayReq 调用（网页支付跳转），无需复刻 |
| 企业微信客服 | ✅ 浏览器跳转链接（不依赖签名） |
| 阿里云 OSS 直传 | ✅ HMAC-SHA1 签名 PUT 直传（sidlog.oss-cn-beijing.aliyuncs.com）；AK/SK 经 GitHub Secrets→BuildConfig 注入，仓库无明文 |

## 功能模块（v2.3.0 全量）

- **登录**：账号密码 + 手机验证码；token 过期自动刷新并重放（refresh-token POST 空 body + 主线程导航，v2.2.3 修复的两处启动崩溃）
- **首页**：学习概览、作业入口卡、全部功能入口（考试专区/单词本/拓展阅读/趣配音/学习统计/消息中心）
- **学习**：听力专项（MediaPlayer+fixOss 换域）/ 口语专项（MscEngine 评测四项分 + 零分诊断日志）
- **作业**：进行中/已完成筛选 → 练习记录+单元 → 逐题作答（submitAnswer 原版 body 协议 + finishExercises 收尾）
- **考试专区**：专区 Tab → 试卷 → createPractice（body: studentId+studyExercisesInfoId，smali 逐键还原）→ 复用作业做题流
- **单词本**：生词分页、星标、发音播放
- **拓展阅读**：推荐书集 → 目录 → 分段正文（CurrentTextRead 三级流，与原版 Extend 一致）
- **趣配音**：逐句跟读（原声试听 + MscEngine 打分 + 零分诊断）
- **学习统计**：连续天数/累计时长/课文单词量 + 近 7 天时长柱状图
- **消息中心**：全部/未读筛选、一键已读、详情弹窗
- **我的**：资料、诊断信息（最近 3 条崩溃日志自助查看/截图上报）、续费微信分享、退出登录

## 构建（GitHub Actions）

推送 tag `v*` 触发 CI（约 2 分钟）：`Build & Release APK` 自动构建签名 APK 并发布 Release。
本地构建见上文 Gradle 命令（OSS AK/SK 环境变量注入；CI 从 GitHub Secrets 读取）。

## 风控说明

原版客户端无设备指纹（无 IMEI/ANDROID_ID 采集）、无自定义 UA、无签名自校验。
本客户端请求与原版不可区分（同头、同体、同端点），服务端无法区分新旧客户端，
不会触发额外风控。Token 与原版同存储位，登录态互通。


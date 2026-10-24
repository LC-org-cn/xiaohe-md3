package com.xiaohe.english.utils

/** OSS 域名迁移工具 —— 与原版 OssUtils.kt 一致：旧 zos.ctyun.cn 域替换为新 zos7 域。 */
object OssUtils {
    private const val NEW_OSS_PREFIX = "https://zos7.shangheedu.com/"
    private const val OLD_OSS_PREFIX = "https://shandong-20.zos.ctyun.cn/"

    fun fixOss(url: String?): String {
        val normalized = (url ?: "").trim()
        if (normalized.isEmpty()) return ""
        val newPrefix = NEW_OSS_PREFIX.trimEnd('/') + "/"
        val oldPrefix = OLD_OSS_PREFIX.trimEnd('/') + "/"
        if (normalized.startsWith(newPrefix, ignoreCase = true)) return normalized
        if (!normalized.startsWith(oldPrefix, ignoreCase = true)) return normalized
        return newPrefix + normalized.substring(oldPrefix.length)
    }
}
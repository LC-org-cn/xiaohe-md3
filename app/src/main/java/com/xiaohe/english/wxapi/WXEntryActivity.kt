package com.xiaohe.english.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.xiaohe.english.wechat.WeChatManager

/** 微信回调页：包名 wxapi 固定，微信 SDK 反射寻址。逻辑与原版一致。 */
class WXEntryActivity : Activity(), IWXAPIEventHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WeChatManager.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        WeChatManager.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq?) {}

    override fun onResp(resp: BaseResp?) {
        val tag = "WXEntryActivity"
        when (resp?.errCode) {
            BaseResp.ErrCode.ERR_OK -> Log.d(tag, "分享成功")
            BaseResp.ErrCode.ERR_USER_CANCEL -> Log.d(tag, "用户取消分享")
            BaseResp.ErrCode.ERR_AUTH_DENIED -> Log.e(tag, "分享被拒绝: ${resp.errStr}")
            else -> Log.d(tag, "其他类型回调: ${resp?.type}")
        }
        finish()
    }
}
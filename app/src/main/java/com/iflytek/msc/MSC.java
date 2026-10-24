package com.iflytek.msc;

/**
 * Direct binding to the JNI glue exported by libmsc.so
 * (Java_com_iflytek_msc_MSC_* symbols are provided by the vendor library itself).
 * Signatures extracted from the original APK's com/iflytek/msc/MSC.smali.
 */
public class MSC {
    static {
        System.loadLibrary("msc");
    }

    // ---- login ----
    public static native int QMSPLogin(byte[] user, byte[] pwd, byte[] params);
    public static native int QMSPLogout();

    // ---- ISE (speech evaluation) ----
    public static native char[] QISESessionBegin(byte[] text, byte[] params, MSCSessionInfo info);
    public static native int QISETextPut(char[] sessionId, byte[] text, byte[] params);
    public static native int QISEAudioWrite(char[] sessionId, byte[] audio, int len, int audioStatus, MSCSessionInfo info);
    public static native byte[] QISEGetResult(char[] sessionId, MSCSessionInfo info);
    public static native int QISESessionEnd(char[] sessionId, byte[] hint);
    public static native int QISEGetParam(char[] sessionId, byte[] param, MSCSessionInfo info);

    // ---- misc ----
    public static native int DebugLog(boolean enable);
}

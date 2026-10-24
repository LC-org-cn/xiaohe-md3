package com.iflytek.msc;

/** Session status holder matching the native struct used by libmsc.so JNI glue. */
public class MSCSessionInfo {
    public byte[] buffer;
    public int buflen;
    public int epstatues;
    public int errorcode;
    public int rsltstatus;
    public int sesstatus;
}

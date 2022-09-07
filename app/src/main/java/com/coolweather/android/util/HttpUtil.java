package com.coolweather.android.util;


import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    /**
     *
     * @param address 请求地址
     * @param callback 最终的请求结果回调到callback中
     */
    public static void sendOKHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}

package com.jivosite.sdk.network.retrofitimport com.jivosite.sdk.model.storage.SharedStorageimport okhttp3.Interceptorimport okhttp3.Responseimport java.net.URL/** * Created on 4/20/21. * * @author Alexander Tavtorkin (av.tavtorkin@gmail.com) */class ChangeUrlInterceptor(private val storage: SharedStorage) : Interceptor {    companion object {        const val URL = "url"        const val API = "api"        const val NODE = "node"        const val TELEMETRY = "telemetry"        const val SDK = "sdk"        const val PUSH = "push"        private const val BASE_HOST = "jivosite.com"    }    override fun intercept(chain: Interceptor.Chain): Response {        val originalRequest = chain.request()        val oldUrl = originalRequest.url        val newUrl = oldUrl.newBuilder().apply {            originalRequest.headers(URL).forEach {                when (it) {                    SDK -> host("sdk.${storage.host.ifBlank { BASE_HOST }}")                    API -> host(storage.apiHost)                    NODE -> {                        val url = URL("https://${storage.chatserverHost}")                        host(url.host)                        val port = url.port                        if (port != -1) {                            port(url.port)                        }                    }                    TELEMETRY -> host("telemetry.${storage.host.ifBlank { BASE_HOST }}")                    PUSH -> host("push.${storage.host.ifBlank { BASE_HOST }}")                }            }        }.build()        val newRequestBuilder = originalRequest.newBuilder()        newRequestBuilder.removeHeader(URL)        return chain.proceed(newRequestBuilder.url(newUrl).build())    }}
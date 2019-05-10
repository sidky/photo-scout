package com.github.sidky.data.apollo

import okhttp3.Interceptor
import okhttp3.Response

class AuthenticatorInterceptor(private val tokenProvider: TokenProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.token()
        val updated = chain.request().newBuilder().addHeader("X-Auth-Token", token).build()
        return chain.proceed(updated)
    }
}
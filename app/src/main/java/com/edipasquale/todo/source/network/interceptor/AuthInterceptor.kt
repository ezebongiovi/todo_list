package com.edipasquale.todo.source.network.interceptor

import android.content.Context
import com.edipasquale.todo.util.PrefUtils
import okhttp3.Interceptor
import okhttp3.Response

private const val CONST_HEADER_AUTH = "Authorization"

class AuthInterceptor(
    private val _context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = PrefUtils.getAccessToken(_context)

        return chain.proceed(
            chain.request().newBuilder()
                .addHeader(CONST_HEADER_AUTH, accessToken ?: "")
                .build()
        )
    }
}

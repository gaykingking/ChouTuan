package com.example.myapplication.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class MultiTenantHeaderInterceptor(
    private val tenantIdProvider: () -> String,
    private val clientRoleProvider: () -> String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header(HEADER_APP_ID, DEFAULT_APP_ID)
            .header(HEADER_TENANT_ID, tenantIdProvider())
            .header(HEADER_CLIENT_ROLE, clientRoleProvider())
            .header(HEADER_COMMISSION_POLICY, POLICY_ZERO_COMMISSION)
            .method(original.method, original.body)
            .build()
        return chain.proceed(request)
    }

    companion object {
        const val HEADER_APP_ID = "X-App-Id"
        const val HEADER_TENANT_ID = "X-Tenant-Id"
        const val HEADER_CLIENT_ROLE = "X-Client-Role"
        const val HEADER_COMMISSION_POLICY = "X-Commission-Policy"

        const val DEFAULT_APP_ID = "com.choutuan.app"
        const val POLICY_ZERO_COMMISSION = "ZERO_COMMISSION"
    }
}

object RetrofitClientFactory {

    private const val DEFAULT_TIMEOUT_SECONDS = 15L

    fun createOkHttpClient(
        tenantId: String = "T1001",
        clientRole: String = "CONSUMER"
    ): OkHttpClient {
        return createOkHttpClient(
            tenantIdProvider = { tenantId },
            clientRoleProvider = { clientRole }
        )
    }

    fun createOkHttpClient(
        tenantIdProvider: () -> String,
        clientRoleProvider: () -> String
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                MultiTenantHeaderInterceptor(
                    tenantIdProvider = tenantIdProvider,
                    clientRoleProvider = clientRoleProvider
                )
            )
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    fun createRetrofit(
        baseUrl: String,
        tenantId: String = "T1001",
        clientRole: String = "CONSUMER"
    ): Retrofit {
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(createOkHttpClient(tenantId = tenantId, clientRole = clientRole))
            .build()
    }

    fun <T> createService(
        baseUrl: String,
        serviceClass: Class<T>,
        tenantId: String = "T1001",
        clientRole: String = "CONSUMER"
    ): T {
        return createRetrofit(
            baseUrl = baseUrl,
            tenantId = tenantId,
            clientRole = clientRole
        ).create(serviceClass)
    }
}

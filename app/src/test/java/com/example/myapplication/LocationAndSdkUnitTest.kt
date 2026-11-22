package com.example.myapplication

import com.example.myapplication.data.remote.MultiTenantHeaderInterceptor
import com.example.myapplication.data.remote.RetrofitClientFactory
import com.example.myapplication.sdk.AMapSphericalUtils
import com.example.myapplication.sdk.LocationAndPaymentSdk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.http.GET

class LocationAndSdkUnitTest {

    private interface DummyApiService {
        @GET("health")
        fun ping(): Call<String>
    }

    @Test
    fun calculateDistanceKm_samePoint_returnsZero() {
        val distance = AMapSphericalUtils.calculateDistanceKm(39.9042, 116.4074, 39.9042, 116.4074)
        assertEquals(0.0, distance, 1e-6)
    }

    @Test
    fun calculateDistanceKm_knownCoordinates_returnsAccurateSphericalDistance() {
        val distanceOneDegreeLat = AMapSphericalUtils.calculateDistanceKm(0.0, 0.0, 1.0, 0.0)
        assertEquals(111.195, distanceOneDegreeLat, 0.1)

        val beijingToShanghai = AMapSphericalUtils.calculateDistanceKm(39.9042, 116.4074, 31.2304, 121.4737)
        assertTrue(beijingToShanghai in 1050.0..1090.0)
    }

    @Test
    fun estimateSafeDeliveryMinutes_appliesGentleBufferMinutes() {
        assertEquals(10, AMapSphericalUtils.estimateSafeDeliveryMinutes(0.0))
        assertEquals(40, AMapSphericalUtils.estimateSafeDeliveryMinutes(10.0))
        assertEquals(20, AMapSphericalUtils.estimateSafeDeliveryMinutes(5.0, 30.0))
    }

    @Test
    fun locationAndPaymentSdk_getSdkStatus_returnsReadyMessage() {
        val sdk = LocationAndPaymentSdk()
        assertEquals("AMapSphericalUtils & Payment Gateway SDK ready", sdk.getSdkStatus())
    }

    @Test
    fun retrofitClientFactory_createOkHttpClient_configuresTimeoutsAndInterceptor() {
        val client = RetrofitClientFactory.createOkHttpClient("T2026", "MERCHANT")
        assertEquals(15_000, client.connectTimeoutMillis())
        assertEquals(15_000, client.readTimeoutMillis())
        assertEquals(15_000, client.writeTimeoutMillis())
        assertTrue(client.interceptors().any { it is MultiTenantHeaderInterceptor })
    }

    @Test
    fun retrofitClientFactory_createRetrofit_normalizesBaseUrl() {
        val retrofitNoSlash = RetrofitClientFactory.createRetrofit("http://localhost:8080")
        assertEquals("http://localhost:8080/", retrofitNoSlash.baseUrl().toString())

        val retrofitWithSlash = RetrofitClientFactory.createRetrofit("http://localhost:8080/")
        assertEquals("http://localhost:8080/", retrofitWithSlash.baseUrl().toString())
    }

    @Test
    fun retrofitClientFactory_createService_buildsServiceProxy() {
        val service = RetrofitClientFactory.createService(
            baseUrl = "http://localhost:8080",
            serviceClass = DummyApiService::class.java,
            tenantId = "T1001",
            clientRole = "CONSUMER"
        )
        assertNotNull(service)
    }
}

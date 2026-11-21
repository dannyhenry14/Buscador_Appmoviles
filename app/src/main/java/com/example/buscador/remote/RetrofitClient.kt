package com.example.buscador.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente centralizado para la configuración de Retrofit hacia Xano.
 */
object RetrofitClient {
    private const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:z2fnM-14/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: CenamadApi by lazy {
        retrofit.create(CenamadApi::class.java)
    }
}

/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.dagger

import android.util.Base64
import com.squareup.moshi.Moshi
import com.vandyke.sia.BuildConfig
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.remote.*
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.ConnectException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ApiModule {
    @Provides
    @Singleton
    fun provideSiaApi(): SiaApi {
        /* always return the actual api if it's a release build, so I don't accidentally release an update that uses the mock api */
        if (BuildConfig.DEBUG && false)
            return MockSiaApi()

        val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                /* no read timeout because some Sia API calls can take a long time to return/respond */
                .readTimeout(0, TimeUnit.MILLISECONDS)
                /* check for a different timeout on this endpoint, for requests made to endpoints other than siad */
                .addInterceptor {
                    val request = it.request()
                    val readTimeout = request.header("READ_TIMEOUT")?.toIntOrNull()
                            ?: it.readTimeoutMillis()
                    val newRequest = request.newBuilder()
                            .removeHeader("READ_TIMEOUT")
                            .build()
                    return@addInterceptor it.withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
                            .proceed(newRequest)
                }
                /* check non-success responses for Sia-specific errors and throw the appropriate exception */
                .addInterceptor {
                    val original: Request = it.request()
                    val request: Request = original.newBuilder()
                            .header("User-agent", "Sia-Agent")
                            .header("Authorization", "Basic " + Base64.encodeToString(":${Prefs.apiPassword}".toByteArray(), Base64.NO_WRAP))
                            .method(original.method(), original.body())
                            .build()
                    try {
                        val response = it.proceed(request)
                        if (!response.isSuccessful) {
                            val errorMsg = response.peekBody(256).string()
                            val e = SiaException.fromError(request, errorMsg)
                            if (e != null) {
                                throw e
                            }
                        }
                        return@addInterceptor response
                    } catch (e: ConnectException) {
                        if (e.message == "Failed to connect to localhost/127.0.0.1:9980")
                            throw SiadNotRunning()
                        else
                            throw e
                    }
                }


        // TODO: if the first (and only first) Retrofit usage on a page is an error,
        // it causes significant UI lag. No idea why. Seems to occur regardless of what the error is.
        // Since it's per-page, maybe that means it's actually per-repository?

        // Actually, it might always be the first request, regardless of whether it's an error or not.
        // Maybe retrofit is "warming up"? But it wasn't like that before. Not sure exactly when
        // it started doing this.

        // Based off my experience when deving QDrops, I think it's caused by the KotlinJsonAdapterFactory

        // TODO: check if it does it with the ShapeShift API
        return Retrofit.Builder()
                .addConverterFactory(
                        MoshiConverterFactory.create(
                                Moshi.Builder()
                                        .add(BigDecimalAdapter())
                                        .add(SiaJsonAdapters())
//                                            .add(KotlinJsonAdapterFactory())
                                        .build()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//.createWithScheduler(Schedulers.io()))
                .client(clientBuilder.build())
                .baseUrl("http://localhost:9980/")
                .build()
                .create(SiaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideShapeShiftApi(): ShapeShiftApi {
        return Retrofit.Builder()
                .addConverterFactory(
                        MoshiConverterFactory.create(
                                Moshi.Builder()
                                        .add(BigDecimalAdapter())
//                                        .add(KotlinJsonAdapterFactory())
                                        .build()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//.createWithScheduler(Schedulers.io()))
                .baseUrl("https://shapeshift.io/")
                .build()
                .create(ShapeShiftApi::class.java)
    }
}
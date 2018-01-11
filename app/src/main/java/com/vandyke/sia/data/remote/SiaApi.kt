/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.remote

import android.util.Base64
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.gateway.GatewayData
import com.vandyke.sia.data.models.renter.*
import com.vandyke.sia.data.models.txpool.FeeData
import com.vandyke.sia.data.models.wallet.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

interface SiaApiInterface {
    /* daemon API */
    @GET("daemon/stop")
    fun daemonStop(): Completable

    /* wallet API */
    @GET("wallet")
    fun wallet(): Single<WalletData>

    @POST("wallet/siacoins")
    fun walletSiacoins(@Query("amount") amount: String, @Query("destination") destination: String): Completable

    @GET("wallet/address")
    fun walletAddress(): Single<AddressData>

    @GET("wallet/addresses")
    fun walletAddresses(): Single<AddressesData>

    @GET("wallet/seeds")
    fun walletSeeds(@Query("dictionary") dictionary: String = "english"): Single<SeedsData>

    @POST("wallet/sweep/seed")
    fun walletSweepSeed(@Query("dictionary") dictionary: String, @Query("seed") seed: String): Completable

    @GET("wallet/transactions")
    fun walletTransactions(@Query("startheight") startHeight: String = "0", @Query("endheight") endHeight: String = "2000000000"): Single<TransactionsData>

    @POST("wallet/init")
    fun walletInit(@Query("encryptionpassword") password: String, @Query("dictionary") dictionary: String, @Query("force") force: Boolean): Single<WalletInitData>

    @POST("wallet/init/seed")
    fun walletInitSeed(@Query("encryptionpassword") password: String, @Query("dictionary") dictionary: String, @Query("seed") seed: String, @Query("force") force: Boolean): Completable

    @POST("wallet/lock")
    fun walletLock(): Completable

    @POST("wallet/unlock")
    fun walletUnlock(@Query("encryptionpassword") password: String): Completable

    @POST("wallet/changepassword")
    fun walletChangePassword(@Query("encryptionpassword") password: String, @Query("newpassword") newPassword: String): Completable

    @GET
    fun getScPrice(@Url url: String = "http://www.coincap.io/page/SC"): Single<ScValueData>

    /* renter API */
    @GET("renter")
    fun renter(): Single<RenterData>

    @POST("renter")
    fun renter(@Query("funds") funds: BigDecimal, @Query("hosts") hosts: Int, @Query("period") period: Int, @Query("renewwindow") renewwindow: Int): Completable

    @GET("renter/contracts")
    fun renterContracts(): Single<ContractsData>

    @GET("renter/downloads")
    fun renterDownloads(): Single<DownloadsData>

    @GET("renter/files")
    fun renterFiles(): Single<RenterFilesData>

    @GET("renter/prices")
    fun renterPrices(): Single<PricesData>

    @POST("renter/rename/{siapath}")
    fun renterRename(@Path("siapath") siapath: String, @Query("newsiapath") newSiaPath: String): Completable

    @POST("renter/delete/{siapath}")
    fun renterDelete(@Path("siapath") siapath: String): Completable

    @POST("renter/upload/{siapath}")
    fun renterUpload(@Path("siapath") siapath: String, @Query("source") source: String, @Query("datapieces") dataPieces: Int, @Query("paritypieces") parityPieces: Int): Completable

    @GET("renter/download/{siapath}")
    fun renterDownload(@Path("siapath") siapath: String, @Query("destination") destination: String): Completable

    @GET("renter/downloadasync/{siapath}")
    fun renterDownloadAsync(@Path("siapath") siapath: String, @Query("destination") destination: String): Completable

    /* gateway API */
    @GET("gateway")
    fun gateway(): Single<GatewayData>

    /* consensus API */
    @GET("consensus")
    fun consensus(): Single<ConsensusData>

    /* transactionpool API */
    @GET("tpool/fee")
    fun txPoolFee(): Single<FeeData>
}

val siaApi: SiaApiInterface = MockSiaApi()//SiaApi.buildApi()

object SiaApi {
    fun buildApi(): SiaApiInterface {

        val clientBuilder = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // no timeout because some Sia API calls can take a long time to return
//                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor({
                    val original: Request = it.request()
                    val request: Request = original.newBuilder()
                            .header("User-agent", "Sia-Agent")
                            .header("Authorization", "Basic " + Base64.encodeToString(":${Prefs.apiPassword}".toByteArray(), Base64.NO_WRAP))
                            .method(original.method(), original.body())
                            .build()
                    return@addInterceptor it.proceed(request)
                })

        return Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .baseUrl("http://localhost:9980/")
                .build()
                .create(SiaApiInterface::class.java)
    }
}

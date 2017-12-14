/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.data.remote

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import vandyke.siamobile.data.data.consensus.ConsensusData
import vandyke.siamobile.data.data.explorer.ExplorerData
import vandyke.siamobile.data.data.explorer.ExplorerHashData
import vandyke.siamobile.data.data.renter.RenterFilesData
import vandyke.siamobile.data.data.wallet.*
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
    fun walletSeeds(@Query("dictionary") dictionary: String): Single<SeedsData>

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
    fun getScPrice(@Url url: String = "http://www.coincap.io/page/SC"): Single<ScPriceData>

    @GET
    fun getSiaTechExplorerHash(@Url url: String): Single<ExplorerHashData>

    @GET
    fun getSiaTechExplorer(@Url url: String = "http://explore.sia.tech/explorer"): Single<ExplorerData>

    /* renter API */
    @GET("renter/files")
    fun renterFiles(): Single<RenterFilesData>

    @POST("renter/delete/{siapath}")
    fun renterDelete(@Path("siapath") siapath: String): Completable

    @POST("renter/upload/{siapath}")
    fun renterUpload(@Path("siapath") siapath: String, @Query("source") source: String, @Query("datapieces") dataPieces: Int, @Query("paritypieces") parityPieces: Int): Completable

    /* consensus API */
    @GET("consensus")
    fun consensus(): Single<ConsensusData>
}

var siaApi: SiaApiInterface = SiaApi.buildApi()

object SiaApi {
    fun buildApi(): SiaApiInterface {
        val clientBuilder = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .addInterceptor({
                    val original: Request = it.request()
                    val request: Request = original.newBuilder()
                            .header("User-agent", "Sia-Agent")
                            .method(original.method(), original.body())
                            .build()
                    return@addInterceptor it.proceed(request)
                })

        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .baseUrl("http://localhost:9980/")
                .build()
                .create(SiaApiInterface::class.java)
    }

    fun rebuildApi() {
        siaApi = buildApi()
    }
}

/* the below extensions are used to simplify subscribing to Singles/Completables from the above sia api */
fun <T> Single<T>.subscribeApi(onNext: (T) -> Unit, onError: (SiaError) -> Unit) {
    this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, {
                if (it is SiaError)
                    onError(it)
                else
                    onError(SiaError(it))
            })
}

fun Completable.subscribeApi(onNext: () -> Unit, onError: (SiaError) -> Unit) {
    this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, {
                if (it is SiaError)
                    onError(it)
                else
                    onError(SiaError(it))
            })
}
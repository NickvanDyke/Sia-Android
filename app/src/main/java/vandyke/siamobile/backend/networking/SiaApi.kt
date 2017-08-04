package vandyke.siamobile.backend.networking

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import vandyke.siamobile.backend.coldstorage.ExplorerHashModel
import vandyke.siamobile.backend.models.*
import vandyke.siamobile.prefs

interface SiaApiInterface {
    @GET("wallet")
    fun getWallet(): Call<WalletModel>

    @GET("wallet/siacoins")
    fun sendSiacoins(@Query("amount") amount: String, @Query("destination") destination: String): Call<Unit>

    @GET("wallet/address")
    fun getAddress(): Call<AddressModel>

    @GET("wallet/addresses")
    fun getAddresses(): Call<AddressesModel>

    @GET("wallet/seeds")
    fun getSeeds(@Query("dictionary") dictionary: String): Call<SeedsModel>

    @POST("wallet/sweep/seed")
    fun sweepSeed(@Query("dictionary") dictionary: String, @Query("seed") seed: String): Call<Unit>

    @GET("wallet/transactions")
    fun getTransactions(@Query("startheight") startHeight: String, @Query("endheight") endHeight: String): Call<TransactionsModel>

    @POST("wallet/init")
    fun initWallet(@Query("encryptionpassword") password: String, @Query("dictionary") dictionary: String, @Query("force") force: Boolean): Call<WalletInitModel>

    @POST("wallet/init/seed")
    fun initWalletSeed(@Query("encryptionpassword") password: String, @Query("dictionary") dictionary: String, @Query("seed") seed: String, @Query("force") force: Boolean): Call<Unit>

    @POST("wallet/lock")
    fun lockWallet(): Call<Unit>

    @POST("wallet/unlock")
    fun unlockWallet(@Query("encryptionpassword") password: String): Call<Unit>

    @POST("wallet/changepassword")
    fun changeWalletPassword(@Query("encryptionpassword") password: String, @Query("newpassword") newPassword: String): Call<Unit>

    @GET
    fun getScPrice(@Url url: String): Call<ScPriceModel>

    @GET
    fun getSiaTechExplorerHash(@Url url: String): Call<ExplorerHashModel>

    @GET
    fun getSiaTechExplorer(@Url url: String): Call<ExplorerModel>

    @GET("consensus")
    fun getConsensus(): Call<ConsensusModel>
}

private var siaApi: SiaApiInterface = SiaApi.buildApi()

object SiaApi {
    fun buildApi(): SiaApiInterface {
        val client: OkHttpClient = OkHttpClient.Builder()
                .addInterceptor({
                    val original: Request = it.request()
                    val request: Request = original.newBuilder()
                            .header("User-agent", "Sia-Agent")
                            .header("Authorization", "Basic " + Base64.encodeToString(":${prefs.apiPass}".toByteArray(), 0).trim())
//                            .url(original.url().toString().replace(Regex("""http://(\d+\.\d+\.\d+\.\d+|localhost):\d+/"""), "http://${prefs.address}/"))
                            .method(original.method(), original.body())
                            .build()
                    return@addInterceptor it.proceed(request)
                }).build()

        return Retrofit.Builder()
                .baseUrl("http://${prefs.address}/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(SiaApiInterface::class.java)
    }
    fun rebuildApi() { siaApi = buildApi() }
}

object Wallet {
    fun wallet(callback: Callback<WalletModel>) = siaApi.getWallet().enqueue(callback)
    fun send(amount: String, destination: String, callback: Callback<Unit>) = siaApi.sendSiacoins(amount, destination).enqueue(callback)
    fun address(callback: Callback<AddressModel>) = siaApi.getAddress().enqueue(callback)
    fun addresses(callback: Callback<AddressesModel>) = siaApi.getAddresses().enqueue(callback)
    fun seeds(dictionary: String, callback: Callback<SeedsModel>) = siaApi.getSeeds(dictionary).enqueue(callback)
    fun sweep(dictionary: String, seed: String, callback: Callback<Unit>) = siaApi.sweepSeed(dictionary, seed).enqueue(callback)
    fun transactions(callback: Callback<TransactionsModel>) = siaApi.getTransactions("0", "2000000000").enqueue(callback)
    fun init(password: String, dictionary: String, force: Boolean, callback: Callback<WalletInitModel>) = siaApi.initWallet(password, dictionary, force).enqueue(callback)
    fun initSeed(password: String, dictionary: String, seed:String, force: Boolean, callback: Callback<Unit>) = siaApi.initWalletSeed(password, dictionary, seed, force).enqueue(callback)
    fun lock(callback: Callback<Unit>) = siaApi.lockWallet().enqueue(callback)
    fun unlock(password: String, callback: Callback<Unit>) = siaApi.unlockWallet(password).enqueue(callback)
    fun changePassword(password: String, newPassword: String, callback: Callback<Unit>) = siaApi.changeWalletPassword(password, newPassword).enqueue(callback)
    fun scPrice(callback: Callback<ScPriceModel>) = siaApi.getScPrice("http://www.coincap.io/page/SC").enqueue(callback)
}

object Consensus {
    fun consensus(callback: Callback<ConsensusModel>) = siaApi.getConsensus().enqueue(callback)
}

object Explorer {
    fun siaTech(callback: Callback<ExplorerModel>) = siaApi.getSiaTechExplorer("http://explore.sia.tech/explorer").enqueue(callback)
    fun siaTechHash(hash: String, callback: Callback<ExplorerHashModel>) = siaApi.getSiaTechExplorerHash("http://explore.sia.tech/explorer/hashes/$hash").enqueue(callback)
    fun siaTechHashBlocking(hash: String) = siaApi.getSiaTechExplorerHash("http://explore.sia.tech/explorer/hashes/$hash")
}
package vandyke.siamobile.api.networking

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import vandyke.siamobile.api.models.ConsensusModel
import vandyke.siamobile.api.models.TransactionsModel
import vandyke.siamobile.api.models.WalletModel
import vandyke.siamobile.prefs

interface SiaApiInterface {
    @GET("wallet")
    fun getWallet(): Call<WalletModel>

    @GET("consensus")
    fun getConsensus(): Call<ConsensusModel>

    @GET("wallet/transactions")
    fun getTransactions(@Query("startheight") startHeight: String, @Query("endheight") endHeight: String): Call<TransactionsModel>
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
    fun transactions(callback: Callback<TransactionsModel>) = siaApi.getTransactions("0", "2000000000").enqueue(callback)
}

object Consensus {
    fun consensus(callback: Callback<ConsensusModel>) = siaApi.getConsensus().enqueue(callback)
}
package vandyke.siamobile.api.networking

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import vandyke.siamobile.api.WalletModel

interface SiaApi {
    @GET("/walletModel")
    fun getWallet(): Call<WalletModel>
}

private val siaApi: SiaApi by lazy {
    Retrofit.Builder()
            .baseUrl("")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SiaApi::class.java)
}

object Wallet {
    fun wallet(callback: Callback<WalletModel>) {
        siaApi.getWallet().enqueue(callback)
    }
}

fun <T> callback(success: (Response<T>) -> Unit, error: (t: Throwable) -> Unit): Callback<T> {
    return object : Callback<T> {
        override fun onResponse(call: Call<T>, response: retrofit2.Response<T>) {
            if (response.isSuccessful)
                success(response)
            else
                error
        }
        override fun onFailure(call: Call<T>, t: Throwable) {
            error(t)
        }
    }
}
package vandyke.siamobile.backend.networking

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SiaCallback<T>(val onSuccess: (T) -> Unit, val onError: (SiaError) -> Unit) : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful)
            onSuccess(response.body()!!)
        else {
            onError(SiaError(response.errorBody()?.string() ?: ""))
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        onError(SiaError(t))
    }

    // TODO: I'm assuming params in onResponse and onFailure aren't null, not sure if they actually can be. will find out
    // the object in response.body() might also be nullable I'm not sure
}
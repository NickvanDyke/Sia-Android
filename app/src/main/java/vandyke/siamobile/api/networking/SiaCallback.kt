package vandyke.siamobile.api.networking

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SiaCallback<T>(val onSuccess: (Response<T>) -> Unit, val onError: (SiaError<T>) -> Unit) : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful)
            onSuccess(response)
        else {
            onError(SiaError(response))
        }
    }

    override fun onFailure(call: Call<T>?, t: Throwable) {
        onError(SiaError(t))
    }

    // TODO: I'm assuming params in onResponse and onFailure aren't null, not sure if they actually can be. will find out
}
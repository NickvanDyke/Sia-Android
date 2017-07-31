package vandyke.siamobile.api.networking

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SiaCallback<T>(val onSuccess: (Response<T>) -> Unit, val onError: (SiaError) -> Unit) : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful)
            onSuccess(response)
        else
            onError(SiaError())
    }

    override fun onFailure(call: Call<T>?, t: Throwable?) {

    }
}
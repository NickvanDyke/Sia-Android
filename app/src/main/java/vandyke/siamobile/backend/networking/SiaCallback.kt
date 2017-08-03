package vandyke.siamobile.backend.networking

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SiaCallback<T> : Callback<T> {
    private var onSuccess: ((T) -> Unit)? = null
    private var onSuccessNull: (() -> Unit)? = null
    private var onError: (SiaError) -> Unit

    constructor(onSuccess: (T) -> Unit, onError: (SiaError) -> Unit) {
        this.onSuccess = onSuccess
        this.onError = onError
    }

    constructor(onSuccessNull: () -> Unit, onError: (SiaError) -> Unit) {
        this.onSuccessNull = onSuccessNull
        this.onError = onError
        this.onSuccessNull = onError
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful)
                onSuccess?.invoke(response.body()!!) ?: onSuccessNull?.invoke()
        else
            onError(SiaError(response.errorBody()?.string() ?: ""))
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        onError(SiaError(t))
    }

    // TODO: I'm assuming params in onResponse and onFailure aren't null, not sure if they actually can be. will find out
    // the object in response.body() might also be nullable I'm not sure
}
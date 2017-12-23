package vandyke.siamobile.util

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import vandyke.siamobile.data.remote.SiaError

fun <T> Single<T>.siaSubscribe(onNext: (T) -> Unit, onError: (SiaError) -> Unit) {
    this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, {
                if (it is SiaError)
                    onError(it)
                else
                    onError(SiaError(it))
            })
}

fun Completable.siaSubscribe(onNext: () -> Unit, onError: (SiaError) -> Unit) {
    this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, {
                if (it is SiaError)
                    onError(it)
                else
                    onError(SiaError(it))
            })
}
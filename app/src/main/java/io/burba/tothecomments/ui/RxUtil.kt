package io.burba.tothecomments.ui

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Flowable<T>.ui() = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())!!
fun <T> Single<T>.ui() = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())!!

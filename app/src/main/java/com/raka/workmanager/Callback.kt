package com.raka.workmanager

interface Callback<T> {
    fun onSuccess(t: T)
    fun onError(e: Throwable)
}
package com.ivy.base.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun <T> MutableLiveData<T>.asLiveData(): LiveData<T> {
    return this
}

fun <T> MutableStateFlow<T>.readOnly(): StateFlow<T> {
    return this
}

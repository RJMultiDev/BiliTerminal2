package com.huanli233.biliterminal2.util.extensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class LoadState<T> {
    val isLoading
        get() = this is Loading || (isSuccess || isError).not()

    val isSuccess
        get() = this is Success

    val isError
        get() = this is Error

    @OptIn(ExperimentalContracts::class)
    inline fun onLoading(
        action: () -> Unit
    ): LoadState<T> {
        contract {
            callsInPlace(action, InvocationKind.AT_MOST_ONCE)
        }
        if (isLoading) action()
        return this
    }

    @OptIn(ExperimentalContracts::class)
    inline fun onSuccess(
        action: (value: T) -> Unit
    ): LoadState<T> {
        contract {
            callsInPlace(action, InvocationKind.AT_MOST_ONCE)
        }
        if (isSuccess) action((this as Success).data)
        return this
    }

    @OptIn(ExperimentalContracts::class)
    inline fun onError(
        action: (error: Throwable) -> Unit
    ): LoadState<T> {
        contract {
            callsInPlace(action, InvocationKind.AT_MOST_ONCE)
        }
        if (isError) action((this as Error).error)
        return this
    }

    fun toLoading() = this as Loading<T>
    fun toSuccess() = this as Success<T>
    fun toError() = this as Error<T>

    fun toLoadingOrNull() = this as? Loading<T>
    fun toSuccessOrNull() = this as? Success<T>
    fun toErrorOrNull() = this as? Error<T>

    class Loading<T> : LoadState<T>()
    data class Success<T>(val data: T) : LoadState<T>()
    data class Error<T>(val error: Throwable) : LoadState<T>()
}
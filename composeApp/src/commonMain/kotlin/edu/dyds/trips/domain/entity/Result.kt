package edu.dyds.trips.domain.entity

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> value
    is Result.Failure -> null
}

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> value
    is Result.Failure -> throw exception
}

fun <T> Result<T>.getOrElse(default: T): T = when (this) {
    is Result.Success -> value
    is Result.Failure -> default
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(value))
    is Result.Failure -> this
}

inline fun <T, R> Result<T>.mapCatching(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> try {
        Result.Success(transform(value))
    } catch (e: Exception) {
        Result.Failure(e)
    }

    is Result.Failure -> this
}

inline fun <T> Result<T>.onFailure(action: (Exception) -> Unit): Result<T> {
    if (this is Result.Failure) action(exception)
    return this
}

fun <T> kotlin.Result<T>.toDomainResult(): Result<T> = fold(
    onSuccess = { Result.Success(it) },
    onFailure = { Result.Failure(it as? Exception ?: RuntimeException(it)) }
)


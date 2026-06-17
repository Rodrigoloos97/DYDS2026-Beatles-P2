package edu.dyds.trips.data.util

import edu.dyds.trips.domain.util.Constants
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException

object NetworkErrorHandler {
    fun handle(throwable: Throwable): String = when (throwable) {
        is SocketTimeoutException -> Constants.ERROR_MESSAGE_TIMEOUT
        is ConnectException -> "${Constants.ERROR_MESSAGE_NO_CONNECTION} - verifica tu red"
        is SerializationException -> Constants.ERROR_MESSAGE_INVALID_DATA
        else -> throwable.message ?: "Error desconocido"
    }
}


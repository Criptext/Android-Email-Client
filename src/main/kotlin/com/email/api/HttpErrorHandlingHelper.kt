package com.email.api

import android.accounts.NetworkErrorException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

/**
 * Utilities to handle errors in various Http requests. BackgroundWorker instances should use
 * these functions instead of implementing their own.
 * Created by gabriel on 9/20/17.
 */

object HttpErrorHandlingHelper {

    val httpExceptionsToNetworkExceptions: (Exception) -> Exception
        get() = { exception: Exception ->
            exception.printStackTrace()
            if (exception is IOException
                    || exception is NullPointerException
                    || exception is SSLException
                    || exception is SocketTimeoutException
                    || exception is ConnectException)
                NetworkErrorException()
            else
                exception
        }


}

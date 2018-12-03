package com.criptext.mail.api

import okhttp3.Headers

/**
 * Created by sebas on 3/1/18.
 */
class ServerErrorException(
        val errorCode: Int,
        val rateLimitTime: Long? = null,
        val headers: ResultHeaders? = null
): Exception("Server error code: $errorCode")

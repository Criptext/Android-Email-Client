package com.criptext.mail.api


/**
 * Created by sebas on 3/1/18.
 */
class ServerErrorException(
        val errorCode: Int,
        val headers: ResultHeaders? = null,
        val body: String? = null
): Exception("Server error code: $errorCode")

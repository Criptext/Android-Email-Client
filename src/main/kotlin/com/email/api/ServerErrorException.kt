package com.email.api

/**
 * Created by sebas on 3/1/18.
 */
class ServerErrorException(val errorCode: Int): Exception("Server error code: $errorCode")

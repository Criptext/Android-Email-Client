package com.email.api

/**
 * Created by sebas on 2/27/18.
 */

class UnprocessableEntityException(val errorCode: Int): Exception("Server error code: $errorCode")

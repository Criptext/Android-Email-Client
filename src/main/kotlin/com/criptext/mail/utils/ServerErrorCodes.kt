package com.criptext.mail.utils

object ServerErrorCodes{
    const val BadRequest = 400
    const val Unauthorized = 401
    const val Forbidden = 403
    const val MethodNotAllowed = 405
    const val TooManyRequests = 429
    const val TooManyDevices = 439
    const val InternalServerError = 500
}
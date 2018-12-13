package com.criptext.mail.utils

object ServerErrorCodes{
    const val BadRequest = 400
    const val Unauthorized = 401
    const val DeviceRemoved = 481
    const val Forbidden = 403
    const val MethodNotAllowed = 405
    const val PayloadTooLarge = 413
    const val TooManyRequests = 429
    const val TooManyDevices = 439
    const val InternalServerError = 500
}
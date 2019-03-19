package com.criptext.mail.utils

object ServerCodes{
    //Success Codes
    const val Success = 200
    const val SuccessAndRepeat = 201


    //Error Codes
    const val BadRequest = 400
    const val Unauthorized = 401
    const val Forbidden = 403
    const val MethodNotAllowed = 405
    const val Gone = 410
    const val PayloadTooLarge = 413
    const val TooManyRequests = 429
    const val TooManyDevices = 439
    const val InternalServerError = 500
}
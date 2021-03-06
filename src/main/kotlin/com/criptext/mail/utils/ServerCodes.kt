package com.criptext.mail.utils

object ServerCodes{
    //Success Codes
    const val Success = 200
    const val SuccessAndRepeat = 201

    const val NoContent = 204


    //Error Codes
    const val BadRequest = 400
    const val Unauthorized = 401
    const val Forbidden = 403
    const val MethodNotAllowed = 405
    const val Conflict = 409
    const val Gone = 410
    const val PreconditionFail = 412
    const val PayloadTooLarge = 413
    const val SessionExpired = 419
    const val PreConditionRequired = 428
    const val TooManyRequests = 429
    const val VersionNotSupported = 430
    const val TooManyDevices = 439
    const val EnterpriseAccountSuspended = 451
    const val AuthenticationPending = 491
    const val AuthenticationDenied = 493
    const val InternalServerError = 500
}
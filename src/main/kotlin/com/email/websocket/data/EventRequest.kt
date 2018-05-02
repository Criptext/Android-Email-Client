package com.email.websocket.data

import com.email.api.models.EmailMetadata

/**
 * Created by gabriel on 5/1/18.
 */
sealed class EventRequest {
    data class InsertNewEmail(val emailMetadata: EmailMetadata): EventRequest()

}
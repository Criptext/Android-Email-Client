package com.email.websocket.data

import com.email.db.models.Email
import com.email.utils.UIMessage

/**
 * Created by gabriel on 5/1/18.
 */
sealed class EventResult {
    sealed class InsertNewEmail: EventResult()  {
        data class Success(val newEmail: Email): InsertNewEmail()
        class Failure(val message: UIMessage): InsertNewEmail()
    }

}
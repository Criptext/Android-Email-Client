package com.email.scenes.emaildetail.data

import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/12/18.
 */

sealed class EmailDetailResult {


    sealed class DecryptMail(): EmailDetailResult() {

        data class Success(val decryptedText: String): DecryptMail()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): DecryptMail()
    }

    sealed class LoadFullEmailsFromThreadId: EmailDetailResult() {
        data class Success(val fullEmailList: List<FullEmail>): LoadFullEmailsFromThreadId()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): LoadFullEmailsFromThreadId()
    }

    sealed class UnsendFullEmailFromEmailId: EmailDetailResult() {
        data class Success(val position: Int): UnsendFullEmailFromEmailId()
        data class Failure(
                val position: Int,
                val message: UIMessage,
                val exception: Exception): UnsendFullEmailFromEmailId()
    }
}

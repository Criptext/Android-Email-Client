package com.email.scenes.emaildetail.data

/**
 * Created by sebas on 3/12/18.
 */

sealed class EmailDetailRequest{

    class DecryptMail(
            val recipientId: String,
            val deviceId: Int,
            val emailId: Int,
            val encryptedText: String): EmailDetailRequest()

    class LoadFullEmailsFromThreadId(
            val threadId: String): EmailDetailRequest()

    class UnsendFullEmailFromEmailId(
            val emailId: Int, val position: Int): EmailDetailRequest()
}

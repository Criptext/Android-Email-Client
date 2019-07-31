package com.criptext.mail.services

import com.github.kittinunf.result.Result
import com.google.firebase.iid.FirebaseInstanceId

interface MessagingInstance{

    val token: String?

    class Default : MessagingInstance {
        private val firebaseInstance = Result.of { FirebaseInstanceId.getInstance() }
        override val token = when(firebaseInstance) {
                is Result.Success -> firebaseInstance.value.token
                is Result.Failure -> null
        }
    }

    companion object {
        const val THREAD_ID = "service.threadId"
    }
}
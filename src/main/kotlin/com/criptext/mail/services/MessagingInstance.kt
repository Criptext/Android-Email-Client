package com.criptext.mail.services

import com.google.firebase.iid.FirebaseInstanceId

interface MessagingInstance{

    val token: String?

    class Default : MessagingInstance {
        override val token = FirebaseInstanceId.getInstance().token
    }
}
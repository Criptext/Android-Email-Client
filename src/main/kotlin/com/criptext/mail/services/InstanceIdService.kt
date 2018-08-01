package com.criptext.mail.services

import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.services.data.MessagingServiceController
import com.criptext.mail.services.data.MessagingServiceDataSource
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService


class InstanceIdService : FirebaseInstanceIdService(){

    private lateinit var messagingController : MessagingServiceController


    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        messagingController = MessagingServiceController(
                dataSource = MessagingServiceDataSource(
                        accountDao = AppDatabase.getAppDatabase(this).accountDao(),
                        httpClient = HttpClient.Default(),
                        runner = AsyncTaskWorkRunner()),
                messagingInstance = MessagingInstance.Default())
        messagingController.refreshPushToken()

    }
}
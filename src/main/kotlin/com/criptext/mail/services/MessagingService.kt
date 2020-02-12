package com.criptext.mail.services

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.criptext.mail.R
import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.AntiPushMap
import com.criptext.mail.db.models.Contact
import com.criptext.mail.push.PushData
import com.criptext.mail.push.PushTypes
import com.criptext.mail.push.notifiers.*
import com.criptext.mail.push.services.PushReceiverIntentService
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalEncryptedData
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import org.json.JSONObject
import org.whispersystems.libsignal.DuplicateMessageException


class MessagingService : FirebaseMessagingService(){

    private lateinit var activeAccount: ActiveAccount
    private lateinit var signalClient: SignalClient
    private var notificationId: Int = -1
    private lateinit var db: AppDatabase
    private val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    private var bm: Bitmap? = null
    private var shouldPostNotification = false

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.data.isNotEmpty()) {
            shouldPostNotification = !PushReceiverIntentService.isAppOnForeground(this, packageName)
            val pushData = remoteMessage.data
            if(shouldPostNotification) {
                val operation = Result.of {
                    db = AppDatabase.getAppDatabase(this)
                    val dbAccount = db.accountDao().getAccount(pushData["account"]!!, pushData["domain"]!!)!!
                    activeAccount = ActiveAccount.loadFromDB(dbAccount)!!
                    signalClient = SignalClient.Default(SignalStoreCriptext(db, activeAccount))

                    setupForAntiPush(pushData)
                    val rowId = pushData["rowId"]?.toInt()

                    if (rowId == null || rowId == 0) {
                        throw Exception()
                    }

                    if (pushData["action"] == PushTypes.newMail.actionCode()) {
                        val preview = getDecryptedEmailPreview(signalClient, remoteMessage.data)
                        pushData["preview"] = preview
                        pushData["subject"] = remoteMessage.data["body"]
                        pushData["name"] = remoteMessage.data["title"]
                        pushData["email"] = remoteMessage.data["senderId"]?.plus("@${remoteMessage.data["senderDomain"]}")
                        bm = try {
                            if (pushData["email"] != null && EmailAddressUtils.isFromCriptextDomain(pushData["email"]!!)) {
                                val domain = EmailAddressUtils.extractEmailAddressDomain(pushData["email"]!!)
                                Picasso.get().load(Hosts.restApiBaseUrl
                                        .plus("/user/avatar/$domain/${EmailAddressUtils.extractRecipientIdFromAddress(pushData["email"]!!, domain)}")).get()
                            } else
                                null
                        } catch (ex: Exception) {
                            null
                        }
                    }
                }

                when (operation) {
                    is Result.Success -> {
                        createAndNotifyPush(pushData, shouldPostNotification, bm, notificationId)
                    }
                    is Result.Failure -> {
                        val data = PushData.Error(UIMessage(R.string.push_email_update_mailbox_title),
                                UIMessage(R.string.push_email_update_mailbox_body), isPostNougat, shouldPostNotification)
                        ErrorNotifier.Open(data).notifyPushEvent(this)
                    }
                }

                val intent = Intent(this, PushReceiverIntentService::class.java)
                val json = JSONObject(remoteMessage.data)
                intent.putExtra("data", json.toString())
                startService(intent)
            }
        }
    }

    private fun createAndNotifyPush(pushData: Map<String, String>, shouldPostNotification: Boolean, senderImage: Bitmap?, notificationId: Int){
        val action = pushData["action"]
        if (action != null) {
            val type = PushTypes.fromActionString(action)
            val notifier =  when (type) {
                PushTypes.newMail -> {
                    val data = PushData.NewMail.parseNewMailPush(pushData, shouldPostNotification, isPostNougat, senderImage)
                    NewMailNotifier.Single(data, notificationId)
                }
                PushTypes.linkDevice -> {
                    val data = PushData.LinkDevice.parseLinkDevicePush(pushData, shouldPostNotification, isPostNougat)
                    LinkDeviceNotifier.Open(data, notificationId)
                }
                PushTypes.openActivity -> {
                    val data = PushData.OpenMailbox.parseNewOpenMailbox(pushData, shouldPostNotification, isPostNougat)
                    OpenMailboxNotifier.Open(data)
                }
                PushTypes.syncDevice -> {
                    val data = PushData.SyncDevice.parseSyncDevicePush(pushData, shouldPostNotification, isPostNougat)
                    SyncDeviceNotifier.Open(data, notificationId)
                }
                PushTypes.antiPush -> {
                    val subAction = pushData["subAction"]
                    when(subAction){
                        "delete_new_email" -> {
                            val metadataKeys = pushData["metadataKeys"]?.split(",")
                            metadataKeys?.forEach {
                                val value = db.antiPushMapDao().getByValue(it, activeAccount.id)
                                if(value != null) db.antiPushMapDao().deleteById(value)
                            }
                        }
                        "delete_sync_link" -> {
                            val randomId = pushData["randomId"] ?: ""
                            if(randomId.isNotEmpty()) {
                                val value = db.antiPushMapDao().getByValue(randomId, activeAccount.id)
                                if(value != null) db.antiPushMapDao().deleteById(value)
                            }
                        }
                    }
                    null
                }
                else -> null
            }
            notifier?.notifyPushEvent(this)
        }
    }

    private fun setupForAntiPush(data: Map<String, String>){
        val action = data["action"] ?: return
        when(action){
            PushTypes.newMail.actionCode() -> {
                val metadataKey = data["metadataKey"]
                if(!metadataKey.isNullOrEmpty())
                    notificationId = db.antiPushMapDao().insert(AntiPushMap(0, metadataKey!!, activeAccount.id)).toInt()
            }
            PushTypes.linkDevice.actionCode(),
            PushTypes.syncDevice.actionCode() -> {
                val randomId = data["randomId"]
                if(!randomId.isNullOrEmpty())
                    notificationId = db.antiPushMapDao().insert(AntiPushMap(0, randomId!!, activeAccount.id)).toInt()
            }
            else -> {
                val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                val type = PushTypes.fromActionString(action)
                notificationId =  if(isPostNougat) type.requestCodeRandom() else type.requestCode()
            }
        }
    }

    private fun getDecryptedEmailPreview(signalClient: SignalClient,
                                      metadata: Map<String, String>): String {
        val senderId = getSenderId(metadata)
        val messageType = metadata["previewMessageType"]?.toInt()
        return if (messageType != null && senderId.first != null) {
            val encryptedData = SignalEncryptedData(
                    encryptedB64 = metadata.getValue("preview"),
                    type = SignalEncryptedData.Type.fromInt(messageType)!!)
            try {
                signalClient.decryptMessage(recipientId = senderId.second,
                        deviceId = senderId.first!!,
                        encryptedData = encryptedData)
            } catch (ex: Exception) {
                if (ex is DuplicateMessageException) throw ex
                Crashlytics.logException(ex)
                "Unable to decrypt message."
            }
        } else
            metadata.getValue("preview")
    }

    private fun getSenderId(metadata: Map<String, String>): Pair<Int?, String>{
        val senderDeviceId = metadata["deviceId"]?.toInt()
        val senderDomain = metadata["senderDomain"] ?: Contact.mainDomain
        val senderRecipientId = if(senderDomain.isEmpty() || senderDomain == Contact.mainDomain) metadata["senderId"]!!
        else metadata["senderId"].plus("@${senderDomain}")
        return Pair(senderDeviceId, senderRecipientId)
    }
}
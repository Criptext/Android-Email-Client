package com.criptext.mail.push.services

import android.app.ActivityManager
import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.PushController
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.notifiers.Notifier
import com.github.kittinunf.result.Result
import org.json.JSONObject
import java.util.HashMap
import com.google.gson.Gson
import androidx.core.content.ContextCompat.getSystemService
import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken


class PushReceiverIntentService: IntentService("CriptextPushReceiverIntentService") {
    private var pushController: PushController? = null

    override fun onHandleIntent(intent: Intent?) {
        if(intent != null) {
            val stringData = intent.getStringExtra("data") ?: return
            val pushData: Map<String, String> = Gson().fromJson<Map<String, String>>(
                    stringData, object : TypeToken<Map<String, String>>() {
            }.type
            )
            if(pushController == null){
                val db = Result.of { AppDatabase.getAppDatabase(this) }
                if(db is Result.Success) {
                    val dbAccount = db.value.accountDao().getAccount(pushData["account"]!!, pushData["domain"]!!)!!
                    val account = ActiveAccount.loadFromDB(dbAccount)!!
                    val signalClient = SignalClient.Default(SignalStoreCriptext(db.value, account))
                    val storage = KeyValueStorage.SharedPrefs(this)
                    pushController = PushController(
                            dataSource = PushDataSource(db = db.value,
                                    runner = AsyncTaskWorkRunner(),
                                    httpClient = HttpClient.Default(),
                                    activeAccount = account,
                                    storage = storage,
                                    filesDir = this.filesDir,
                                    cacheDir = this.cacheDir,
                                    signalClient = signalClient),
                            host = this)
                }
            }
            val shouldPostNotification = !isAppOnForeground(this, packageName)
            pushController?.updateMailbox(pushData, shouldPostNotification)
        }
    }

    fun stopIntentService(){
        stopSelf()
    }

    companion object {
        fun isAppOnForeground(context: Context, appPackageName: String): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses ?: return false
            return appProcesses.any {
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && it.processName == appPackageName
            }
        }
    }
}
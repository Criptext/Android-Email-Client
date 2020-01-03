package com.criptext.mail.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.criptext.mail.scenes.emaildetail.EmailDetailActivity
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.services.MessagingInstance

/**
 * Factory class for creating Pending intents invoked when a push notification is clicked.
 * Created by gabriel on 8/21/17.
 */

class ActivityIntentFactory {
    companion object {

        private fun buildSceneActivityIntent(ctx: Context, type: PushTypes, extraParam: String?, account: String?,
                                             domain: String?)
                : Intent {
            val intent: Intent?
            when(type) {
                PushTypes.openActivity -> {
                    intent = Intent(ctx, MailboxActivity::class.java)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putAccountExtra(intent, account, domain)
                    return intent
                }
                PushTypes.linkDevice,
                    PushTypes.syncDevice -> {
                    intent = Intent(ctx, MailboxActivity::class.java)
                    intent.action = Intent.ACTION_MAIN
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putAccountExtra(intent, account, domain)
                    return intent
                }
                else -> {
                    intent = Intent(ctx, MailboxActivity::class.java)
                    intent.action = Intent.ACTION_MAIN
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putAccountExtra(intent, account, domain)
                    if (extraParam != null) {
                        intent.putExtra(MessagingInstance.THREAD_ID, extraParam)
                    }
                    return intent
                }
            }
        }

        private fun putAccountExtra(intent: Intent, account: String?, domain: String?){
            if(account != null)
                intent.putExtra("account", account)
            if(domain != null)
                intent.putExtra("domain", domain)
        }

        internal fun buildSceneActivityPendingIntent(ctx: Context, type : PushTypes,
                                                     extraParam: String?, isPostNougat: Boolean,
                                                     account: String? = null, domain: String? = null) : PendingIntent {
            val intent = buildSceneActivityIntent(ctx, type, extraParam, account, domain)
            return PendingIntent.getActivity(ctx, if(isPostNougat) type.requestCodeRandom() else type.requestCode(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)
        }
    }
}
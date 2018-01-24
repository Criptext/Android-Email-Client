package com.email

import android.app.Application
import android.content.Context
import android.os.Environment
/*import android.support.multidex.MultiDex
import com.google.api.services.gmail.GmailScopes
import uk.co.chrisjenx.calligraphy.CalligraphyConfig*/
import java.io.File
import java.util.*

/**
 * Created by hirobreak on 30/03/17.
 */

//class SecureEmail : com.activeandroid.app.Application() {
class SecureEmail {
    private var singleton: SecureEmail? = null

    companion object{
        val IS_REGISTERED = "MonkeyChat.IsRegistered"
        val APPNAME = "Criptext Secure Email"
        val FULLNAME = "MonkeyChat.FullName"
        val SOCKET_DOMAIN = "MonkeyChat.SocketDomain"
        val SOCKET_PORT = "MonkeyChat.SocketPort"
        val NO_MORE_PAGES = "NO_MORE_PAGES"

        val FILES_DIR = "files"

        val ACTIVE_ACCOUNT = "activeAcount"
/*        val GMAIL_SCOPE : ArrayList<String>
            get(){
                var scope = ArrayList<String>()
                scope.add(GmailScopes.MAIL_GOOGLE_COM)
                return scope
            }*/

        val LABEL_CORREO_NO_DESEADO = "Label_1"
        val LABEL_USER_SENT = "Label_2"
        val LABEL_USER_THRASH = "Label3"
        val LABEL_CATEGORY_PERSONAL = "CATEGORY_PERSONAL"
        val LABEL_CATEGORY_SOCIAL = "CATEGORY_SOCIAL"
        val LABEL_IMPORTANT = "IMPORTANT"
        val LABEL_CATEGORY_UPDATES = "CATEGORY_UPDATES"
        val LABEL_CATEGORY_FORUMS = "CATEGORY_FORUMS"
        val LABEL_CHAT = "CHAT"
        val LABEL_SENT = "SENT"
        val LABEL_INBOX = "INBOX"
        val LABEL_TRASH = "TRASH"
        val LABEL_ALL_MAIL = ""
        val LABEL_CATEGORY_PROMOTIONS = "CATEGORY_PROMOTIONS"
        val LABEL_DRAFT = "DRAFT"
        val LABEL_SPAM = "SPAM"
        val LABEL_STARRED = "STARRED"
        val LABEL_UNREAD = "UNREAD"

        val PARAM_LABEL = "service.label"
        val PARAM_EMAIL = "service.email"
        val ACTION_UPDATE_TOKEN = "action.update.token"
        val ACTION_TOKEN = "action.token"
        val INBOX_UPDATE = "preferences.updated"
        val THREAD_ID = "service.threadid"
        val NOTIFICATION_ID = "service.notificationid"
        val ACTIVITY_FLAG = "service.activity"
        val REPLY_FLAG = "service.reply"
        val ARCHIVE_FLAG = "service.archive"

        val SECURE_SHOW = "settings.secureShow"
        val CRIPTEXT_SEARCH = "criptext.search"

    }

/*
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
*/



/*
        val downloadDir = File(Environment.getExternalStorageDirectory(),
                ctx.resources.getString(R.string.app_name))
        downloadDir.mkdirs()
        var subdir = File(downloadDir, SecureEmail.FILES_DIR)
        subdir.mkdir()
        return downloadDir
    }
*/

/*
    override fun onCreate() {
        super.onCreate()
        singleton = this
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }
*/

    fun getInstance(): SecureEmail? {
        return singleton
    }

}
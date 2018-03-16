package com.email

/**
 * Created by hirobreak on 30/03/17.
 */

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

    fun getInstance(): SecureEmail? {
        return singleton
    }

}
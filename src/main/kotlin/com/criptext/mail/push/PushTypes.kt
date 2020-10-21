package com.criptext.mail.push

enum class PushTypes {
    newMail, openActivity,linkDevice, syncDevice, antiPush, jobBackup, failedEmail;

    fun actionCode(): String = when (this) {
        newMail -> "open_thread"
        openActivity -> "open_activity"
        linkDevice -> "link_device"
        syncDevice -> "sync_device"
        antiPush -> "anti_push"
        jobBackup -> "job_backup"
        failedEmail -> "open_thread"
    }

    fun requestCodeRandom(): Int = this.ordinal + System.currentTimeMillis().toInt()
    fun requestCode(): Int = this.ordinal

    companion object {
        fun fromActionString(action: String): PushTypes =
            when(action){
                newMail.actionCode() -> newMail
                openActivity.actionCode() -> openActivity
                linkDevice.actionCode() -> linkDevice
                syncDevice.actionCode() -> syncDevice
                antiPush.actionCode() -> antiPush
                else -> throw IllegalArgumentException("Unknown push action: $action")
            }
    }
}

package com.criptext.mail.push

/**
 * Enum class to model each type of push notification.
 * Created by gabriel on 8/21/17.
 */
enum class PushTypes {
    newMail, openActivity;

    fun actionCode(): String = when (this) {
        newMail -> "open_thread"
        openActivity -> "open_activity"
    }

    fun requestCodeRandom(): Int = this.ordinal + System.currentTimeMillis().toInt()
    fun requestCode(): Int = this.ordinal

    companion object {
        fun fromActionString(action: String): PushTypes =
            when(action){
                newMail.actionCode() -> newMail
                openActivity.actionCode() -> openActivity
                else -> throw IllegalArgumentException("Unknown push action: $action")
            }
    }
}

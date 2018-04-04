package com.email.scenes

import com.email.scenes.composer.ui.UIData

/**
 * Created by gabriel on 4/3/18.
 */

sealed class ActivityMessage {
    data class SendMail(val uiData: UIData): ActivityMessage()
}
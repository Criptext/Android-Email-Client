package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.mailbox.emailsource.EmailSourceActivity

class EmailSourceParams(val emailSource: String): SceneParams(){
    override val activityClass = EmailSourceActivity::class.java
}
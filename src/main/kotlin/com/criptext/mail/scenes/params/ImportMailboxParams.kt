package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.import_mailbox.ImportMailboxActivity

open class ImportMailboxParams: SceneParams() {
    override val activityClass = ImportMailboxActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == ImportMailboxParams::class.java
    }

}

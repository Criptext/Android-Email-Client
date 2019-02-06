package com.criptext.mail.scenes.mailbox

import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.models.Label

enum class NavigationMenuOptions {
    INBOX, SENT, DRAFT, STARRED, SPAM, TRASH, ALL_MAIL, LABELS, SETTINGS, SUPPORT;

    fun toLabel(): Label? = when(this) {
        INBOX -> Label.defaultItems.inbox
        SENT -> Label.defaultItems.sent
        DRAFT -> Label.defaultItems.draft
        STARRED -> Label.defaultItems.starred
        SPAM -> Label.defaultItems.spam
        TRASH -> Label.defaultItems.trash
        ALL_MAIL -> Label(id = -1, color = "000000", text = Label.LABEL_ALL_MAIL,
                visible = true, type = LabelTypes.SYSTEM, uuid = "00000000-0000-0000-0000-000000000000",
                accountId = -1)
        else -> null
    }
}

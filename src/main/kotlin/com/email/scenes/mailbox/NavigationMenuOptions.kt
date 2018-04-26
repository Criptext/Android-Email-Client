package com.email.scenes.mailbox

import com.email.db.models.Label

enum class NavigationMenuOptions {
    INBOX, SENT, DRAFT, STARRED, SPAM, TRASH, ALL_MAIL, LABELS, SETTINGS, SUPPORT;

    fun toLabel(): Label? = when(this) {
        INBOX -> Label.defaultItems.inbox
        SENT -> Label.defaultItems.sent
        DRAFT -> Label.defaultItems.draft
        STARRED -> Label.defaultItems.starred
        SPAM -> Label.defaultItems.spam
        TRASH -> Label.defaultItems.trash
        else -> null
    }
}

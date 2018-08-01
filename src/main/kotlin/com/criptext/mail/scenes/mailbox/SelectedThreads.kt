package com.criptext.mail.scenes.mailbox

import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.removeWithDiscrimination
import java.util.*

class SelectedThreads {
    private val selectedItems: LinkedList<EmailPreview> = LinkedList()

    private var selectedUnreadItemCount = 0

    val isInUnreadMode: Boolean
        get() = if(selectedItems.isEmpty()) false
        else  selectedItems[0].unread

    val hasUnreadThreads: Boolean
        get() = selectedUnreadItemCount > 0

    fun add(item: EmailPreview) {
        selectedItems.add(item)
        if(!item.isSelected)
            item.isSelected  = true
        if (item.unread) selectedUnreadItemCount += 1
    }

    fun remove(item: EmailPreview) {
        item.isSelected = false
        selectedItems.removeWithDiscrimination { it.threadId.equals(item.threadId) }
        if (item.unread) selectedUnreadItemCount -= 1
    }

    fun clear() {
        selectedItems.forEach { it.isSelected = false }
        selectedItems.clear()
        selectedUnreadItemCount = 0
    }

    fun length(): Int = selectedItems.size

    fun isEmpty(): Boolean = selectedItems.isEmpty()

    fun toIDs(): List<String> =
            selectedItems.map { it.threadId }

    fun toList() = selectedItems.toList()
}


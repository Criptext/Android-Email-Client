package com.email.scenes.mailbox

import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.file.removeWithDiscrimination
import java.util.*

class SelectedThreads() {
    private val selectedItems: LinkedList<EmailThread> = LinkedList()

    private var selectedUnreadItemCount = 0

    val hasUnreadThreads: Boolean
        get() = selectedUnreadItemCount > 0

    fun add(item: EmailThread) {
        selectedItems.add(item)
        if(!item.isSelected)
            item.isSelected  = true
        if (item.unread) selectedUnreadItemCount += 1
    }

    fun remove(item: EmailThread) {
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

package com.criptext.mail.scenes.label_chooser

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.removeWithDiscrimination
import java.util.*

/**
 * Created by sebas on 2/2/18.
 */
class SelectedLabels() {
    private val selectedItems: LinkedList<LabelWrapper> = LinkedList()

    fun addMultipleSelected(items: List<LabelWrapper>) {
        selectedItems.addAll(items)
        items.forEach {
            if(!it.isSelected)
                it.isSelected  = true
        }
    }

    fun add(item: LabelWrapper) {
        selectedItems.add(item)
        if(!item.isSelected)
            item.isSelected  = true
    }

    fun remove(item: LabelWrapper) {
        item.isSelected = false
        selectedItems.removeWithDiscrimination { it.id.equals(item.id) }
    }

    fun clear() {
        selectedItems.forEach { it.isSelected = false }
        selectedItems.clear()
    }

    fun length(): Int = selectedItems.size

    fun isEmpty(): Boolean = selectedItems.isEmpty()

    fun toIDs(): List<Long> =
            selectedItems.map { it.id }

    fun toList() = selectedItems.toList()

}

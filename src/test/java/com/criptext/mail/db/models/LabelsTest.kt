package com.criptext.mail.db.models

import org.amshove.kluent.`should equal`
import org.junit.Test


class LabelsTest {

    @Test
    fun `should load rejected labels correctly based on a current Label`() {
        val defaultLabels = Label.DefaultItems()
        for (label in defaultLabels.toList()) {
            when(label.text){
                "Trash" -> {
                    val rejectedLabelsInbox = defaultLabels.rejectedLabelsByFolder("Trash")
                    rejectedLabelsInbox.map { it.text } `should equal` listOf("Spam")
                }
                "Spam" -> {
                    val rejectedLabelsInbox = defaultLabels.rejectedLabelsByFolder("Spam")
                    rejectedLabelsInbox.map { it.text } `should equal` listOf("Trash")
                }
                else -> {
                    val rejectedLabelsInbox = defaultLabels.rejectedLabelsByFolder(label.text)
                    rejectedLabelsInbox.map { it.text } `should equal` listOf("Spam", "Trash")
                }
            }
        }

    }
}
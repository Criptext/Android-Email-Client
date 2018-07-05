package com.email.scenes.mailbox.data

import com.email.api.models.EmailMetadata
import com.email.db.AppDatabase
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.db.models.EmailLabel
import com.email.db.models.Label

object ExistingEmailUpdateSetup {

    val defaultItems = Label.DefaultItems()

    fun updateExistingEmailTransaction(metadata: EmailMetadata, dao: EmailInsertionDao,
                                       activeAccount: ActiveAccount): Email {
        val existingEmail = dao.findEmailByMessageId(metadata.messageId)!!
        if(metadata.senderRecipientId == activeAccount.recipientId){
            dao.deleteByEmailLabelIds(labelId = defaultItems.sent.id, emailId = existingEmail.id)
            val emailLabel = EmailLabel(emailId = existingEmail.id, labelId = defaultItems.sent.id)
            dao.insertEmailLabelRelations(listOf(emailLabel))
        }
        if(metadata.bcc.contains(activeAccount.userEmail)
                || metadata.cc.contains(activeAccount.userEmail)
                || metadata.to.contains(activeAccount.userEmail)){
            dao.deleteByEmailLabelIds(labelId = defaultItems.inbox.id, emailId = existingEmail.id)
            dao.deleteByEmailLabelIds(labelId = defaultItems.inbox.id, emailId = existingEmail.id)
            val emailLabel = EmailLabel(emailId = existingEmail.id, labelId = defaultItems.inbox.id)
            dao.insertEmailLabelRelations(listOf(emailLabel))
        }
        return existingEmail
    }

}
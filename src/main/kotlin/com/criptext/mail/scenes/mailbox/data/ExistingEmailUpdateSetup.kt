package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label

object ExistingEmailUpdateSetup {

    val defaultItems = Label.DefaultItems()

    fun updateExistingEmailTransaction(metadata: EmailMetadata, dao: EmailInsertionDao,
                                       activeAccount: ActiveAccount): Email {
        val existingEmail = dao.findEmailByMessageId(metadata.messageId, activeAccount.id)!!
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
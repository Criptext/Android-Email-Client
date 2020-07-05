package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.AliasDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage

/**
 * Created by sebas on 3/13/18.
 */

class LoadFullEmailsFromThreadWorker(
        private val db: EmailDetailLocalDB,
        private val accountDao: AccountDao,
        private val aliasDao: AliasDao,
        private val activeAccount: ActiveAccount,
        private val changeAccountMessage: UIMessage?,
        private val threadId: String,
        private val currentLabel: Label,
        override val publishFn: (EmailDetailResult.LoadFullEmailsFromThreadId) -> Unit)
    : BackgroundWorker<EmailDetailResult.LoadFullEmailsFromThreadId> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.LoadFullEmailsFromThreadId {
        return EmailDetailResult.LoadFullEmailsFromThreadId.Failure()
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.LoadFullEmailsFromThreadId>): EmailDetailResult.LoadFullEmailsFromThreadId {
        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val items = db.getFullEmailsFromThreadId(threadId = threadId,
                rejectedLabels = rejectedLabels,
                selectedLabel = currentLabel.text, account = activeAccount)
        val unreadEmails = items.filter { it.email.unread }.size
        items.forEach {
            it.email.unread = false
            if(getTrueEmail(it.from.email) == activeAccount.userEmail)
                it.isFromMe = true
        }
        return EmailDetailResult.LoadFullEmailsFromThreadId.Success(items, unreadEmails, changeAccountMessage)
    }

    override fun cancel() {
    }

    private fun getTrueEmail(from: String): String{
        val domain = EmailAddressUtils.extractEmailAddressDomain(from)
        if(domain.isNotEmpty()){
            val recipientId = EmailAddressUtils.extractRecipientIdFromAddress(from, domain)
            var dbAccount = accountDao.getAccount(recipientId, domain)
            if(dbAccount == null){
                val alias = (if(domain == Contact.mainDomain) {
                    aliasDao.getCriptextAliasByName(recipientId)
                } else {
                    aliasDao.getAliasByName(recipientId, domain)
                }) ?: return from
                dbAccount = (accountDao.getAccountById(alias.accountId) ?: return from)
            }
            return dbAccount.recipientId.plus("@${dbAccount.domain}")
        }
        return from
    }

}

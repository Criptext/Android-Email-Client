package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap

class GetTotalUnreadMailsByLabelWorker(private val emailDao: EmailDao,
                                       private val db: MailboxLocalDB,
                                       private val activeAccount: ActiveAccount,
                                       private val currentLabel: String,
                                       override val publishFn: (GeneralResult.TotalUnreadEmails) -> Unit
                          ) : BackgroundWorker<GeneralResult.TotalUnreadEmails> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): GeneralResult.TotalUnreadEmails {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<GeneralResult.TotalUnreadEmails>)
            : GeneralResult.TotalUnreadEmails? {
        val rejectedLabels = Label.defaultItems.rejectedLabelsByFolder(currentLabel).map { it.id }
        val operation = Result.of {
            val labelId = Label.getLabelIdWildcard(labelName = currentLabel, labels = Label.defaultItems.toList())
            val extraLabelId = Label.getLabelIdWildcard(labelName = Label.LABEL_INBOX, labels = Label.defaultItems.toList())
            val extraAccounts = db.getLoggedAccounts().filter { it.recipientId.plus("@${it.domain}") != activeAccount.userEmail }
            val extraAccountsData = extraAccounts.map { Pair(it.recipientId.plus("@${it.domain}"),
                    emailDao.getTotalUnreadThreads(rejectedLabels, extraLabelId, it.id).size) }
            Pair(emailDao.getTotalUnreadThreads(rejectedLabels, labelId, activeAccount.id).size, extraAccountsData)
        }
        return when (operation){
            is Result.Success -> {
                GeneralResult.TotalUnreadEmails.Success(operation.value.first, operation.value.second)
            }
            is Result.Failure -> {
                GeneralResult.TotalUnreadEmails.Failure(UIMessage(R.string.local_error, arrayOf(operation.error.toString())))
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}
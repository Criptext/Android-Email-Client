package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupData
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result


class CheckCloudBackupEnableWorker(val activeAccount: ActiveAccount,
                                   private val accountDao: AccountDao,
                                   override val publishFn: (MailboxResult) -> Unit)
    : BackgroundWorker<MailboxResult.CheckCloudBackupEnabled> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.CheckCloudBackupEnabled {
        return if(ex is ServerErrorException) {
            MailboxResult.CheckCloudBackupEnabled.Failure(UIMessage(R.string.server_bad_status), ex)
        }else {
            MailboxResult.CheckCloudBackupEnabled.Failure(UIMessage(R.string.local_error, arrayOf(ex.toString())), ex)
        }
    }

    override fun work(reporter: ProgressReporter<MailboxResult.CheckCloudBackupEnabled>): MailboxResult.CheckCloudBackupEnabled? {
        val result =  Result.of {
            accountDao.getAccountById(activeAccount.id) ?: throw Exception()
        }

        return when (result) {
            is Result.Success -> MailboxResult.CheckCloudBackupEnabled.Success(
                result.value.hasCloudBackup
            )

            is Result.Failure -> catchException(result.error)
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}
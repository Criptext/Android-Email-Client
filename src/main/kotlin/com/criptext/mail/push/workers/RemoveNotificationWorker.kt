package com.criptext.mail.push.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import org.whispersystems.libsignal.DuplicateMessageException


class RemoveNotificationWorker(
        private val db: AppDatabase,
        private val notificationValue: String,
        private val pushData: Map<String, String>,
        override val publishFn: (
                PushResult.RemoveNotification) -> Unit)
    : BackgroundWorker<PushResult.RemoveNotification> {


    override val canBeParallelized = false

    private lateinit var activeAccount: ActiveAccount

    override fun catchException(ex: Exception): PushResult.RemoveNotification {
        val message = createErrorMessage(ex)
        return PushResult.RemoveNotification.Failure(message)
    }

    private fun processFailure(failure: Result.Failure<Int,
            Exception>): PushResult.RemoveNotification {
        return PushResult.RemoveNotification.Failure(createErrorMessage(failure.error))
    }

    override fun work(reporter: ProgressReporter<PushResult.RemoveNotification>)
            : PushResult.RemoveNotification? {

        val (account, domain) = Pair(pushData["account"], pushData["domain"])
        if(account.isNullOrEmpty() || domain.isNullOrEmpty())
            return catchException(Exception())


        val dbAccount = db.accountDao().getAccount(account!!, domain!!) ?: return catchException(Exception())
        activeAccount = ActiveAccount.loadFromDB(dbAccount)!!

        val operationResult = Result.of {
            db.antiPushMapDao().getByValue(notificationValue, activeAccount.id)
        }


        return when(operationResult) {
            is Result.Success -> {
                db.antiPushMapDao().deleteById(operationResult.value)
                PushResult.RemoveNotification.Success(operationResult.value)
            }

            is Result.Failure -> processFailure(operationResult)
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex) {
            is DuplicateMessageException ->
                UIMessage(resId = R.string.email_already_decrypted)
            else -> {
                UIMessage(resId = R.string.failed_getting_emails)
            }
        }
    }
}

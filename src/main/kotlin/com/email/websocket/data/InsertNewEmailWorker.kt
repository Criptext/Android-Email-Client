package com.email.websocket.data

import com.email.R
import com.email.api.EmailInsertionAPIClient
import com.email.api.models.EmailMetadata
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.db.models.CRFile
import com.email.scenes.mailbox.data.EmailInsertionSetup
import com.email.scenes.mailbox.data.ExistingEmailUpdateSetup
import com.email.signal.SignalClient
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import org.whispersystems.libsignal.DuplicateMessageException

/**
 * Created by gabriel on 5/1/18.
 */
class InsertNewEmailWorker(private val emailInsertionDao: EmailInsertionDao,
                           private val emailInsertionApi: EmailInsertionAPIClient,
                           private val signalClient: SignalClient,
                           private val metadata: EmailMetadata,
                           private val activeAccount: ActiveAccount,
                           override val publishFn: (EventResult.InsertNewEmail) -> Unit): BackgroundWorker<EventResult.InsertNewEmail> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EventResult.InsertNewEmail {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private fun insertIncomingEmail() {
        EmailInsertionSetup.insertIncomingEmailTransaction(signalClient = signalClient,
                        dao = emailInsertionDao, apiClient = emailInsertionApi,
                        metadata = metadata, activeAccount = activeAccount)
    }

    private fun loadNewEmail(): Email? =
        emailInsertionDao.findEmailByMessageId(metadata.messageId)

    override fun work(reporter: ProgressReporter<EventResult.InsertNewEmail>)
            : EventResult.InsertNewEmail? {
        val result: Result<Email, Exception> = Result.of {
            insertIncomingEmail()
            loadNewEmail()!!
        }

        return when (result) {
            is Result.Success ->  EventResult.InsertNewEmail.Success(result.value)
            is Result.Failure ->  handleFailure(result.error)
        }

    }

    private fun handleFailure(exception: Exception): EventResult.InsertNewEmail{
        if(exception is DuplicateMessageException){
            return EventResult.InsertNewEmail.Success(
                    ExistingEmailUpdateSetup.updateExistingEmailTransaction(metadata = metadata,
                            dao = emailInsertionDao, activeAccount = activeAccount))
        }
        val errorMessage = exception.message ?: exception.javaClass.name
        val message = UIMessage(R.string.insert_try_again_error, arrayOf(errorMessage))
        return EventResult.InsertNewEmail.Failure(message)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}
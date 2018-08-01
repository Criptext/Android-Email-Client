package com.criptext.mail.websocket.data

import com.criptext.mail.R
import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.CRFile
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.scenes.mailbox.data.ExistingEmailUpdateSetup
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.UIMessage
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
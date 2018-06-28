package com.email.scenes.mailbox.data

import com.email.api.HttpClient
import com.email.db.MailboxLocalDB
import com.email.db.dao.EmailDao
import com.email.db.dao.EmailInsertionDao
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.ActiveAccount
import com.email.mocks.MockedWorkRunner
import com.email.signal.SignalClient
import com.email.utils.runTransactionsAsTheyAreInvoked
import io.mockk.mockk
import org.junit.Before

/**
 * Created by gabriel on 5/8/18.
 */

open class MailboxWorkerTest {

    protected lateinit var signalClient: SignalClient
    protected lateinit var httpClient: HttpClient
    protected lateinit var emailDao: EmailDao
    protected lateinit var rawSessionDao: RawSessionDao
    protected lateinit var db: MailboxLocalDB
    protected lateinit var dao: EmailInsertionDao
    protected lateinit var activeAccount: ActiveAccount
    protected lateinit var dataSource: MailboxDataSource
    protected lateinit var runner: MockedWorkRunner
    protected var lastResult: MailboxResult? = null
    protected var userEmail = "gabriel@criptext.com"

    @Before
    fun setup() {
        lastResult = null
        activeAccount = ActiveAccount(name = "Gabriel", recipientId = "gabriel", deviceId = 2,
                jwt = "__JWTOKEN__")
        signalClient = mockk()
        db = mockk()
        emailDao = mockk()
        dao = mockk(relaxed = true)
        dao.runTransactionsAsTheyAreInvoked()

        httpClient = mockk()
        runner = MockedWorkRunner()
        rawSessionDao = mockk()
        dataSource = MailboxDataSource(signalClient = signalClient, httpClient = httpClient,
                activeAccount = activeAccount, mailboxLocalDB = db, emailDao = emailDao,
                emailInsertionDao = dao, rawSessionDao = rawSessionDao, runner = runner)
        dataSource.listener = { result -> lastResult = result }
    }

}
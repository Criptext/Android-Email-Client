package com.email.scenes.mailbox.data

import com.email.api.HttpClient
import com.email.db.MailboxLocalDB
import com.email.db.dao.*
import com.email.db.dao.signal.RawIdentityKeyDao
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
    private lateinit var feedItemDao: FeedItemDao
    private lateinit var contactDao: ContactDao
    private lateinit var fileDao: FileDao
    private lateinit var fileKeyDao: FileKeyDao
    private lateinit var labelDao: LabelDao
    private lateinit var emailLabelDao: EmailLabelDao
    private lateinit var emailContactDao: EmailContactJoinDao
    protected lateinit var rawSessionDao: RawSessionDao
    protected lateinit var rawIdentityKeyDao: RawIdentityKeyDao
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
                jwt = "__JWTOKEN__", signature = "")
        signalClient = mockk()
        db = mockk()
        emailDao = mockk()
        feedItemDao = mockk()
        contactDao = mockk()
        fileDao = mockk()
        fileKeyDao = mockk()
        labelDao = mockk()
        emailLabelDao = mockk()
        emailContactDao = mockk()
        dao = mockk(relaxed = true)
        dao.runTransactionsAsTheyAreInvoked()

        httpClient = mockk()
        runner = MockedWorkRunner()
        rawSessionDao = mockk()
        rawIdentityKeyDao = mockk()
        dataSource = MailboxDataSource(signalClient = signalClient, httpClient = httpClient,
                activeAccount = activeAccount, mailboxLocalDB = db, emailDao = emailDao,
                emailInsertionDao = dao, rawSessionDao = rawSessionDao, runner = runner,
                feedItemDao = feedItemDao, contactDao = contactDao, fileDao = fileDao,
                labelDao = labelDao, emailLabelDao = emailLabelDao, emailContactJoinDao = emailContactDao,
                fileKeyDao = fileKeyDao, rawIdentityKeyDao = rawIdentityKeyDao)
        dataSource.listener = { result -> lastResult = result }
    }

}
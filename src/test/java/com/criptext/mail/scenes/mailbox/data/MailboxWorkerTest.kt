package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.dao.signal.RawIdentityKeyDao
import com.criptext.mail.db.dao.signal.RawSessionDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.mocks.MockedWorkRunner
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.runTransactionsAsTheyAreInvoked
import io.mockk.mockk
import org.junit.Before
import java.io.File

/**
 * Created by gabriel on 5/8/18.
 */

open class MailboxWorkerTest {

    protected lateinit var signalClient: SignalClient
    protected lateinit var httpClient: HttpClient
    protected lateinit var emailDao: EmailDao
    protected lateinit var pendingDao: PendingEventDao
    private lateinit var feedItemDao: FeedItemDao
    private lateinit var contactDao: ContactDao
    private lateinit var accountDao: AccountDao
    private lateinit var fileDao: FileDao
    private lateinit var fileKeyDao: FileKeyDao
    private lateinit var labelDao: LabelDao
    private lateinit var emailLabelDao: EmailLabelDao
    private lateinit var emailContactDao: EmailContactJoinDao
    protected lateinit var rawSessionDao: RawSessionDao
    protected lateinit var rawIdentityKeyDao: RawIdentityKeyDao
    protected lateinit var db: MailboxLocalDB
    protected lateinit var appDB: AppDatabase
    protected lateinit var eventDB: EventLocalDB
    protected lateinit var dao: EmailInsertionDao
    protected lateinit var activeAccount: ActiveAccount
    protected lateinit var dataSource: MailboxDataSource
    protected lateinit var storage: KeyValueStorage
    protected lateinit var runner: MockedWorkRunner
    protected var lastResult: MailboxResult? = null
    protected var userEmail = "gabriel@criptext.com"

    @Before
    fun setup() {
        lastResult = null
        activeAccount = ActiveAccount(name = "Gabriel", recipientId = "gabriel", deviceId = 2,
                jwt = "__JWTOKEN__", signature = "", refreshToken = "__REFRESH__", id = 1,
                domain = Contact.mainDomain)
        signalClient = mockk()
        db = mockk()
        appDB = mockk()
        storage = mockk(relaxed = true)
        emailDao = mockk()
        feedItemDao = mockk()
        contactDao = mockk()
        fileDao = mockk()
        fileKeyDao = mockk()
        labelDao = mockk()
        emailLabelDao = mockk()
        emailContactDao = mockk()
        accountDao = mockk()
        pendingDao = mockk()
        dao = mockk(relaxed = true)
        dao.runTransactionsAsTheyAreInvoked()
        eventDB = mockk(relaxed = true)
        httpClient = mockk()
        runner = MockedWorkRunner()
        rawSessionDao = mockk()
        rawIdentityKeyDao = mockk()
        dataSource = MailboxDataSource(signalClient = signalClient, httpClient = httpClient,
                activeAccount = activeAccount, mailboxLocalDB = db, emailDao = emailDao,
                emailInsertionDao = dao, rawSessionDao = rawSessionDao, runner = runner,
                feedItemDao = feedItemDao, contactDao = contactDao, fileDao = fileDao,
                labelDao = labelDao, emailLabelDao = emailLabelDao, emailContactJoinDao = emailContactDao,
                fileKeyDao = fileKeyDao, rawIdentityKeyDao = rawIdentityKeyDao, accountDao = accountDao,
                eventLocalDB = eventDB, storage = storage, pendingDao = pendingDao, filesDir = File("mock"),
                db = appDB)
        dataSource.listener = { result -> lastResult = result }
    }

}
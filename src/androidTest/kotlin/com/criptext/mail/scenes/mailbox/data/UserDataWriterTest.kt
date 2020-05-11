package com.criptext.mail.scenes.mailbox.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.db.*
import com.criptext.mail.db.models.*
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.generaldatasource.data.BackupFileMetadata
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import io.mockk.mockk
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
class UserDataWriterTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase

    private val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Android)
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1,
            domain = Contact.mainDomain, type = AccountTypes.STANDARD, blockRemoteContent = true)
    private val storage: KeyValueStorage = mockk(relaxed = true)

    val nowDate = Calendar.getInstance().time
    val strDate = DateAndTimeUtils.printDateWithServerFormat(nowDate)

    private val bobContact = Contact(email = "bob@criptext.com", name = "Bob", id = 1,
            isTrusted = false, score = 0, spamScore = 0)
    private val joeContact = Contact(email = "joe@criptext.com", name = "Joe", id = 2,
            isTrusted = false, score = 0, spamScore = 0)

    private val labelOne = Label(id = 1, color = "red", text = "Custom Label 1", type = LabelTypes.CUSTOM,
            visible = true, uuid = "uuid1", accountId = activeAccount.id)
    private val labelTwo = Label(id = 2, color = "blue", text = "Custom Label 2", type = LabelTypes.CUSTOM,
            visible = true, uuid = "uuid2", accountId = activeAccount.id)

    private val emailOne = Email(id = 1, content = "contents 1", date = nowDate,
            delivered = DeliveryTypes.DELIVERED, messageId = "id_1", metadataKey = 123,
            preview = "cont", secure = true, subject = "subject 1", threadId = "", unread = true, unsentDate = nowDate,
            trashDate = nowDate,
            fromAddress = "bob@criptext.com", replyTo = null, boundary = null, accountId = activeAccount.id)
    private val emailTwo = Email(id = 2, content = "contents 2", date = nowDate,
            delivered = DeliveryTypes.DELIVERED, messageId = "id_2", metadataKey = 456,
            preview = "cont", secure = true, subject = "subject 2", threadId = "", unread = true, unsentDate = nowDate,
            trashDate = nowDate,
            fromAddress = "bob@criptext.com", replyTo = null, boundary = null, accountId = activeAccount.id)

    private val fileOne = CRFile(id = 1, date = nowDate, emailId = 1, name = "this.txt",
            size = 12, status = 0, token = "txt", shouldDuplicate = false, fileKey = "__FILE_KEY__",
            cid = null)
    private val fileTwo = CRFile(id = 2, date = nowDate, emailId = 2, name = "that.txt",
            size = 14, status = 0, token = "txt", shouldDuplicate = false, fileKey = "__FILE_KEY__",
            cid = null)

    private val aliasOne = Alias(id = 1, name = "alias1", domain = null, active = true, rowId = 1, accountId = activeAccount.id)
    private val aliasTwo = Alias(id = 2, name = "alias2", domain = "custom.com", active = true, rowId = 2, accountId = activeAccount.id)

    private val domainOne = CustomDomain(id = 1, name = "custom.com", validated = true, rowId = 1, accountId = activeAccount.id)

    private val emailLabel1 = EmailLabel(emailId = 1, labelId = 1)
    private val emailLabel2 = EmailLabel(emailId = 2, labelId = 2)

    private val emailContact1 = EmailContact(id = 1, emailId = 1, contactId = 1, type = ContactTypes.TO)
    private val emailContact2 = EmailContact(id = 2, emailId = 2, contactId = 2, type = ContactTypes.FROM)

    private val fileKey1 = FileKey(id = 1, emailId = 1, key = "test_key_16bytes:test_iv_16_bytes")
    private val fileKey2 = FileKey(id = 2, emailId = 2, key = "test_key_16bytes:test_iv_16_bytes")
    private val metadata = BackupFileMetadata(
            fileVersion = UserDataWriter.FILE_SYNC_VERSION,
            recipientId = activeAccount.recipientId,
            domain = activeAccount.domain,
            signature = activeAccount.signature,
            darkTheme = false,
            hasCriptextFooter = true,
            language = Locale.getDefault().language,
            showPreview = false)

    private val deviceLinkFileExpectedContent = listOf(BackupFileMetadata.toJSON(metadata).toString(),
    "{\"table\":\"contact\",\"object\":{\"id\":1,\"email\":\"bob@criptext.com\",\"name\":\"Bob\",\"isTrusted\":false,\"spamScore\":0}}",
    "{\"table\":\"contact\",\"object\":{\"id\":2,\"email\":\"joe@criptext.com\",\"name\":\"Joe\",\"isTrusted\":false,\"spamScore\":0}}",
    "{\"table\":\"label\",\"object\":{\"id\":1,\"color\":\"red\",\"text\":\"Custom Label 1\",\"type\":\"custom\",\"uuid\":\"uuid1\",\"visible\":true}}",
    "{\"table\":\"label\",\"object\":{\"id\":2,\"color\":\"blue\",\"text\":\"Custom Label 2\",\"type\":\"custom\",\"uuid\":\"uuid2\",\"visible\":true}}",
    "{\"table\":\"email\",\"object\":{\"id\":1,\"messageId\":\"id_1\",\"threadId\":\"\",\"unread\":true,\"secure\":true,\"content\":\"contents 1\",\"preview\":\"cont\",\"fromAddress\":\"bob@criptext.com\",\"subject\":\"subject 1\",\"status\":6,\"date\":\"$strDate\",\"key\":123,\"unsentDate\":\"$strDate\",\"trashDate\":\"$strDate\"}}",
    "{\"table\":\"email\",\"object\":{\"id\":2,\"messageId\":\"id_2\",\"threadId\":\"\",\"unread\":true,\"secure\":true,\"content\":\"contents 2\",\"preview\":\"cont\",\"fromAddress\":\"bob@criptext.com\",\"subject\":\"subject 2\",\"status\":6,\"date\":\"$strDate\",\"key\":456,\"unsentDate\":\"$strDate\",\"trashDate\":\"$strDate\"}}",
    "{\"table\":\"file\",\"object\":{\"id\":1,\"token\":\"txt\",\"name\":\"this.txt\",\"size\":12,\"status\":0,\"date\":\"$strDate\",\"emailId\":1,\"mimeType\":\"text\\/plain\"}}",
    "{\"table\":\"file\",\"object\":{\"id\":2,\"token\":\"txt\",\"name\":\"that.txt\",\"size\":14,\"status\":0,\"date\":\"$strDate\",\"emailId\":2,\"mimeType\":\"text\\/plain\"}}",
    "{\"table\":\"alias\",\"object\":{\"id\":1,\"name\":\"alias1\",\"rowId\":1,\"active\":true}}",
    "{\"table\":\"alias\",\"object\":{\"id\":2,\"name\":\"alias2\",\"rowId\":2,\"domain\":\"custom.com\",\"active\":true}}",
    "{\"table\":\"customDomain\",\"object\":{\"id\":1,\"name\":\"custom.com\",\"rowId\":1,\"validated\":true}}",
    "{\"table\":\"email_label\",\"object\":{\"emailId\":1,\"labelId\":1}}",
    "{\"table\":\"email_label\",\"object\":{\"emailId\":2,\"labelId\":2}}",
    "{\"table\":\"email_contact\",\"object\":{\"id\":1,\"emailId\":1,\"contactId\":1,\"type\":\"to\"}}",
    "{\"table\":\"email_contact\",\"object\":{\"id\":2,\"emailId\":2,\"contactId\":2,\"type\":\"from\"}}")

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.accountDao().insert(Account(activeAccount.id, activeAccount.recipientId, activeAccount.deviceId,
                activeAccount.name, activeAccount.jwt, activeAccount.refreshToken,
                "_KEY_PAIR_", 0, "", "criptext.com",
                true, true, type = AccountTypes.STANDARD, blockRemoteContent = true,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true, lastTimeBackup = null))
        db.contactDao().insertIgnoringConflicts(bobContact)
        db.contactDao().insertIgnoringConflicts(joeContact)
        db.emailInsertionDao().insertAccountContact(listOf(
                AccountContact(0, activeAccount.id, bobContact.id),
                AccountContact(0, activeAccount.id, joeContact.id)))
        db.labelDao().insertAll(listOf(labelOne,labelTwo))
        db.emailDao().insert(emailOne)
        db.emailDao().insert(emailTwo)
        db.fileDao().insert(fileOne)
        db.fileDao().insert(fileTwo)
        db.aliasDao().insert(aliasOne)
        db.aliasDao().insert(aliasTwo)
        db.customDomainDao().insert(domainOne)
        db.emailLabelDao().insert(emailLabel1)
        db.emailLabelDao().insert(emailLabel2)
        db.emailContactDao().insert(emailContact1)
        db.emailContactDao().insert(emailContact2)
        db.fileKeyDao().insert(fileKey1)
        db.fileKeyDao().insert(fileKey2)
    }

    @Test
    fun should_correctly_save_all_data_from_database_into_link_device_file_with_correct_json_format() {
        val account = db.accountDao().getLoggedInAccount()!!
        val dataWriter = UserDataWriter(db, mActivityRule.activity.filesDir)
        val result = dataWriter.createFile(account, storage)

        val lines: List<String> = File(result).readLines()
        lines `shouldEqual` deviceLinkFileExpectedContent
    }
}
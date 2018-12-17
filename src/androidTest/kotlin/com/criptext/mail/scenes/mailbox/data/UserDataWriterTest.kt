package com.criptext.mail.scenes.mailbox.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.db.*
import com.criptext.mail.db.models.*
import com.criptext.mail.signal.*
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.text.SimpleDateFormat

@RunWith(AndroidJUnit4::class)
class UserDataWriterTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase

    private val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Android)

    private val bobContact = Contact(email = "bob@criptext.com", name = "Bob", id = 1)
    private val joeContact = Contact(email = "joe@criptext.com", name = "Joe", id = 2)

    private val labelOne = Label(id = 1, color = "red", text = "Custom Label 1", type = LabelTypes.CUSTOM, visible = true)
    private val labelTwo = Label(id = 2, color = "blue", text = "Custom Label 2", type = LabelTypes.CUSTOM, visible = true)

    private val emailOne = Email(id = 1, content = "contents 1", date = SimpleDateFormat("dd/MM/yyyy").parse("21/12/2012"),
            delivered = DeliveryTypes.DELIVERED, isMuted = false, messageId = "id_1", metadataKey = 123,
            preview = "cont", secure = true, subject = "subject 1", threadId = "", unread = true, unsentDate = SimpleDateFormat("dd/MM/yyyy").parse("21/12/2012"),
            trashDate = SimpleDateFormat("dd/MM/yyyy").parse("21/12/2012"))
    private val emailTwo = Email(id = 2, content = "contents 2", date = SimpleDateFormat("dd/MM/yyyy").parse("21/12/2012"),
            delivered = DeliveryTypes.DELIVERED, isMuted = false, messageId = "id_2", metadataKey = 456,
            preview = "cont", secure = true, subject = "subject 2", threadId = "", unread = true, unsentDate = SimpleDateFormat("dd/MM/yyyy").parse("21/12/2012"),
            trashDate = SimpleDateFormat("dd/MM/yyyy").parse("21/12/2012"))

    private val fileOne = CRFile(id = 1, date = SimpleDateFormat("dd/MM/yyyy").parse("21/12/2012"), emailId = 1, name = "this.txt",
            readOnly = true, size = 12, status = 0, token = "txt", shouldDuplicate = false, fileKey = "__FILE_KEY__")
    private val fileTwo = CRFile(id = 2, date = SimpleDateFormat("dd/MM/yyyy").parse("21/12/2012"), emailId = 2, name = "that.txt",
            readOnly = true, size = 14, status = 0, token = "txt", shouldDuplicate = false, fileKey = "__FILE_KEY__")

    private val emailLabel1 = EmailLabel(emailId = 1, labelId = 1)
    private val emailLabel2 = EmailLabel(emailId = 2, labelId = 2)

    private val emailContact1 = EmailContact(id = 1, emailId = 1, contactId = 1, type = ContactTypes.TO)
    private val emailContact2 = EmailContact(id = 2, emailId = 2, contactId = 2, type = ContactTypes.FROM)

    private val fileKey1 = FileKey(id = 1, emailId = 1, key = "test_key_16bytes:test_iv_16_bytes")
    private val fileKey2 = FileKey(id = 2, emailId = 2, key = "test_key_16bytes:test_iv_16_bytes")

    private val deviceLinkFileExpectedContent = listOf("{\"table\":\"contact\",\"object\":{\"id\":1,\"email\":\"bob@criptext.com\",\"name\":\"Bob\"}}",
    "{\"table\":\"contact\",\"object\":{\"id\":2,\"email\":\"joe@criptext.com\",\"name\":\"Joe\"}}",
    "{\"table\":\"label\",\"object\":{\"id\":1,\"color\":\"red\",\"text\":\"Custom Label 1\",\"type\":\"custom\",\"visible\":true}}",
    "{\"table\":\"label\",\"object\":{\"id\":2,\"color\":\"blue\",\"text\":\"Custom Label 2\",\"type\":\"custom\",\"visible\":true}}",
    "{\"table\":\"email\",\"object\":{\"id\":1,\"messageId\":\"id_1\",\"threadId\":\"\",\"unread\":true,\"secure\":true,\"content\":\"contents 1\",\"preview\":\"cont\",\"subject\":\"subject 1\",\"status\":6,\"date\":\"2012-12-21 05:00:00\",\"key\":123,\"isMuted\":false,\"unsentDate\":\"2012-12-21 05:00:00\",\"trashDate\":\"2012-12-21 05:00:00\"}}",
    "{\"table\":\"email\",\"object\":{\"id\":2,\"messageId\":\"id_2\",\"threadId\":\"\",\"unread\":true,\"secure\":true,\"content\":\"contents 2\",\"preview\":\"cont\",\"subject\":\"subject 2\",\"status\":6,\"date\":\"2012-12-21 05:00:00\",\"key\":456,\"isMuted\":false,\"unsentDate\":\"2012-12-21 05:00:00\",\"trashDate\":\"2012-12-21 05:00:00\"}}",
    "{\"table\":\"file\",\"object\":{\"id\":1,\"token\":\"txt\",\"name\":\"this.txt\",\"size\":12,\"status\":0,\"date\":\"2012-12-21 05:00:00\",\"readOnly\":true,\"emailId\":1,\"mimeType\":\"text\\/plain\"}}",
    "{\"table\":\"file\",\"object\":{\"id\":2,\"token\":\"txt\",\"name\":\"that.txt\",\"size\":14,\"status\":0,\"date\":\"2012-12-21 05:00:00\",\"readOnly\":true,\"emailId\":2,\"mimeType\":\"text\\/plain\"}}",
    "{\"table\":\"email_label\",\"object\":{\"emailId\":1,\"labelId\":1}}",
    "{\"table\":\"email_label\",\"object\":{\"emailId\":2,\"labelId\":2}}",
    "{\"table\":\"email_contact\",\"object\":{\"id\":1,\"emailId\":1,\"contactId\":1,\"type\":\"to\"}}",
    "{\"table\":\"email_contact\",\"object\":{\"id\":2,\"emailId\":2,\"contactId\":2,\"type\":\"from\"}}",
    "{\"table\":\"filekey\",\"object\":{\"id\":1,\"key\":\"test_key_16bytes\",\"iv\":\"test_iv_16_bytes\",\"emailId\":1}}",
    "{\"table\":\"filekey\",\"object\":{\"id\":2,\"key\":\"test_key_16bytes\",\"iv\":\"test_iv_16_bytes\",\"emailId\":2}}")

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.contactDao().insertIgnoringConflicts(bobContact)
        db.contactDao().insertIgnoringConflicts(joeContact)
        db.labelDao().insertAll(listOf(labelOne,labelTwo))
        db.emailDao().insert(emailOne)
        db.emailDao().insert(emailTwo)
        db.fileDao().insert(fileOne)
        db.fileDao().insert(fileTwo)
        db.emailLabelDao().insert(emailLabel1)
        db.emailLabelDao().insert(emailLabel2)
        db.emailContactDao().insert(emailContact1)
        db.emailContactDao().insert(emailContact2)
        db.fileKeyDao().insert(fileKey1)
        db.fileKeyDao().insert(fileKey2)
    }

    @Test
    fun should_correctly_save_all_data_from_database_into_link_device_file_with_correct_json_format() {
        val dataWriter = UserDataWriter(db)
        val result = dataWriter.createFile()

        val lines: List<String> = File(result).readLines()
        lines `shouldEqual` deviceLinkFileExpectedContent
    }
}
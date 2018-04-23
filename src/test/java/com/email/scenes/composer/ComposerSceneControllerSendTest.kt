package com.email.scenes.composer

import com.email.R
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.*
import com.email.db.dao.*
import com.email.db.models.*
import com.email.mocks.MockedIHostActivity
import com.email.scenes.composer.data.ComposerAPIClient
import com.email.scenes.composer.data.ComposerDataSource
import com.email.scenes.composer.data.ComposerResult
import com.email.scenes.composer.mocks.MockedComposerScene
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

/**
 * Created by danieltigse on 4/19/18.
 */

@RunWith(RobolectricTestRunner::class)
class ComposerSceneControllerSendTest {

    private lateinit var apiClient: ComposerAPIClient
    private lateinit var scene: MockedComposerScene
    private lateinit var model: ComposerModel
    private lateinit var controller: ComposerController
    private lateinit var dataSource: ComposerDataSource
    private lateinit var host: MockedIHostActivity

    @Before
    fun createComposerSceneController() {
        apiClient = ComposerAPIClient("")
        scene = MockedComposerScene()
        host = MockedIHostActivity()
        model = ComposerModel(fullEmail = null, composerType = null)

        val db = ComposerLocalDB(object : ContactDao{
            override fun insert(contact: Contact) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun insertAll(users: List<Contact>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getAll(): List<Contact> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getLoggedInUser(): Contact? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun deleteAll(contacts: List<Contact>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }, object : EmailDao{
            override fun insertAll(emails: List<Email>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getAll(): List<Email> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getLatestEmails(): List<Email> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getNotArchivedEmailThreads(): List<Email> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getEmailThreadsFromMailboxLabel(starterDate: Date, rejectedLabels: List<Int>, selectedLabel: Int, offset: Int): List<Email> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getLatestEmailFromThreadId(threadId: String): Email {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun deleteAll(emails: List<Email>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun toggleRead(id: Int, unread: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun updateEmail(id: Int, threadId: String, key: String, date: Date) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun update(emails: List<Email>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getEmailsFromThreadId(threadId: String): List<Email> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun insert(email: Email): Long {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun changeDeliveryType(id: Int, deliveryType: DeliveryTypes) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getInitialEmailThreadsFromMailboxLabel(rejectedLabels: List<Int>, selectedLabel: Int, offset: Int): List<Email> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }, object : LabelDao{
            override fun insertAll(labels: List<Label>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getAll(): List<Label> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun deleteMultipleLabels(labels: List<Label>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun delete(label: Label) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun get(labelTextType: MailFolders): Label {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun get(labelTextTypes: List<String>): List<Label> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        },object : EmailLabelDao{
            override fun insert(emailLabel: EmailLabel) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getEmailsFromLabel(labelId: Int): List<Email> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getLabelsFromEmail(emailId: Int): List<Label> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getLabelsFromEmailThreadId(threadId: String): List<Label> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun insertAll(emailLabels: List<EmailLabel>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getAll(): List<EmailLabel> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun deleteAll(emailLabels: List<EmailLabel>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun deleteByEmailLabelIds(labelId: Int, emailId: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun deleteRelationByEmailIds(emailIds: List<Int>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }, object : EmailContactJoinDao{
            override fun getEmailsFromContact(contactId: String): List<Email> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getContactsFromEmail(emailId: Int, contactType: ContactTypes): List<Contact> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun insertAll(emailUsers: List<EmailContact>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getAll(): List<EmailContact> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun insert(emailContact: EmailContact) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun deleteAll(emailUsers: List<EmailContact>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        dataSource = ComposerDataSource(db, ActiveAccount("daniel", ""),
                AsyncTaskWorkRunner())
        controller = ComposerController(model, scene, host, dataSource)
    }

    private fun clickSendButton() {
        controller.onOptionsItemSelected(R.id.composer_send)
    }

    @Test
    fun `On send btn clicked, should save mail with no errors if mail has recipients`() {

        val data = scene.getDataInputByUser()
        controller.updateModelWithInputData(data)
        controller.onStart(null)
        clickSendButton()
        scene.showedError `should be` false
    }

    private fun runAfterSuccessfullyClickingSendButton(fn: () -> Unit) {
        val data = scene.getDataInputByUser()
        controller.updateModelWithInputData(data)
        controller.onStart(null)
        clickSendButton()
        fn()
    }

    private fun simulateMailSaveEvent(result: ComposerResult.SaveEmail) {
        controller.onEmailSavesAsDraft(result)
    }

    @Test
    fun `after receiving ack of mail saved without errors, should exit scene and let MailBox send the email`() {
        runAfterSuccessfullyClickingSendButton {

            //simulate ack without errors
            simulateMailSaveEvent(ComposerResult.SaveEmail.Success(1, false))

            host.isFinished `should be` true

        }
    }

    @Test
    fun `after receiving ack of failed to save mail, should show an error message`() {
        runAfterSuccessfullyClickingSendButton {

            scene.showedError `should be` false

            //simulate send operation failed
            simulateMailSaveEvent(ComposerResult.SaveEmail.Failure())

            scene.showedError `should be` true
        }
    }
}
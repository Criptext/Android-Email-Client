package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.*
import com.criptext.mail.utils.EmailUtils
import java.io.File

abstract class BackupDataWriter<T>(private val batchSize: Int) : Flushable {

    private val dataList: MutableList<T> = mutableListOf()

    protected abstract fun deserialize(item: String): T
    protected abstract fun  writeBatch(batch: List<T>)

    // calls deserialize and and adds item to batch. inserts to db when batch is ready
    fun insert(item: String){
        val valueToInsert = deserialize(item)
        if(dataList.size == batchSize){
            writeBatch(dataList)
            dataList.clear()
        }
        dataList.add(valueToInsert)
    }

    override fun flush(){
        if(dataList.isNotEmpty()) {
            writeBatch(dataList)
            dataList.clear()
        }
    }
}

class ContactDataWriter(private val contactDao: ContactDao,
                        private val accountContactDao: AccountContactDao,
                        private val dependencies: List<Flushable> = listOf(),
                        private val activeAccount: ActiveAccount,
                        private val dataMapper: UserDataWriter.DataMapper):
        BackupDataWriter<Contact>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): Contact {
        return Contact.fromJSON(item)
    }

    override fun writeBatch(batch: List<Contact>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }

        val existingContacts = contactDao.getContactByEmails(batch.map { it.email })
        existingContacts.forEach { existingContact ->
            val matchedContact = batch.find { it.email == existingContact.email }
            if(matchedContact != null)
                dataMapper.idsMap[matchedContact.id] = existingContact.id
        }
        val newContacts = batch.filter { batchContact -> batchContact.email !in existingContacts.map { it.email } }
        val oldIds = mutableListOf<Long>()
        newContacts.forEach {
            oldIds.add(it.id)
            it.id = 0
        }
        val newIds = contactDao.insertAll(newContacts)

        oldIds.forEach {
            dataMapper.idsMap[it] = newIds[oldIds.indexOf(it)]
        }
        val accountContacts = dataMapper.idsMap.keys.map { AccountContact(0, accountId = activeAccount.id, contactId = dataMapper.idsMap[it]!!) }
        accountContactDao.insert(accountContacts)

    }
}

class LabelDataWriter(private val labelDao: LabelDao,
                      private val dependencies: List<Flushable> = listOf(),
                      private val activeAccount: ActiveAccount,
                      private val dataMapper: UserDataWriter.DataMapper):
        BackupDataWriter<Label>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): Label {
        return Label.fromJSON(item, activeAccount.id)
    }

    override fun writeBatch(batch: List<Label>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }


        val oldIds = mutableListOf<Long>()
        batch.forEach {
            oldIds.add(it.id)
            it.id = 0
        }
        val newIds = labelDao.insertAll(batch)

        oldIds.forEach {
            dataMapper.idsMap[it] = newIds[oldIds.indexOf(it)]
        }
    }
}

class EmailDataWriter(private val emailDao: EmailDao,
                      private val dependencies: List<Flushable> = listOf(),
                      private val activeAccount: ActiveAccount,
                      private val filesDir: File,
                      private val dataMapper: UserDataWriter.DataMapper):
        BackupDataWriter<Email>(UserDataWriter.EMAIL_BATCH_SIZE){
    override fun deserialize(item: String): Email {
        return Email.fromJSON(item, activeAccount.id)
    }

    override fun writeBatch(batch: List<Email>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        insertAllEmails(batch)
    }

    private fun insertAllEmails(batch: List<Email>){
        val oldIds = mutableListOf<Long>()
        batch.forEach {
            EmailUtils.saveEmailInFileSystem(
                    filesDir = filesDir,
                    recipientId = activeAccount.recipientId,
                    domain = activeAccount.domain,
                    metadataKey = it.metadataKey,
                    content = it.content,
                    headers = it.headers)
            oldIds.add(it.id)
            it.id = 0
        }

        val emails = batch.map { it.copy(content = "") }

        val newIds = emailDao.insertAll(emails)

        oldIds.forEach {
            dataMapper.idsMap[it] = newIds[oldIds.indexOf(it)]
        }

    }
}

class FileDataWriter(private val fileDao: FileDao,
                     private val dependencies: List<Flushable> = listOf(),
                     private val emailDataMapper: UserDataWriter.DataMapper):
        BackupDataWriter<CRFile>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): CRFile {
        return CRFile.fromJSON(item)
    }

    override fun writeBatch(batch: List<CRFile>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }

        batch.forEach {
            it.id = 0
            it.emailId = emailDataMapper.idsMap[it.emailId]!!
        }

        fileDao.insertAll(batch)
    }
}

class AliasDataWriter(private val aliasDao: AliasDao,
                      private val dependencies: List<Flushable> = listOf(),
                      private val activeAccount: ActiveAccount):
        BackupDataWriter<Alias>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): Alias {
        return Alias.fromJSON(item, activeAccount.id)
    }

    override fun writeBatch(batch: List<Alias>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }

        aliasDao.insertAll(batch)
    }
}

class CustomDomainDataWriter(private val customDomain: CustomDomainDao,
                      private val dependencies: List<Flushable> = listOf(),
                      private val activeAccount: ActiveAccount):
        BackupDataWriter<CustomDomain>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): CustomDomain {
        return CustomDomain.fromJSON(item, activeAccount.id)
    }

    override fun writeBatch(batch: List<CustomDomain>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }

        customDomain.insertAll(batch)
    }
}

class EmailLabelDataWriter(private val emailLabelDao: EmailLabelDao,
                           private val dependencies: List<Flushable> = listOf(),
                           private val emailDataMapper: UserDataWriter.DataMapper,
                           private val labelDataMapper: UserDataWriter.DataMapper):
        BackupDataWriter<EmailLabel>(UserDataWriter.RELATIONS_BATCH_SIZE){
    override fun deserialize(item: String): EmailLabel {
        return EmailLabel.fromJSON(item)
    }

    override fun writeBatch(batch: List<EmailLabel>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        batch.forEach { emailLabel ->
            if(emailLabel.labelId !in Label.defaultItems.toList().map { it.id }) {
                emailLabel.labelId = labelDataMapper.idsMap[emailLabel.labelId]!!
            }
            emailLabel.emailId = emailDataMapper.idsMap[emailLabel.emailId]!!
        }
        emailLabelDao.insertAll(batch)
    }
}

class EmailContactDataWriter(private val emailContactDao: EmailContactJoinDao,
                             private val dependencies: List<Flushable> = listOf(),
                             private val emailDataMapper: UserDataWriter.DataMapper,
                             private val contactDataMapper: UserDataWriter.DataMapper):
        BackupDataWriter<EmailContact>(UserDataWriter.RELATIONS_BATCH_SIZE){
    override fun deserialize(item: String): EmailContact {
        return EmailContact.fromJSON(item)
    }

    override fun writeBatch(batch: List<EmailContact>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }

        batch.forEach {
            it.id = 0
            it.contactId = contactDataMapper.idsMap[it.contactId]!!
            it.emailId = emailDataMapper.idsMap[it.emailId]!!
        }

        emailContactDao.insertAll(batch)
    }
}

interface Flushable{
    fun flush()
}
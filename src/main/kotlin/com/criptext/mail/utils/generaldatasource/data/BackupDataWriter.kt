package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.*

abstract class BackupDataWriter<T>(private val batchSize: Int) : Flushable {

    private val dataList: MutableList<T> = mutableListOf()

    protected abstract fun deserialize(item: String): T
    protected abstract fun writeBatch(batch: List<T>)

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
        if(dataList.size > 0) {
            writeBatch(dataList)
            dataList.clear()
        }
    }
}

class ContactDataWriter(private val contactDao: ContactDao,
                        private val dependencies: List<Flushable> = listOf()):
        BackupDataWriter<Contact>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): Contact {
        return Contact.fromJSON(item)
    }

    override fun writeBatch(batch: List<Contact>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        contactDao.insertAll(batch)
    }
}

class LabelDataWriter(private val labelDao: LabelDao,
                      private val dependencies: List<Flushable> = listOf()):
        BackupDataWriter<Label>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): Label {
        return Label.fromJSON(item)
    }

    override fun writeBatch(batch: List<Label>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        labelDao.insertAll(batch)
    }
}

class EmailDataWriter(private val emailDao: EmailDao,
                      private val dependencies: List<Flushable> = listOf()):
        BackupDataWriter<Email>(UserDataWriter.EMAIL_BATCH_SIZE){
    override fun deserialize(item: String): Email {
        return Email.fromJSON(item)
    }

    override fun writeBatch(batch: List<Email>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        emailDao.insertAll(batch)
    }
}

class FileDataWriter(private val fileDao: FileDao,
                     private val dependencies: List<Flushable> = listOf()):
        BackupDataWriter<CRFile>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): CRFile {
        return CRFile.fromJSON(item)
    }

    override fun writeBatch(batch: List<CRFile>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        fileDao.insertAll(batch)
    }
}

class EmailLabelDataWriter(private val emailLabelDao: EmailLabelDao,
                           private val dependencies: List<Flushable> = listOf()):
        BackupDataWriter<EmailLabel>(UserDataWriter.RELATIONS_BATCH_SIZE){
    override fun deserialize(item: String): EmailLabel {
        return EmailLabel.fromJSON(item)
    }

    override fun writeBatch(batch: List<EmailLabel>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        emailLabelDao.insertAll(batch)
    }
}

class EmailContactDataWriter(private val emailContactDao: EmailContactJoinDao,
                             private val dependencies: List<Flushable> = listOf()):
        BackupDataWriter<EmailContact>(UserDataWriter.RELATIONS_BATCH_SIZE){
    override fun deserialize(item: String): EmailContact {
        return EmailContact.fromJSON(item)
    }

    override fun writeBatch(batch: List<EmailContact>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        emailContactDao.insertAll(batch)
    }
}

class FileKeyDataWriter(private val fileKeyDao: FileKeyDao,
                        private val dependencies: List<Flushable> = listOf()):
        BackupDataWriter<FileKey>(UserDataWriter.DEFAULT_BATCH_SIZE){
    override fun deserialize(item: String): FileKey {
        return FileKey.fromJSON(item)
    }

    override fun writeBatch(batch: List<FileKey>) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach { it.flush() }
        }
        fileKeyDao.insertAll(batch)
    }
}

interface Flushable{
    fun flush()
}
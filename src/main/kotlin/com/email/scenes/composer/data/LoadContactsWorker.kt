package com.email.scenes.composer.data

import com.email.R
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.ComposerLocalDB
import com.email.db.dao.ContactDao
import com.email.utils.UIMessage

/**
 * Created by gabriel on 2/26/18.
 */
class LoadContactsWorker(
        private val db: ComposerLocalDB,
        override val publishFn: (ComposerResult.GetAllContacts) -> Unit)
    : BackgroundWorker<ComposerResult.GetAllContacts> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.GetAllContacts {
        return ComposerResult.GetAllContacts.Failure("Failed to get Contacts")
    }

    override fun work(reporter: ProgressReporter<ComposerResult.GetAllContacts>)
            : ComposerResult.GetAllContacts? {
        val contacts = db.contactDao.getAll()
        return ComposerResult.GetAllContacts.Success(contacts = contacts)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}


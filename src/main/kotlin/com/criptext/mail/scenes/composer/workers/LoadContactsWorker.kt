package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.utils.UIMessage

/**
 * Created by gabriel on 2/26/18.
 */
class LoadContactsWorker(
        private val db: ComposerLocalDB,
        override val publishFn: (ComposerResult.GetAllContacts) -> Unit)
    : BackgroundWorker<ComposerResult.GetAllContacts> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.GetAllContacts {
        return ComposerResult.GetAllContacts.Failure(UIMessage(R.string.contact_db_get_error))
    }

    override fun work(reporter: ProgressReporter<ComposerResult.GetAllContacts>)
            : ComposerResult.GetAllContacts? {
        val contacts = db.contactDao.getAll().sortedByDescending { it.score }
        return ComposerResult.GetAllContacts.Success(contacts = contacts)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}


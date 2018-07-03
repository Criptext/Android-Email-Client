package com.email.db

import com.email.db.dao.*
import com.email.db.models.*
import com.email.scenes.composer.data.ComposerInputData
import com.email.utils.DateUtils
import com.email.utils.HTMLUtils
import java.util.*

/**
 * Created by danieltigse on 4/17/18.
 */

class ComposerLocalDB(val contactDao: ContactDao, val emailDao: EmailDao, val fileDao: FileDao,
                      val labelDao: LabelDao, val emailLabelDao: EmailLabelDao,
                      val emailContactDao: EmailContactJoinDao, val accountDao: AccountDao) {

    fun loadFullEmail(id: Long): FullEmail? {
        val email = emailDao.findEmailById(id) ?: return null
        val labels = emailLabelDao.getLabelsFromEmail(id)
        val contactsCC = emailContactDao.getContactsFromEmail(id, ContactTypes.CC)
        val contactsBCC = emailContactDao.getContactsFromEmail(id, ContactTypes.BCC)
        val contactsFROM = emailContactDao.getContactsFromEmail(id, ContactTypes.FROM)
        val contactsTO = emailContactDao.getContactsFromEmail(id, ContactTypes.TO)
        val files = fileDao.getAttachmentsFromEmail(id)

        return FullEmail(
                email = email,
                bcc = contactsBCC,
                cc = contactsCC,
                from = contactsFROM[0],
                files = files,
                labels = labels,
                to = contactsTO )
    }
}

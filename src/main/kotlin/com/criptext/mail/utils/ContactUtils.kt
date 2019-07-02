package com.criptext.mail.utils

import com.criptext.mail.db.ContactTypes
import com.criptext.mail.db.dao.ContactDao
import com.criptext.mail.db.dao.EmailContactJoinDao
import com.criptext.mail.db.models.Contact

object ContactUtils {
    fun getFromContact(emailContactDao: EmailContactJoinDao, contactDao: ContactDao, accountId: Long,
                       emailId: Long, fromAddress: String): Contact {
        val dbContact = emailContactDao.getContactsFromEmail(emailId, ContactTypes.FROM)
        return if(fromAddress.isEmpty()){
            dbContact[0]
        }else {
            val emailAddress = EmailAddressUtils.extractEmailAddress(fromAddress)
            val name = EmailAddressUtils.extractName(fromAddress)
            val dbFromContact = contactDao.getContact(emailAddress, accountId)
                    ?: dbContact.find { contact ->
                        (contact.email == emailAddress && contact.name == name)
                    }  ?: dbContact[0]
            Contact(
                    id = dbFromContact.id,
                    email = emailAddress,
                    name = name,
                    isTrusted = dbFromContact.isTrusted,
                    score = dbFromContact.score
            )
        }
    }
}
package com.email.db.seeders

import com.email.db.dao.EmailContactJoinDao
import com.email.db.models.EmailContact

/**
 * Created by sebas on 2/7/18.
 */

class EmailContactSeeder {

    companion object {
        var emailContacts : List<EmailContact> = mutableListOf()

        fun seed(emailContactJoinDao: EmailContactJoinDao){
            emailContacts = emailContactJoinDao.getAll()
            emailContactJoinDao.deleteAll(emailContacts)
            emailContacts = mutableListOf()
            for (a in 1..3){
                emailContacts += fillEmailContacts(a)
            }
            emailContactJoinDao.insertAll(emailContacts)
        }


        private fun fillEmailContacts(iteration: Int): EmailContact {
            lateinit var emailContact: EmailContact
            when (iteration) {
                1 -> emailContact = EmailContact(
                        emailId = 1,
                        contactId = "ascacere92@gmail.com",
                        type = "ENTREGA")
                2 -> emailContact = EmailContact( emailId = 2,
                        contactId = "xndres@gmail.com",
                        type = "ENTREGA")

                3 -> emailContact = EmailContact( emailId = 1,
                        contactId = "ascacere92@gmail.com",
                        type = "ENVIO")
            }
            return emailContact
        }
    }
}

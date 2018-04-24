package com.email.db.seeders

import com.email.db.dao.ContactDao
import com.email.db.models.Contact

/**
 * Created by sebas on 2/7/18.
 */

class ContactSeeder {

    companion object {
        var contacts : List<Contact> = mutableListOf()

        fun seed(contactDao: ContactDao){
            contacts = contactDao.getAll()
            contactDao.deleteAll(contacts)
            contacts = mutableListOf()
            for (a in 1..3){
                contacts += fillContact(a)
            }
            contactDao.insertAll(contacts)
        }


        private fun fillContact(iteration: Int): Contact {
            lateinit var contact: Contact
            when (iteration) {
                1 -> contact = Contact(
                        id = 0,
                        name = "Daniel Tigse" ,
                        email = "daniel@criptext.com"
                        )

                2 -> contact = Contact(
                        id = 1,
                        name = "Sebastian Caceres" ,
                        email = "ascacere92@gmail.com")

                3 -> contact = Contact(
                        id = 2,
                        name = "Gabriel Aumala" ,
                        email = "gabriel@criptext.com")
            }
            return contact
        }
    }
}

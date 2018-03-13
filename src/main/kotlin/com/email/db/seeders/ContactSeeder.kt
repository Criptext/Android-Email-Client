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
            for (a in 1..2){
                contacts += fillContact(a)
            }
            contactDao.insertAll(contacts)
        }


        private fun fillContact(iteration: Int): Contact {
            lateinit var contact: Contact
            when (iteration) {
                1 -> contact = Contact(
                        name = "Daniel ItsgeWTF" ,
                        email = "daniel@criptext.com"
                        )

                2 -> contact = Contact(
                        name = "Sebastian Caceres" ,
                        email = "ascacere92@gmail.com")

                3 -> contact = Contact(
                        name = "Gabriel Aumala" ,
                        email = "gabriel@criptext.com")
            }
            return contact
        }
    }
}

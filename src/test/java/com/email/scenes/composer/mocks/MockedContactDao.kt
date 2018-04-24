package com.email.scenes.composer.mocks

import com.email.db.DeliveryTypes
import com.email.db.dao.ContactDao
import com.email.db.models.Contact
import com.email.db.models.Email
import java.util.*

/**
 * Created by gabriel on 4/24/18.
 */

class MockedContactDao : ContactDao {
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

        }
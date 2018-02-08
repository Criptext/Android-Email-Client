package com.email.DB.seeders

import com.email.DB.DAO.EmailUserJoinDao
import com.email.DB.models.EmailUser

/**
 * Created by sebas on 2/7/18.
 */

class EmailUserSeeder {

    companion object {
        var emailUsers : List<EmailUser> = mutableListOf<EmailUser>()

        fun seed(emailUserJoinDao: EmailUserJoinDao){
            emailUsers = emailUserJoinDao.getAll()
            emailUserJoinDao.deleteAll(emailUsers)
            emailUsers = mutableListOf<EmailUser>()
            for (a in 1..3){
                emailUsers += fillEmailUsers(a)
            }
            emailUserJoinDao.insertAll(emailUsers)
        }


        fun fillEmailUsers(iteration: Int): EmailUser {
            lateinit var emailUser: EmailUser
            when (iteration) {
                1 -> emailUser = EmailUser( emailId = 1,
                        userId = 1,
                        type = "ENVIO"
                        )
                2 -> emailUser = EmailUser( emailId = 2,
                        userId = 2,
                        type = "ENTREGA"
                )

                3 -> emailUser = EmailUser( emailId = 1,
                        userId = 2,
                        type = "ENVIO"
                )
            }
            return emailUser
        }
    }
}

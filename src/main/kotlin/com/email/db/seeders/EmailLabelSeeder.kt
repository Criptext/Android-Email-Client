package com.email.db.seeders

import com.email.db.dao.EmailLabelJoinDao
import com.email.db.models.EmailLabel

/**
 * Created by sebas on 1/25/18.
 */

class EmailLabelSeeder{
    companion object {
        var emailLabels : List<EmailLabel> = mutableListOf()

        fun seed(emailLabelDao: EmailLabelJoinDao){
            emailLabels = emailLabelDao.getAll()
            emailLabelDao.deleteAll(emailLabels)
            emailLabels = mutableListOf()
            for (a in 1..10){
                emailLabels += fillEmailLabel(a)
            }
            emailLabelDao.insertAll(emailLabels)
        }


        private fun fillEmailLabel(iteration: Int): EmailLabel {
            lateinit var emailLabel : EmailLabel
            when (iteration) {
                1 -> emailLabel = EmailLabel(emailId = 1,
                        labelId = 1)

                2 -> emailLabel = EmailLabel(emailId = 2,
                        labelId = 2)

                3 -> emailLabel = EmailLabel(emailId = 3,
                        labelId = 3)

                4 -> emailLabel = EmailLabel(emailId = 4,
                        labelId = 4)

                5 -> emailLabel = EmailLabel(emailId = 5,
                        labelId = 5)

                6 -> emailLabel = EmailLabel(emailId = 7,
                        labelId = 6)

                7 -> emailLabel = EmailLabel(emailId = 9,
                        labelId = 6)

                8 -> emailLabel = EmailLabel(emailId = 6,
                        labelId = 8)

                9 -> emailLabel = EmailLabel(emailId = 6,
                        labelId = 10)

                10 -> emailLabel = EmailLabel(emailId = 6,
                        labelId = 9)
            }
            return emailLabel
        }
    }
}

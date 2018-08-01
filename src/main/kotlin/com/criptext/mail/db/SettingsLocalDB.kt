package com.criptext.mail.db

import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.EmailContact
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.utils.DateUtils
import com.criptext.mail.utils.HTMLUtils
import java.util.*

/**
 * Created by danieltigse on 6/14/18.
 */

class SettingsLocalDB(val labelDao: LabelDao, val accountDao: AccountDao, val contactDao: ContactDao)

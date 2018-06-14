package com.email.db

import com.email.db.dao.*
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.EmailContact
import com.email.db.models.EmailLabel
import com.email.scenes.composer.data.ComposerInputData
import com.email.utils.DateUtils
import com.email.utils.HTMLUtils
import java.util.*

/**
 * Created by danieltigse on 6/14/18.
 */

class SettingsLocalDB(val labelDao: LabelDao, val accountDao: AccountDao, val contactDao: ContactDao)

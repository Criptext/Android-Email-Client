package com.email.scenes.mailbox.data

import com.email.bgworker.BackgroundWorker
import com.email.db.DeliveryTypes
import com.email.db.MailboxLocalDB
import com.email.utils.DateUtils
import org.json.JSONObject

/**
 * Created by danieltigse on 4/18/18.
 */

class UpdateEmailWorker(
        private val db: MailboxLocalDB,
        private val emailId: Long,
        private val response: JSONObject,
        override val publishFn: (MailboxResult.UpdateMail) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateMail> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateMail {
        return MailboxResult.UpdateMail.Failure()
    }

    override fun work(): MailboxResult.UpdateMail? {
        db.updateEmailAndAddLabelSent(emailId, response.getString("threadId"),
                response.getString("metadataKey"),
                DateUtils.getDateFromString(response.getString("date"), null),
                DeliveryTypes.SENT)
        return MailboxResult.UpdateMail.Success()
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}


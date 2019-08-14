package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.utils.UIMessage


class LoadFromAddressesWorker(
        private val db: ComposerLocalDB,
        override val publishFn: (ComposerResult.GetAllFromAddresses) -> Unit)
    : BackgroundWorker<ComposerResult.GetAllFromAddresses> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.GetAllFromAddresses {
        return ComposerResult.GetAllFromAddresses.Failure(UIMessage(R.string.from_db_get_error))
    }

    override fun work(reporter: ProgressReporter<ComposerResult.GetAllFromAddresses>)
            : ComposerResult.GetAllFromAddresses? {
        val accounts = db.accountDao.getLoggedInAccounts()
        return ComposerResult.GetAllFromAddresses.Success(accounts = accounts)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}


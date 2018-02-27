package com.email.scenes.composer.data

import com.email.bgworker.BackgroundWorker

/**
 * Created by gabriel on 2/26/18.
 */

class SendMailWorker(override val publishFn: (ComposerResult.SendMail) -> Unit) : BackgroundWorker<ComposerResult.SendMail> {
    override val canBeParallelized = false

    override fun catchException(ex: Exception): ComposerResult.SendMail {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(): ComposerResult.SendMail? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
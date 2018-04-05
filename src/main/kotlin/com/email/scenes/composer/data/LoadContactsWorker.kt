package com.email.scenes.composer.data

import com.email.bgworker.BackgroundWorker

/**
 * Created by gabriel on 2/26/18.
 */
class LoadContactsWorker(override val publishFn: (ComposerResult.SuggestContacts) -> Unit) : BackgroundWorker<ComposerResult.SuggestContacts> {
    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.SuggestContacts {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(): ComposerResult.SuggestContacts? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


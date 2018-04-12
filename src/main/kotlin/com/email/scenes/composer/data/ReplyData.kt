package com.email.scenes.composer.data

import com.email.db.models.FullEmail
import com.email.scenes.composer.ComposerModel

/**
 * Created by danieltigse on 4/16/18.
 */

sealed class ReplyData(val composerType: ComposerTypes, val fullEmail: FullEmail){
    class FromModel(model: ComposerModel): ReplyData(
            composerType = model.composerType!!,
            fullEmail = model.fullEmail!!
    )
}
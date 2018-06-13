package com.email.scenes

import com.email.db.models.FullEmail
import com.email.scenes.composer.ComposerModel
import com.email.scenes.composer.data.ComposerTypes

object ModelFactory {
    private fun createReplyComposerModel(fullParentEmail: FullEmail, type: ComposerTypes,
                                         userEmail: String): ComposerModel {
        val newModel = ComposerModel(fullParentEmail, type)
        newModel.body = fullParentEmail.email.content
        if(fullParentEmail.from.email == userEmail){
            newModel.to.addAll(fullParentEmail.to)
        }
        else{
            newModel.to.add(fullParentEmail.from)
        }
        newModel.subject =
                (if(fullParentEmail.email.subject.matches("^(Re|RE): .*\$".toRegex())) "" else "RE: ") +
                fullParentEmail.email.subject
        return newModel
    }

    private fun createReplyAllComposerModel(fullParentEmail: FullEmail, type: ComposerTypes,
                                            userEmail: String): ComposerModel {
        val newModel = ComposerModel(fullParentEmail, type)
        newModel.body = fullParentEmail.email.content
        if(fullParentEmail.from.email == userEmail){
            newModel.to.addAll(fullParentEmail.to)
        }
        else{
            newModel.to.add(fullParentEmail.from)
            newModel.to.addAll(fullParentEmail.to.filter {
                it.email != userEmail
            })
        }
        newModel.cc.addAll(fullParentEmail.cc)
        newModel.subject =
                (if(fullParentEmail.email.subject.matches("^(Re|RE): .*\$".toRegex())) "" else "RE: ") +
                fullParentEmail.email.subject
        return newModel
    }

    private fun createForwardComposerModel(fullParentEmail: FullEmail, type: ComposerTypes): ComposerModel {
        val newModel = ComposerModel(fullParentEmail, type)
        newModel.body = fullParentEmail.email.content
        newModel.subject =
                (if(fullParentEmail.email.subject.matches("^(Fw|FW): .*\$".toRegex())) "" else "FW: ") +
                fullParentEmail.email.subject
        return newModel
    }

    private fun createDraftComposerModel(fullParentEmail: FullEmail, type: ComposerTypes): ComposerModel {
        val newModel = ComposerModel(fullParentEmail, type)
        newModel.body = fullParentEmail.email.content
        newModel.to.addAll(fullParentEmail.to)
        newModel.cc.addAll(fullParentEmail.cc)
        newModel.bcc.addAll(fullParentEmail.bcc)
        newModel.subject = fullParentEmail.email.subject
        return newModel
    }

    fun createComposerModel(fullParentEmail: FullEmail?, type: ComposerTypes?,
                            userEmail: String?): ComposerModel {
        return if (fullParentEmail != null && type != null && userEmail != null) {
            when (type) {
                ComposerTypes.REPLY -> createReplyComposerModel(fullParentEmail, type, userEmail)
                ComposerTypes.REPLY_ALL -> createReplyAllComposerModel(fullParentEmail, type, userEmail)
                ComposerTypes.FORWARD -> createForwardComposerModel(fullParentEmail, type)
                ComposerTypes.CONTINUE_DRAFT -> createDraftComposerModel(fullParentEmail, type)
            }
        } else ComposerModel()

    }
}
package com.email.scenes.composer.data

/**
 * Created by sebas on 3/27/18.
 */

sealed class ComposerType {
    class Empty(): ComposerType() {
        override fun equals(other: Any?): Boolean = other is Empty
    }
    data class Draft(val draftId: Long): ComposerType()
    data class Reply(val originalId: Long, val threadId: String): ComposerType()
    data class ReplyAll(val originalId: Long, val threadId: String): ComposerType()
    data class Forward(val originalId: Long, val threadId: String): ComposerType()
}
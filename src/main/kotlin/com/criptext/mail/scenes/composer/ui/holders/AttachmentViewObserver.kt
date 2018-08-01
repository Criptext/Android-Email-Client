package com.criptext.mail.scenes.composer.ui.holders

interface AttachmentViewObserver{
    fun onRemoveAttachmentClicked(position: Int)
    fun onAttachmentViewClick(position: Int)
}
package com.criptext.mail.scenes.settings.profile.data

import com.criptext.mail.utils.UIMessage

sealed class ProfileResult{

    sealed class DeleteProfilePicture : ProfileResult() {
        class Success: DeleteProfilePicture()
        data class Failure(val message: UIMessage,
                           val exception: Exception?): DeleteProfilePicture()
        class EnterpriseSuspended: DeleteProfilePicture()
    }

}
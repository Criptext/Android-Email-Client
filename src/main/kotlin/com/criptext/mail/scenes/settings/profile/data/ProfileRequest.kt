package com.criptext.mail.scenes.settings.profile.data

import android.graphics.Bitmap

sealed class ProfileRequest{
    data class SetProfilePicture(val image: Bitmap): ProfileRequest()
    class DeleteProfilePicture: ProfileRequest()
}
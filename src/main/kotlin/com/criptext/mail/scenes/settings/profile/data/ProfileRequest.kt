package com.criptext.mail.scenes.settings.profile.data

import android.graphics.Bitmap

sealed class ProfileRequest{
    class DeleteProfilePicture: ProfileRequest()
}
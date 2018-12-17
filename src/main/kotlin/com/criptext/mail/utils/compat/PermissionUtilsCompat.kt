package com.criptext.mail.utils.compat

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class PermissionUtilsCompat {
    companion object {
        fun checkPermission(context: Context, permission: String): Boolean =
            if (Build.VERSION.SDK_INT >= 23)
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            else true
    }
}
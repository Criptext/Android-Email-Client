package com.email.utils.compat

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat

class PermissionUtilsCompat {
    companion object {
        fun checkPermission(context: Context, permission: String): Boolean =
            if (Build.VERSION.SDK_INT >= 23)
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            else true
    }
}
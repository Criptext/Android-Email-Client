package com.criptext.mail.utils

import com.github.omadahealth.lollipin.lib.managers.LockManager

object PinLockUtils{

    fun enablePinLock(){
        val lockManager = LockManager.getInstance()
        if(lockManager.appLock != null) {
            lockManager.appLock.enable()
        }
    }

    fun disablePinLock(){
        val lockManager = LockManager.getInstance()
        if(lockManager.appLock != null) {
            lockManager.appLock.disable()
        }
    }

    fun resetLastMillisPin(){
        val lockManager = LockManager.getInstance()
        if(lockManager.appLock != null)
            lockManager.appLock.setLastActiveMillis()
    }

    fun setPinLockTimeout(time: Long){
        val lockManager = LockManager.getInstance()
        if(lockManager.appLock != null)
            lockManager.appLock.timeout = time
    }

    fun setPinLockTimeoutPosition(position: Int){
        val lockManager = LockManager.getInstance()
        if(lockManager.appLock != null) {
            lockManager.appLock.timeout = when (position) {
                0 -> 500
                1 -> 60000
                2 -> 5 * 60000
                3 -> 15 * 60000
                4 -> 60 * 60000
                else -> 24 * 60 * 60000
            }
        }
    }

    const val TIMEOUT_TO_DISABLE = 10000000000000
}
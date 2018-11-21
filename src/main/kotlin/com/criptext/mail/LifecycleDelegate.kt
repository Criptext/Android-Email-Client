package com.criptext.mail

interface LifecycleDelegate {
    fun onAppBackgrounded()
    fun onAppForegrounded()
}
package com.criptext.mail.scenes.signin.data

sealed class LinkDeviceState {
    class Begin: LinkDeviceState()
    class Auth: LinkDeviceState()
    class Accepted: LinkDeviceState()
    class Denied: LinkDeviceState()
    class WaitingForDownload: LinkDeviceState()
}
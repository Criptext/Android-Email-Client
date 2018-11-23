package com.criptext.mail.scenes.settings.pinlock.pinscreen

import android.content.Intent
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.getLocalizedUIMessage
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity
import java.util.*


class LockScreenActivity: AppLockActivity(){


    private val lockScreenUIObserver = object : LockScreenUIObserver{
        override fun onForgotPinYesPressed(dataSource: GeneralDataSource) {
            showLoginOutDialog()
            dataSource.submitRequest(GeneralRequest.Logout())
        }

    }

    private fun onLogout(result: GeneralResult.Logout){
        when(result) {
            is GeneralResult.Logout.Success -> {
                val i = baseContext.packageManager
                        .getLaunchIntentForPackage(baseContext.packageName)
                i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(i)
            }
            is GeneralResult.Logout.Failure -> {
                showMessage(UIMessage(R.string.error_login_out))
            }
        }
    }

    override fun onPinSuccess(attempts: Int) {

    }

    override fun onPinFailure(attempts: Int) {
        if(attempts < MAX_ATTEMPTS){
            handleAttepts(MAX_ATTEMPTS - attempts)
        }else{
            showLoginOutDialog()
            getDataSource().submitRequest(GeneralRequest.Logout())
        }
    }

    override fun showForgotDialog() {
        PinForgotDialog(this).showDialog(lockScreenUIObserver, getDataSource())
    }

    override fun getBackableTypes(): List<Int> {
        return Arrays.asList(AppLock.ENABLE_PINLOCK, AppLock.CHANGE_PIN,
                AppLock.DISABLE_PINLOCK, AppLock.UNLOCK_PIN)
    }

    private fun showLoginOutDialog(){
        MessageAndProgressDialog(this, UIMessage(R.string.login_out_dialog_message)).showDialog()
    }

    private fun getDataSource(): GeneralDataSource{
        val storage = KeyValueStorage.SharedPrefs(this)
        val activeAccount = ActiveAccount.loadFromStorage(storage)
        val db = AppDatabase.getAppDatabase(this)
        val dataSourceListener: (GeneralResult) -> Unit = { result ->
            when(result) {
                is GeneralResult.Logout -> onLogout(result)
            }
        }
        return GeneralDataSource(storage = storage, activeAccount = activeAccount,
                db = db, runner = AsyncTaskWorkRunner(),
                eventLocalDB = EventLocalDB(db), httpClient = HttpClient.Default(),
                signalClient = SignalClient.Default(SignalStoreCriptext(db))).also {
            it.listener = dataSourceListener
        }
    }

    private fun handleAttepts(attempts: Int){
        val message = if(attempts > 3)
            UIMessage(R.string.attempts_remaining, arrayOf(attempts))
        else
            UIMessage(R.string.attempts_remaining_critical, arrayOf(attempts))
        showMessage(message)
    }

    private fun showMessage(message: UIMessage){
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(
                this,
                this.getLocalizedUIMessage(message),
                duration)
        toast.show()
    }

    companion object {
        private const val MAX_ATTEMPTS = 10
    }
}
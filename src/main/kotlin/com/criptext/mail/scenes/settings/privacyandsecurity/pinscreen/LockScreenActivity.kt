package com.criptext.mail.scenes.settings.privacyandsecurity.pinscreen

import android.content.Intent
import androidx.core.content.ContextCompat
import android.widget.TextView
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

    private val warningText: TextView by lazy {
        this.findViewById<TextView>(R.id.warning_text)
    }


    private val lockScreenUIObserver = object : LockScreenUIObserver{
        override fun onForgotPinYesPressed(dataSource: GeneralDataSource) {
            showLoginOutDialog()
            dataSource.submitRequest(GeneralRequest.Logout(false))
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
            handleAttempts(MAX_ATTEMPTS - attempts)
        }else{
            showLoginOutDialog()
            getDataSource().submitRequest(GeneralRequest.Logout(true))
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
                eventLocalDB = EventLocalDB(db, this.filesDir), httpClient = HttpClient.Default(),
                signalClient = SignalClient.Default(SignalStoreCriptext(db)),
                filesDir = this.filesDir).also {
            it.listener = dataSourceListener
        }
    }

    private fun handleAttempts(attempts: Int){
        val message = if(attempts > 3) {
            UIMessage(R.string.attempts_remaining, arrayOf(attempts))
        }else {
            warningText.setBackgroundResource(R.drawable.pin_warning_background)
            warningText.setTextColor(ContextCompat.getColor(this, R.color.white))
            UIMessage(R.string.attempts_remaining_critical, arrayOf(attempts))
        }
        showMessage(message)
    }

    private fun showMessage(message: UIMessage){
        warningText.text = this.getLocalizedUIMessage(message)
    }

    companion object {
        private const val MAX_ATTEMPTS = 10
    }
}
package com.criptext.mail.scenes.syncing

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.scenes.syncing.holders.*
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.RetryManualSyncAlertDialogNewDevice


interface SyncingScene{

    fun attachView(model: SyncingModel, syncingUIObserver: SyncingUIObserver)
    fun showMessage(message : UIMessage)
    fun setProgress(progress: Int, onFinish:(() -> Unit)? = null)
    fun setProgressStatus(message: UIMessage, drawable: Int? = null)
    fun disableSkip()
    fun showRetrySyncDialog(result: GeneralResult)


    var syncingUIObserver: SyncingUIObserver?

    class Default(private val view: View): SyncingScene {

        private val context = view.context

        override var syncingUIObserver: SyncingUIObserver? = null
        private val viewGroup = view.parent as ViewGroup
        private var holder: BaseSyncingHolder? = null

        private val retrySyncDialog: RetryManualSyncAlertDialogNewDevice = RetryManualSyncAlertDialogNewDevice(context)

        override fun attachView(model: SyncingModel, syncingUIObserver: SyncingUIObserver) {
            this.syncingUIObserver = syncingUIObserver
            removeAllViews()
            val state = model.state
            holder = when (state) {
                is SyncingLayoutState.SyncBegin -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sync_import_begin, viewGroup)
                    SyncBeginHolder(newLayout)
                }
                is SyncingLayoutState.SyncRejected -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sync_import_denied, viewGroup)
                    SyncDeniedHolder(newLayout)
                }
                is SyncingLayoutState.SyncImport -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sync_import_restore, viewGroup)
                    SyncImportHolder(newLayout)
                }
            }
        }

        private fun removeAllViews() {
            viewGroup.removeAllViews()
            holder?.uiObserver = null
        }

        override fun setProgress(progress: Int, onFinish: (() -> Unit)?) {
            val currentHolder = holder as SyncImportHolder
            currentHolder.setProgress(progress, onFinish)
        }

        override fun setProgressStatus(message: UIMessage, drawable: Int?) {
            val currentHolder = holder as SyncImportHolder
            currentHolder.setStatus(message, drawable)
        }

        override fun disableSkip() {
            val currentHolder = holder as SyncImportHolder
            currentHolder.disableSkip()
        }

        override fun showRetrySyncDialog(result: GeneralResult) {
            retrySyncDialog.showLinkDeviceAuthDialog(syncingUIObserver, result)
        }


        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

    }
}
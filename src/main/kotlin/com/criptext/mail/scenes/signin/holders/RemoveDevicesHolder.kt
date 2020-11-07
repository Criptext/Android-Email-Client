package com.criptext.mail.scenes.signin.holders

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.settings.devices.data.DeviceAdapter
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.signin.SignInSceneModel
import com.criptext.mail.scenes.signin.data.VirtualDeviceList
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView
import com.criptext.mail.validation.ProgressButtonState


class RemoveDevicesHolder(
        val view: View,
        model: SignInSceneModel,
        devicesListItemListener: DevicesListItemListener?
): BaseSignInHolder() {

    private val backButton: ImageView by lazy {
        view.findViewById<ImageView>(R.id.mailbox_back_button)
    }
    private val submitButton: Button by lazy {
        view.findViewById<Button>(R.id.signin_button)
    }
    private val submitProgress: ProgressBar by lazy {
        view.findViewById<ProgressBar>(R.id.signin_progress_login)
    }

    private val recyclerViewDevices: RecyclerView by lazy {
        view.findViewById<RecyclerView>(R.id.recyclerViewDevices)
    }

    val deviceListView: VirtualListView = VirtualRecyclerView(recyclerViewDevices)

    init {
        deviceListView.setAdapter(DeviceAdapter(view.context, devicesListItemListener, VirtualDeviceList(model),
                DeviceItem.Companion.Type.Normal))
        setListeners()
    }

    fun setSubmitButtonState(state : ProgressButtonState){
        when (state) {
            ProgressButtonState.disabled -> {
                submitButton.visibility = View.VISIBLE
                submitButton.isEnabled = false
                submitProgress.visibility = View.GONE
            }
            ProgressButtonState.enabled -> {
                submitButton.visibility = View.VISIBLE
                submitButton.isEnabled = true
                submitProgress.visibility = View.GONE
            }
            ProgressButtonState.waiting -> {
                submitButton.visibility = View.GONE
                submitButton.isEnabled = false
                submitProgress.visibility = View.VISIBLE
            }
        }
    }


    private fun setListeners() {
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
        submitButton.setOnClickListener {
            uiObserver?.onSubmitButtonClicked()
        }
    }
}

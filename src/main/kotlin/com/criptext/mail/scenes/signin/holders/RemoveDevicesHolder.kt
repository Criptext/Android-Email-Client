package com.criptext.mail.scenes.signin.holders

import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.settings.devices.data.DeviceAdapter
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.signin.SignInSceneModel
import com.criptext.mail.scenes.signin.data.VirtualDeviceList
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView


class RemoveDevicesHolder(
        val view: View,
        private val recipientId: String,
        private val domain: String,
        model: SignInSceneModel,
        devicesListItemListener: DevicesListItemListener?
): BaseSignInHolder() {

    private val backButton: ImageView by lazy {
        view.findViewById<ImageView>(R.id.mailbox_back_button)
    }

    private val toolbarTitle: TextView by lazy {
        view.findViewById<TextView>(R.id.mailbox_toolbar_title)
    }

    private val messageText: TextView by lazy {
        view.findViewById<TextView>(R.id.message_text)
    }

    private val recyclerViewDevices: RecyclerView by lazy {
        view.findViewById<RecyclerView>(R.id.recyclerViewDevices)
    }

    private val removeDeviceToolbar: Toolbar by lazy {
        view.findViewById<Toolbar>(R.id.remove_device_toolbar)
    }

    private val removeDevicesTrash: MenuItem
    private val trasDrawable = view.context.getDrawable(R.drawable.trash)

    val deviceListView: VirtualListView = VirtualRecyclerView(recyclerViewDevices)

    init {
        removeDeviceToolbar.inflateMenu(R.menu.menu_remove_device_holder)
        removeDevicesTrash = removeDeviceToolbar.menu.findItem(R.id.delete_devices)
        removeDevicesTrash.setOnMenuItemClickListener {
            uiObserver?.onTrashPressed(recipientId, domain)
            true
        }
        messageText.text = view.context.getLocalizedUIMessage(
                UIMessage(
                        resId = R.string.sign_in_remove_message,
                        args = arrayOf(model.maxDevices, (model.devices.size - (model.maxDevices - 1)))
                )
        )
        deviceListView.setAdapter(DeviceAdapter(view.context, devicesListItemListener, VirtualDeviceList(model), DeviceItem.Companion.Type.WithCheckbox))
        setListeners()
    }


    private fun setListeners() {
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
    }

    fun setToolbarCount(checked: Int){
        when(checked){
            0 -> {
                toolbarTitle.text = view.context.getLocalizedUIMessage(UIMessage(R.string.devices_toolbar_title))
                removeDevicesTrash.setOnMenuItemClickListener {
                    false
                }
                trasDrawable?.setTintList(null)
                removeDevicesTrash.icon = trasDrawable
                backButton.setImageResource(R.drawable.back)
                backButton.setOnClickListener {
                    uiObserver?.onBackPressed()
                }
            }
            else -> {
                toolbarTitle.text = checked.toString()
                removeDevicesTrash.setOnMenuItemClickListener {
                    uiObserver?.onTrashPressed(recipientId, domain)
                    true
                }
                trasDrawable?.setTint(ContextCompat.getColor(
                        view.context, R.color.white))
                removeDevicesTrash.icon = trasDrawable
                backButton.setImageResource(R.drawable.x_circle)
                backButton.setOnClickListener {
                    uiObserver?.onXPressed()
                }
            }
        }
    }
}

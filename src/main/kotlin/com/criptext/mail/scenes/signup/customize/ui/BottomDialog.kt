package com.criptext.mail.scenes.signup.customize.ui

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.profile.ProfileUIObserver

class BottomDialog(val context: Context) {

    private var dialog: BottomSheetDialog? = null

    private lateinit var buttonCamera: LinearLayout
    private lateinit var buttonGallery: LinearLayout
    private lateinit var rootView: LinearLayout

    private lateinit var view: View

    var uiObserver: CustomizeUIObserver? = null

    fun showDialog(observer: CustomizeUIObserver?) {



        dialog = BottomSheetDialog(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.dialog_customize_bottom, null)
        initializeLayoutComponents()
        assignButtonEvents()

        dialog?.setContentView(view)
        uiObserver = observer

        dialog?.show()
    }

    private fun initializeLayoutComponents(){
        buttonCamera = view.findViewById(R.id.viewCamera)

        buttonGallery = view.findViewById(R.id.viewPhotoLibrary)

        rootView = view.findViewById(R.id.rootView)
    }

    private fun assignButtonEvents() {

        buttonCamera.setOnClickListener {
            uiObserver?.onNewCamPictureRequested()
            dialog?.dismiss()
        }

        buttonGallery.setOnClickListener {
            uiObserver?.onNewGalleryPictureRequested()
            dialog?.dismiss()
        }
    }

}
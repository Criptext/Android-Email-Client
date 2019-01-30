package com.criptext.mail.scenes.settings.profile.ui

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.profile.ProfileUIObserver


/**
 * Created by gabriel on 6/8/17.
 */

class BottomDialog(val context: Context) {

    private var dialog: BottomSheetDialog? = null

    private lateinit var buttonCamera: LinearLayout
    private lateinit var buttonGallery: LinearLayout
    private lateinit var buttonDocuments: LinearLayout
    private lateinit var rootView: LinearLayout

    private lateinit var view: View

    var uiObserver: ProfileUIObserver? = null

    fun showDialog(observer: ProfileUIObserver?) {



        dialog = BottomSheetDialog(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.dialog_profile_bottom, null)
        initializeLayoutComponents()
        assignButtonEvents(observer)

        dialog?.setContentView(view)
        uiObserver = observer

        dialog?.show()
    }

    private fun initializeLayoutComponents(){
        buttonCamera = view.findViewById(R.id.viewCamera)

        buttonGallery = view.findViewById(R.id.viewPhotoLibrary)
        buttonDocuments = view.findViewById(R.id.viewDocuments)

        rootView = view.findViewById(R.id.rootView)
    }

    private fun assignButtonEvents(observer: ProfileUIObserver?) {

        buttonCamera.setOnClickListener {
            uiObserver?.onNewCamPictureRequested()
            dialog?.dismiss()
        }

        buttonGallery.setOnClickListener {
            uiObserver?.onNewGalleryPictureRequested()
            dialog?.dismiss()
        }

        buttonDocuments.setOnClickListener {
            uiObserver?.onDeletePictureRequested()
            dialog?.dismiss()
        }
    }

}
package com.criptext.mail.scenes.composer.ui

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.criptext.mail.R


/**
 * Created by gabriel on 6/8/17.
 */

class AttachmentsBottomDialog(val context: Context) {

    private var dialog: BottomSheetDialog? = null

    private lateinit var buttonCamera: LinearLayout
    private lateinit var buttonGallery: LinearLayout
    private lateinit var buttonDocuments: LinearLayout
    private lateinit var rootView: LinearLayout

    private lateinit var view: View

    var uiObserver: ComposerUIObserver? = null

    fun showAttachmentsDialog(observer: ComposerUIObserver?) {



        dialog = BottomSheetDialog(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.dialog_attachment, null)
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

    private fun assignButtonEvents(observer: ComposerUIObserver?) {

        buttonCamera.setOnClickListener {
            uiObserver?.onNewCamAttachmentRequested()
            dialog?.dismiss()
        }

        buttonGallery.setOnClickListener {
            uiObserver?.onNewGalleryAttachmentRequested()
            dialog?.dismiss()
        }

        buttonDocuments.setOnClickListener {
            uiObserver?.onNewFileAttachmentRequested()
            dialog?.dismiss()
        }
    }

}
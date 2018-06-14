package com.email.scenes.composer

import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.criptext.timedurationpicker.SlideDialogFragment
import com.email.R

class AttachmentsDialog: SlideDialogFragment() {

    var observer: AttachmentDialogObserver? = null

    private val onRootClicked: View.OnClickListener = View.OnClickListener {
        dismiss()
    }

    private val onGalleryButtonClicked: View.OnClickListener = View.OnClickListener {
        observer?.onGalleryRequested()
    }

    private val onCameraButtonClicked: View.OnClickListener = View.OnClickListener {
        observer?.onCameraRequested()
    }

    private val onFileButtonClicked: View.OnClickListener = View.OnClickListener {
        observer?.onDocumentRequested()
        dismiss()
    }

    override fun initView(): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_attachment, null)
        val buttonCamera = view.findViewById<View>(R.id.viewCamera)
        val buttonGallery = view.findViewById<View>(R.id.viewPhotoLibrary)
        val buttonDocuments = view.findViewById<View>(R.id.viewDocuments)
        val rootView = view.findViewById<View>(R.id.rootView)

        buttonCamera.setOnClickListener(onCameraButtonClicked)
        buttonGallery.setOnClickListener(onGalleryButtonClicked)
        buttonDocuments.setOnClickListener(onFileButtonClicked)
        rootView.setOnClickListener(onRootClicked)

        return view
    }

    class Builder {
        internal var observer: AttachmentDialogObserver? = null

        fun setAttributes(observer: AttachmentDialogObserver?) : Builder{
            this.observer = observer
            return this
        }

        fun build(): DialogFragment {
            val dialog = AttachmentsDialog()
            dialog.observer = observer
            return dialog
        }
    }

    interface AttachmentDialogObserver{
        fun onGalleryRequested()
        fun onCameraRequested()
        fun onDocumentRequested()
    }

}
package com.criptext.mail.scenes.signup.customize.holder

import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.db.models.Contact
import com.criptext.mail.validation.ProgressButtonState
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class CustomizePictureHolder(
        val view: View,
        val name: String,
        val recipientId: String): BaseCustomizeHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val skipButton: TextView = view.findViewById(R.id.skip)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val pictureProgress: ProgressBar = view.findViewById(R.id.profile_picture_progress)
    private val picture: CircleImageView = view.findViewById(R.id.profile_picture)
    private val nameTextView: TextView = view.findViewById(R.id.textViewName)

    init {
        setListeners()
        nameTextView.text = name
        val urlAvatar = Hosts.restApiBaseUrl.plus("/user/avatar/${Contact.mainDomain}/$recipientId")
        Picasso.get()
                .load(urlAvatar)
                .placeholder(R.drawable.img_profile)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(picture, object : Callback {
                    override fun onSuccess() {

                    }

                    override fun onError(e: Exception) {
                        Picasso.get()
                                .load(urlAvatar)
                                .placeholder(R.drawable.img_profile)
                                .into(picture, object : Callback {
                                    override fun onSuccess() {

                                    }

                                    override fun onError(e: Exception) {
                                        picture.setImageResource(R.drawable.img_profile)
                                    }
                                })
                    }
                })
    }

    fun setImageBitmap(image: Bitmap){
        picture.setImageBitmap(image)
    }

    fun changeNextButton(){
        nextButton.setText(R.string.btn_next)
    }

    fun showProfilePictureProgress(show: Boolean){
        if(show){
            pictureProgress.visibility = View.VISIBLE
        } else {
            pictureProgress.visibility = View.GONE
        }
    }

    private fun setListeners() {
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
        }
        skipButton.setOnClickListener {
            uiObserver?.onSkipButtonPressed()
        }
    }

    override fun setSubmitButtonState(state : ProgressButtonState) {
        when (state) {
            ProgressButtonState.disabled -> {
                skipButton.isEnabled = true
                nextButton.visibility = View.VISIBLE
                nextButton.isEnabled = false
                nextButtonProgress.visibility = View.GONE
            }
            ProgressButtonState.enabled -> {
                skipButton.isEnabled = true
                nextButton.visibility = View.VISIBLE
                nextButton.isEnabled = true
                nextButtonProgress.visibility = View.GONE
            }
            ProgressButtonState.waiting -> {
                skipButton.isEnabled = false
                nextButton.visibility = View.GONE
                nextButton.isEnabled = false
                nextButtonProgress.visibility = View.VISIBLE
            }
        }
    }

}
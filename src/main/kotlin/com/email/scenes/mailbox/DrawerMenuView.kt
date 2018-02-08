package com.email.scenes.mailbox

import android.support.design.widget.NavigationView
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.utils.Utility
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by danieltigse on 2/7/18.
 */

class DrawerMenuView(navigationView: NavigationView){

    //HEADER
    val avatarView : CircleImageView
    val textViewNombre: TextView
    val textViewCorreo: TextView

    //MENU OPTIONS
    val sliderInbox : LinearLayout
    val sliderSent : LinearLayout
    val sliderDrafts : LinearLayout
    val sliderStarred : LinearLayout
    val sliderSpam : LinearLayout
    val sliderTrash : LinearLayout
    val sliderLabels : LinearLayout
    val sliderSettings : LinearLayout
    val sliderSupport : LinearLayout

    //COUNTERS
    val textViewCounterInbox: TextView
    val textViewCounterDraft: TextView
    val textViewCounterSpam: TextView

    init {
        avatarView = navigationView.findViewById(R.id.circleView)
        textViewNombre = navigationView.findViewById(R.id.textViewNombre)
        textViewCorreo = navigationView.findViewById(R.id.textViewCorreo)

        sliderInbox = navigationView.findViewById(R.id.slider_inbox)
        sliderSent = navigationView.findViewById(R.id.slider_sent)
        sliderDrafts = navigationView.findViewById(R.id.slider_drafts)
        sliderStarred = navigationView.findViewById(R.id.slider_starred)
        sliderSpam = navigationView.findViewById(R.id.slider_spam)
        sliderTrash = navigationView.findViewById(R.id.slider_trash)
        sliderLabels = navigationView.findViewById(R.id.slider_labels)
        sliderSettings = navigationView.findViewById(R.id.slider_settings)
        sliderSupport = navigationView.findViewById(R.id.slider_support)

        textViewCounterInbox = navigationView.findViewById(R.id.count_inbox)
        textViewCounterDraft = navigationView.findViewById(R.id.count_drafts)
        textViewCounterSpam = navigationView.findViewById(R.id.count_spam)
    }

    fun initNavHeader(fullName: String){
        avatarView.setImageBitmap(Utility.getBitmapFromText(fullName, "D", 250, 250))
    }
}
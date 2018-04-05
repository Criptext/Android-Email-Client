package com.email.scenes.mailbox.ui

import android.support.design.widget.NavigationView
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.scenes.mailbox.DrawerMenuItemListener
import com.email.scenes.mailbox.NavigationMenuOptions
import com.email.utils.Utility
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by danieltigse on 2/7/18.
 */

class DrawerMenuView(navigationView: NavigationView,
                        private val drawerMenuItemListener: DrawerMenuItemListener
                     ){

    //HEADER
    private val avatarView : CircleImageView
    private val textViewNombre: TextView
    private val textViewCorreo: TextView

    //MENU OPTIONS
    private val sliderInbox : LinearLayout
    private val sliderSent : LinearLayout
    private val sliderDrafts : LinearLayout
    private val sliderStarred : LinearLayout
    private val sliderSpam : LinearLayout
    private val sliderTrash : LinearLayout
    private val sliderLabels : LinearLayout
    private val sliderSettings : LinearLayout
    private val sliderSupport : LinearLayout

    //COUNTERS
    private val textViewCounterInbox: TextView
    private val textViewCounterDraft: TextView
    private val textViewCounterSpam: TextView

    private fun setListeners() {
        sliderInbox.setOnClickListener {
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.INBOX)
        }

        sliderSent.setOnClickListener {
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.SENT)
        }

        sliderDrafts.setOnClickListener {
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.DRAFT)
        }

        sliderStarred.setOnClickListener {
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.STARRED)
        }

        sliderSpam.setOnClickListener {
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.SPAM)
        }

        sliderTrash.setOnClickListener {
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.TRASH)
        }
    }


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

        setListeners()
    }

    fun initNavHeader(fullName: String){
        avatarView.setImageBitmap(Utility.getBitmapFromText(fullName, "D", 250, 250))
    }
}
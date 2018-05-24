package com.email.scenes.mailbox.ui

import android.graphics.Color
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.scenes.mailbox.DrawerMenuItemListener
import com.email.scenes.mailbox.NavigationMenuOptions
import com.email.utils.Utility
import de.hdodenhof.circleimageview.CircleImageView
import uk.co.chrisjenx.calligraphy.TypefaceUtils

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
    private val sliderAllMail : LinearLayout
    private val sliderLabels : LinearLayout
    private val sliderSettings : LinearLayout
    private val sliderSupport : LinearLayout

    //MENU TITLES
    private val textViewTitleInbox: TextView
    private val textViewTitleSent: TextView
    private val textViewTitleDraft: TextView
    private val textViewTitleStarred: TextView
    private val textViewTitleSpam: TextView
    private val textViewTitleTrash: TextView
    private val textViewTitleAllMail: TextView

    //MENU ICONS
    private val imageViewInbox: ImageView
    private val imageViewSent: ImageView
    private val imageViewDraft: ImageView
    private val imageViewStarred: ImageView
    private val imageViewSpam: ImageView
    private val imageViewTrash: ImageView
    private val imageViewAllMail: ImageView

    //COUNTERS
    private val textViewCounterInbox: TextView
    private val textViewCounterDraft: TextView
    private val textViewCounterSpam: TextView

    private fun setListeners() {
        sliderInbox.setOnClickListener {
            setActiveLabel(NavigationMenuOptions.INBOX)
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.INBOX)
        }

        sliderSent.setOnClickListener {
            setActiveLabel(NavigationMenuOptions.SENT)
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.SENT)
        }

        sliderDrafts.setOnClickListener {
            setActiveLabel(NavigationMenuOptions.DRAFT)
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.DRAFT)
        }

        sliderStarred.setOnClickListener {
            setActiveLabel(NavigationMenuOptions.STARRED)
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.STARRED)
        }

        sliderSpam.setOnClickListener {
            setActiveLabel(NavigationMenuOptions.SPAM)
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.SPAM)
        }

        sliderTrash.setOnClickListener {
            setActiveLabel(NavigationMenuOptions.TRASH)
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.TRASH)
        }

        sliderAllMail.setOnClickListener {
            setActiveLabel(NavigationMenuOptions.ALL_MAIL)
            drawerMenuItemListener.onNavigationItemClick(
                    navigationMenuOptions = NavigationMenuOptions.ALL_MAIL)
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
        sliderAllMail = navigationView.findViewById(R.id.slider_all_mail)
        sliderLabels = navigationView.findViewById(R.id.slider_labels)
        sliderSettings = navigationView.findViewById(R.id.slider_settings)
        sliderSupport = navigationView.findViewById(R.id.slider_support)

        textViewCounterInbox = navigationView.findViewById(R.id.count_inbox)
        textViewCounterDraft = navigationView.findViewById(R.id.count_drafts)
        textViewCounterSpam = navigationView.findViewById(R.id.count_spam)

        textViewTitleInbox = navigationView.findViewById(R.id.textViewTitleInbox)
        textViewTitleSent = navigationView.findViewById(R.id.textViewTitleSent)
        textViewTitleDraft = navigationView.findViewById(R.id.textViewTitleDraft)
        textViewTitleStarred = navigationView.findViewById(R.id.textViewTitleStarred)
        textViewTitleSpam = navigationView.findViewById(R.id.textViewTitleSpam)
        textViewTitleTrash = navigationView.findViewById(R.id.textViewTitleTrash)
        textViewTitleAllMail = navigationView.findViewById(R.id.textViewTitleAllMail)

        imageViewInbox = navigationView.findViewById(R.id.imageViewInbox)
        imageViewSent = navigationView.findViewById(R.id.imageViewSent)
        imageViewDraft = navigationView.findViewById(R.id.imageViewDraft)
        imageViewStarred = navigationView.findViewById(R.id.imageViewStarred)
        imageViewSpam = navigationView.findViewById(R.id.imageViewSpam)
        imageViewTrash = navigationView.findViewById(R.id.imageViewTrash)
        imageViewAllMail = navigationView.findViewById(R.id.imageViewAllMail)

        setListeners()
    }

    fun initNavHeader(fullName: String, email: String){
        val safeFullName = if(fullName.isEmpty())
            avatarView.context.resources.getString(R.string.unknown) else fullName
        avatarView.setImageBitmap(Utility.getBitmapFromText(safeFullName,
                safeFullName.toCharArray()[0].toString(), 250, 250))
        textViewNombre.text = safeFullName
        textViewCorreo.text = email
    }

    private fun setActiveLabel(menu: NavigationMenuOptions){
        clearActiveLabel()
        when(menu){
            NavigationMenuOptions.INBOX -> {
                setResourcesSelected(sliderInbox, textViewTitleInbox, imageViewInbox)
            }
            NavigationMenuOptions.DRAFT -> {
                setResourcesSelected(sliderDrafts, textViewTitleDraft, imageViewDraft)
            }
            NavigationMenuOptions.SENT -> {
                setResourcesSelected(sliderSent, textViewTitleSent, imageViewSent)
            }
            NavigationMenuOptions.STARRED -> {
                setResourcesSelected(sliderStarred, textViewTitleStarred, imageViewStarred)
            }
            NavigationMenuOptions.SPAM -> {
                setResourcesSelected(sliderSpam, textViewTitleSpam, imageViewSpam)
            }
            NavigationMenuOptions.TRASH -> {
                setResourcesSelected(sliderTrash, textViewTitleTrash, imageViewTrash)
            }
            NavigationMenuOptions.ALL_MAIL -> {
                setResourcesSelected(sliderAllMail, textViewTitleAllMail, imageViewAllMail)
            }
        }
    }

    private fun setResourcesSelected(slider: LinearLayout, textView: TextView, imageView: ImageView){
        slider.setBackgroundColor(ContextCompat.getColor(slider.context, R.color.menu_selected))
        textView.typeface = TypefaceUtils.load(textView.resources.assets,
                "fonts/NunitoSans-Bold.ttf")
        DrawableCompat.setTint(imageView.drawable,
                ContextCompat.getColor(imageView.context, R.color.drawer_icon_selected))
    }

    fun setCounterLabel(menu: NavigationMenuOptions, total: Int){
        textViewCounterInbox.text = total.toString()
        when(menu){
            NavigationMenuOptions.INBOX -> {
                textViewCounterInbox.visibility = if (total > 0) View.VISIBLE else View.GONE
            }
            NavigationMenuOptions.DRAFT -> {
                textViewCounterDraft.visibility = if (total > 0) View.VISIBLE else View.GONE
            }
            NavigationMenuOptions.SPAM -> {
                textViewCounterSpam.visibility = if (total > 0) View.VISIBLE else View.GONE
            }
        }
    }

    private fun clearActiveLabel(){
        sliderInbox.setBackgroundColor(Color.TRANSPARENT)
        sliderDrafts.setBackgroundColor(Color.TRANSPARENT)
        sliderSent.setBackgroundColor(Color.TRANSPARENT)
        sliderSpam.setBackgroundColor(Color.TRANSPARENT)
        sliderTrash.setBackgroundColor(Color.TRANSPARENT)
        sliderStarred.setBackgroundColor(Color.TRANSPARENT)
        sliderAllMail.setBackgroundColor(Color.TRANSPARENT)
        textViewTitleInbox.typeface = TypefaceUtils.load(textViewTitleInbox.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitleSent.typeface = TypefaceUtils.load(textViewTitleSent.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitleDraft.typeface = TypefaceUtils.load(textViewTitleDraft.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitleStarred.typeface = TypefaceUtils.load(textViewTitleStarred.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitleSpam.typeface = TypefaceUtils.load(textViewTitleSpam.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitleTrash.typeface = TypefaceUtils.load(textViewTitleTrash.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitleAllMail.typeface = TypefaceUtils.load(textViewTitleAllMail.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        DrawableCompat.setTint(imageViewInbox.drawable,
                ContextCompat.getColor(imageViewInbox.context, R.color.drawer_icon_unselected))
        DrawableCompat.setTint(imageViewSent.drawable,
                ContextCompat.getColor(imageViewSent.context, R.color.drawer_icon_unselected))
        DrawableCompat.setTint(imageViewDraft.drawable,
                ContextCompat.getColor(imageViewDraft.context, R.color.drawer_icon_unselected))
        DrawableCompat.setTint(imageViewStarred.drawable,
                ContextCompat.getColor(imageViewStarred.context, R.color.drawer_icon_unselected))
        DrawableCompat.setTint(imageViewSpam.drawable,
                ContextCompat.getColor(imageViewSpam.context, R.color.drawer_icon_unselected))
        DrawableCompat.setTint(imageViewTrash.drawable,
                ContextCompat.getColor(imageViewTrash.context, R.color.drawer_icon_unselected))
        DrawableCompat.setTint(imageViewAllMail.drawable,
                ContextCompat.getColor(imageViewAllMail.context, R.color.drawer_icon_unselected))
    }
}
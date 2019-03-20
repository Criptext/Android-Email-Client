package com.criptext.mail.scenes.mailbox.ui

import android.graphics.Color
import com.google.android.material.navigation.NavigationView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.DrawerMenuItemListener
import com.criptext.mail.scenes.mailbox.NavigationMenuOptions
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.Utility
import com.criptext.mail.utils.getColorFromAttr
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView
import com.squareup.picasso.Picasso
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
    private val sliderInviteFriend : LinearLayout
    private val sliderSupport : LinearLayout

    private val recyclerViewLabels : RecyclerView
    private val imageViewArrow: ImageView

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

        sliderLabels.setOnClickListener {
            val visible = recyclerViewLabels.visibility == View.VISIBLE
            recyclerViewLabels.visibility = if (visible) View.GONE else View.VISIBLE
            Picasso.get().load(
                    if(visible) R.drawable.arrow_down else
                        R.drawable.arrow_up).into(imageViewArrow)
        }

        sliderSettings.setOnClickListener {
            drawerMenuItemListener.onSettingsOptionClicked()
        }

        sliderInviteFriend.setOnClickListener{
            drawerMenuItemListener.onInviteFriendOptionClicked()
        }

        sliderSupport.setOnClickListener {
            drawerMenuItemListener.onSupportOptionClicked()
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
        sliderInviteFriend = navigationView.findViewById(R.id.slider_invite_friend)
        sliderSupport = navigationView.findViewById(R.id.slider_support)

        imageViewArrow = navigationView.findViewById(R.id.imageViewArrow)
        recyclerViewLabels = navigationView.findViewById(R.id.recyclerViewLabels)

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
        if(EmailAddressUtils.isFromCriptextDomain(email))
            UIUtils.setProfilePicture(
                    iv = avatarView,
                    resources = avatarView.context.resources,
                    recipientId = EmailAddressUtils.extractRecipientIdFromCriptextAddress(email),
                    name = safeFullName,
                    runnable = null)
        else
            avatarView.setImageBitmap(
                    Utility.getBitmapFromText(
                            fullName,
                            250,
                            250))
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
        slider.setBackgroundColor(imageView.context.getColorFromAttr(R.attr.criptextLeftMenuSelected))
        textView.typeface = TypefaceUtils.load(textView.resources.assets,
                "fonts/NunitoSans-Bold.ttf")
        textView.setTextColor(textView.context.getColorFromAttr(R.attr.criptextLeftMenuSelectedText))
        DrawableCompat.setTint(imageView.drawable,
                imageView.context.getColorFromAttr(R.attr.criptextLeftMenuIconSelected))
    }

    fun setCounterLabel(menu: NavigationMenuOptions, total: Int){
        when(menu){
            NavigationMenuOptions.INBOX -> {
                textViewCounterInbox.visibility = if (total > 0) View.VISIBLE else View.GONE
                textViewCounterInbox.text = total.toString()
            }
            NavigationMenuOptions.DRAFT -> {
                textViewCounterDraft.visibility = if (total > 0) View.VISIBLE else View.GONE
                textViewCounterDraft.text = total.toString()
            }
            NavigationMenuOptions.SPAM -> {
                textViewCounterSpam.visibility = if (total > 0) View.VISIBLE else View.GONE
                textViewCounterSpam.text = total.toString()
            }
        }
    }

    fun setLabelAdapter(label: List<LabelWrapper>){
        val labelListView = VirtualRecyclerView(recyclerViewLabels)
        labelListView.setAdapter(LabelWrapperAdapter(recyclerViewLabels.context,
                drawerMenuItemListener, VirtualLabelWrapperList(label)))
    }

    fun clearActiveLabel(){
        sliderInbox.setBackgroundColor(Color.TRANSPARENT)
        sliderDrafts.setBackgroundColor(Color.TRANSPARENT)
        sliderSent.setBackgroundColor(Color.TRANSPARENT)
        sliderSpam.setBackgroundColor(Color.TRANSPARENT)
        sliderTrash.setBackgroundColor(Color.TRANSPARENT)
        sliderStarred.setBackgroundColor(Color.TRANSPARENT)
        sliderAllMail.setBackgroundColor(Color.TRANSPARENT)
        val font = TypefaceUtils.load(textViewTitleInbox.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitleInbox.typeface = font
        textViewTitleSent.typeface = font
        textViewTitleDraft.typeface = font
        textViewTitleStarred.typeface = font
        textViewTitleSpam.typeface = font
        textViewTitleTrash.typeface = font
        textViewTitleAllMail.typeface = font
        val color = imageViewInbox.context.getColorFromAttr(R.attr.criptextLeftMenuIconUnSelected)
        DrawableCompat.setTint(imageViewInbox.drawable, color)
        DrawableCompat.setTint(imageViewSent.drawable, color)
        DrawableCompat.setTint(imageViewDraft.drawable, color)
        DrawableCompat.setTint(imageViewStarred.drawable, color)
        DrawableCompat.setTint(imageViewSpam.drawable, color)
        DrawableCompat.setTint(imageViewTrash.drawable, color)
        DrawableCompat.setTint(imageViewAllMail.drawable, color)
        val colorText = imageViewInbox.context.getColorFromAttr(R.attr.criptextLeftMenuText)
        textViewTitleInbox.setTextColor(colorText)
        textViewTitleSent.setTextColor(colorText)
        textViewTitleDraft.setTextColor(colorText)
        textViewTitleStarred.setTextColor(colorText)
        textViewTitleSpam.setTextColor(colorText)
        textViewTitleTrash.setTextColor(colorText)
        textViewTitleAllMail.setTextColor(colorText)
    }

    inner class VirtualLabelWrapperList(val labels: List<LabelWrapper>): VirtualList<LabelWrapper>{

        override fun get(i: Int): LabelWrapper {
            return labels[i]
        }

        override val size: Int
            get() = labels.size

        override val hasReachedEnd = true

    }

}
package com.criptext.mail.scenes.emaildetail.ui.holders

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Matrix
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.*
import android.webkit.*
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.composer.ui.holders.AttachmentViewObserver
import com.criptext.mail.scenes.emaildetail.WebviewJavascriptInterface
import com.criptext.mail.scenes.emaildetail.ui.EmailContactInfoPopup
import com.criptext.mail.scenes.emaildetail.ui.FileListAdapter
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.criptext.mail.utils.*
import com.criptext.mail.utils.ui.MyZoomLayout
import com.otaliastudios.zoom.ZoomEngine
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


/**
 * Created by sebas on 3/12/18.
 */

class FullEmailHolder(view: View) : ParentEmailHolder(view) {

    private val context = view.context
    private val layout : FrameLayout
    private val rootView : LinearLayout
    private val continueDraftView: ImageView
    private val replyView: ImageView
    private val threePointsView: ImageView
    private val moreButton: TextView
    private val toView: TextView
    private val readView: ImageView
    private val contactInfoPopUp: EmailContactInfoPopup
    private val bodyWebView: WebView
    private val bodyContainer : LinearLayout
    private val zoomLayout: MyZoomLayout
    private val attachmentsRecyclerView: RecyclerView
    private val leftImageView: CircleImageView
    private val unsendProgressBar: ProgressBar

    override fun setListeners(fullEmail: FullEmail, fileDetails: List<FileDetail>,
                     emailListener: FullEmailListAdapter.OnFullEmailEventListener?,
                     adapter: FullEmailListAdapter, position: Int) {
        view.setOnClickListener {

            emailListener?.ontoggleViewOpen(
                    fullEmail = fullEmail,
                    position = position,
                    viewOpen = false)
        }
        threePointsView.setOnClickListener {
            displayPopMenu(emailListener, fullEmail, adapter, position - 1)
        }

        moreButton.setOnClickListener {
            contactInfoPopUp.createPopup(fullEmail, null)
        }

        continueDraftView.setOnClickListener{
            emailListener?.onContinueDraftOptionSelected(fullEmail)
        }

        replyView.setOnClickListener{
            emailListener?.onReplyOptionSelected(
                    fullEmail = fullEmail,
                    position = position,
                    all = false)
        }

        setAttachments(fileDetails, emailListener)

    }

    private fun displayPopMenu(emailListener: FullEmailListAdapter.OnFullEmailEventListener?, fullEmail: FullEmail,
                               adapter: FullEmailListAdapter, position: Int){
        val popupMenu = createPopupMenu(fullEmail)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.reply_all ->
                    emailListener?.onReplyAllOptionSelected(
                            fullEmail = fullEmail,
                            position = position,
                            all = false)
                R.id.reply ->
                    emailListener?.onReplyOptionSelected(
                            fullEmail = fullEmail,
                            position = position,
                            all = false)
                R.id.forward ->
                    emailListener?.onForwardOptionSelected(
                            fullEmail = fullEmail,
                            position = position,
                            all = false)
                R.id.mark_read, R.id.mark_unread -> {
                    emailListener?.onToggleReadOption(
                            fullEmail = fullEmail,
                            position = position,
                            markAsRead = item.itemId == R.id.mark_read)
                }
                R.id.unsend -> {
                    emailListener?.onUnsendEmail(
                                fullEmail = fullEmail,
                                position = position)
                }
                R.id.delete ->
                    emailListener?.onDeleteOptionSelected(
                            fullEmail = fullEmail,
                            position = position )
                R.id.mark_spam ->
                    emailListener?.onSpamOptionSelected(
                            fullEmail = fullEmail,
                            position = position )

            }
            false
        }

        popupMenu.gravity = Gravity.END
        popupMenu.show()

    }

    private fun createPopupMenu(fullEmail: FullEmail): PopupMenu {
        val wrapper = ContextThemeWrapper(context, R.style.email_detail_popup_menu)
        val popupMenu = PopupMenu(wrapper , threePointsView)

        val popuplayout = if(fullEmail.email.delivered == DeliveryTypes.NONE
                || fullEmail.email.delivered == DeliveryTypes.UNSEND
                || !fullEmail.email.secure) {
                    if (fullEmail.email.unread) {
                        if (fullEmail.labels.contains(Label.defaultItems.trash))
                            R.menu.mail_options_unread_menu_in_trash
                        else
                            R.menu.mail_options_unread_menu
                    }else {
                        if (fullEmail.labels.contains(Label.defaultItems.trash))
                            R.menu.mail_options_read_menu_in_trash
                        else
                            R.menu.mail_options_read_menu
                     }
        }else{
            if (fullEmail.email.unread) {
                if (fullEmail.labels.contains(Label.defaultItems.trash))
                    R.menu.mail_options_unread_menu_sent_in_trash
                else
                    R.menu.mail_options_unread_menu_sent
            }else {
                if (fullEmail.labels.contains(Label.defaultItems.trash))
                    R.menu.mail_options_read_menu_sent_in_trash
                else
                    R.menu.mail_options_read_menu_sent
            }
        }

        popupMenu.inflate(popuplayout)
        return popupMenu
    }

    override fun bindFullMail(fullEmail: FullEmail) {

        if(fullEmail.email.delivered != DeliveryTypes.UNSEND) {
            bodyWebView.loadDataWithBaseURL("", HTMLUtils.
                    changedHeaderHtml(fullEmail.email.content), "text/html", "utf-8", "")
            setDefaultBackgroundColors()
        }
        else {
            rootView.background = ContextCompat.getDrawable(
                    view.context, R.drawable.background_cardview_unsend)
            bodyWebView.loadDataWithBaseURL("", HTMLUtils.
                    changedHeaderHtml("Unsent: " + fullEmail.email.unsentDate.toString()),
                    "text/html", "utf-8", "")
            deactivateElementsForUnsend()
        }

        dateView.text = DateAndTimeUtils.getFormattedDate(fullEmail.email.date.time)
        headerView.text =
                if(EmailThreadValidator.isLabelInList(fullEmail.labels, Label.LABEL_DRAFT)) {
                    headerView.setTextColor(ContextCompat.getColor(headerView.context, R.color.colorUnsent))
                    headerView.context.getString(R.string.draft)
                }
                else {
                    headerView.setTextColor(ContextCompat.getColor(headerView.context, R.color.textColorPrimary))
                    fullEmail.from.name
                }

        if(fullEmail.isUnsending)
            unsendProgressBar.visibility = View.VISIBLE
        else
            unsendProgressBar.visibility = View.INVISIBLE

        leftImageView.setImageBitmap(Utility.getBitmapFromText(
                fullEmail.from.name,250, 250))

        setToText(fullEmail)
        setDraftIcon(fullEmail)
        setIcons(fullEmail.email.delivered)
    }

    private fun setAttachments(files: List<FileDetail>, emailListener: FullEmailListAdapter.OnFullEmailEventListener?){
        val adapter = FileListAdapter(view.context, files)
        val mLayoutManager = LinearLayoutManager(view.context)
        adapter.observer = object: AttachmentViewObserver {
            override fun onAttachmentViewClick(position: Int) {
                emailListener?.onAttachmentSelected(adapterPosition - 1, position)
            }
            override fun onRemoveAttachmentClicked(position: Int) {}
        }
        attachmentsRecyclerView.layoutManager = mLayoutManager
        attachmentsRecyclerView.adapter = adapter
    }

    private fun setDraftIcon(fullEmail: FullEmail){
        if(fullEmail.labels.contains(Label.defaultItems.draft)){
            continueDraftView.visibility = View.VISIBLE
            replyView.visibility = View.GONE
            threePointsView.visibility = View.GONE
        }
        else{
            continueDraftView.visibility = View.GONE
            replyView.visibility = View.VISIBLE
            threePointsView.visibility = View.VISIBLE
        }
    }

    private fun setToText(fullEmail: FullEmail){
        val numberContacts = fullEmail.to.size
        val isFromMe = (fullEmail.email.delivered != DeliveryTypes.NONE
                || EmailThreadValidator.isLabelInList(fullEmail.labels, Label.LABEL_DRAFT))
        toView.text = when {
            isFromMe ->
                "${toView.resources.getString(R.string.to)} ${fullEmail.to.joinToString { it.name }}"
            numberContacts == 2 ->
                "${toView.resources.getString(R.string.to_me)} and ${fullEmail.to.joinToString { it.name }}"
            numberContacts > 2 ->
                "${toView.resources.getString(R.string.to_me)}, ${fullEmail.to.joinToString { it.name }}"
            else ->
                toView.resources.getString(R.string.to_me)
        }
    }

    private fun setIcons(deliveryType: DeliveryTypes){

        readView.visibility = View.VISIBLE

        when(deliveryType){
            DeliveryTypes.UNSEND -> {
                readView.visibility = View.GONE
            }
            DeliveryTypes.SENDING -> {
                setIconAndColor(R.drawable.clock, R.color.sent)
            }
            DeliveryTypes.READ -> {
                setIconAndColor(R.drawable.read, R.color.azure)
            }
            DeliveryTypes.DELIVERED -> {
                setIconAndColor(R.drawable.read, R.color.sent)
            }
            DeliveryTypes.SENT -> {
                setIconAndColor(R.drawable.mail_sent, R.color.sent)
            }
            else -> {
                readView.visibility = View.GONE
            }
        }
    }

    private fun setIconAndColor(drawable: Int, color: Int){
        Picasso.with(view.context).load(drawable).into(readView, object : Callback {
            override fun onError() {}
            override fun onSuccess() {
                DrawableCompat.setTint(readView.drawable,
                        ContextCompat.getColor(view.context, color))
            }
        })
    }

    private fun setupWebview(){
        val metrics = DisplayMetrics()
        val display = (context.getSystemService(
                Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        display.getMetrics(metrics)
        bodyWebView.layoutParams = FrameLayout.LayoutParams(
                metrics.widthPixels - context.resources.
                        getDimension(R.dimen.webview_left_margin).toInt(), bodyWebView.layoutParams.height)

        val webSettings = bodyWebView.settings
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        bodyWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                WebViewUtils.openUrl(bodyWebView.context!!, url)
                return true
            }
            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                WebViewUtils.openUrl(bodyWebView.context!!, request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript("""window.scrollTo(0,0);""") { }
                reSizeZoomLayout(view, true)
                setupZoomLayout()
            }
        }
        val javascriptInterface = WebviewJavascriptInterface(context, zoomLayout, bodyWebView)
        bodyWebView.addJavascriptInterface(javascriptInterface, "CriptextSecureEmail")
    }

    private fun reSizeZoomLayout(view: WebView?, pageFinished: Boolean){
        if(view == null){
            return
        }
        if(view.height > 0) {
            zoomLayout.layoutParams = LinearLayout.LayoutParams(view.width, view.height)
            return
        }
        //Sometimes onPageFinished is called when the webView has not finished loading
        //So I put a temporal height to the webView and then call the manual zoom method
        if(pageFinished){
            zoomLayout.layoutParams = LinearLayout.LayoutParams(view.width, 250)
            view.postDelayed({
                zoomLayout.realZoomTo(1.0f, false)
            }, 500)
        }
    }

    private fun setupZoomLayout(){
        zoomLayout.mListener = object : MyZoomLayout.ZoomUpdateListener{
            override fun onUpdate(helper: ZoomEngine?, matrix: Matrix?) {
                val values = FloatArray(9)
                matrix?.getValues(values)
                val scaleY = values[Matrix.MSCALE_Y]
                zoomLayout.layoutParams = LinearLayout.LayoutParams(bodyWebView.width, (scaleY * bodyWebView.height).toInt())
            }
        }
    }

    private fun setupWeChromeClient(): WebChromeClient{
        return object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if(newProgress >= 80) {
                    reSizeZoomLayout(view, false)
                }
            }
        }
    }

    private fun deactivateElementsForUnsend() {
        bodyContainer.alpha = 0.4.toFloat()
    }

    private fun setDefaultBackgroundColors() {
        bodyContainer.alpha = 1.toFloat()
        bodyContainer.isEnabled = true

    }

    fun updateAttachmentProgress(attachmentPosition: Int){
        attachmentsRecyclerView.adapter?.notifyItemChanged(attachmentPosition)
    }

    init {
        layout = view.findViewById(R.id.open_full_mail_item_container)
        toView = view.findViewById(R.id.to)
        threePointsView = view.findViewById(R.id.more)
        moreButton = view.findViewById(R.id.more_text)
        replyView = view.findViewById(R.id.reply)
        continueDraftView = view.findViewById(R.id.continue_draft)
        readView =  view.findViewById(R.id.check)
        contactInfoPopUp = EmailContactInfoPopup(moreButton)
        bodyWebView = view.findViewById(R.id.email_body)
        bodyWebView.webChromeClient = setupWeChromeClient()
        bodyContainer = view.findViewById(R.id.body_container)
        rootView = view.findViewById(R.id.cardview)
        attachmentsRecyclerView = view.findViewById(R.id.attachments_recycler_view)
        leftImageView = view.findViewById(R.id.mail_item_left_name)
        unsendProgressBar = view.findViewById(R.id.loadingPanel)
        zoomLayout = view.findViewById(R.id.zoomLayout)
        setupWebview()
    }
}

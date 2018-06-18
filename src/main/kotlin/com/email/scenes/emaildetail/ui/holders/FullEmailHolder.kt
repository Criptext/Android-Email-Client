package com.email.scenes.emaildetail.ui.holders

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.email.R
import com.email.SecureEmail
import com.email.db.DeliveryTypes
import com.email.db.models.File
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.composer.ui.holders.AttachmentViewHolder
import com.email.scenes.composer.ui.holders.AttachmentViewObserver
import com.email.scenes.emaildetail.WebviewJavascriptInterface
import com.email.scenes.emaildetail.ui.*
import com.email.utils.DateUtils
import com.email.utils.EmailThreadValidator
import com.email.utils.HTMLUtils
import com.email.utils.ui.ZoomLayout
import com.email.utils.WebViewUtils
import com.github.ybq.android.spinkit.SpinKitView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailHolder(view: View) : ParentEmailHolder(view) {

    private val context = view.context
    private val layout : FrameLayout
    private val continueDraftView: ImageView
    private val replyView: ImageView
    private val moreView: ImageView
    private val toView: TextView
    private val attachmentView: ImageView
    private val readView: ImageView
    private val unsendView: AppCompatImageView
    private val email_options: View
    private val contactInfoPopUp: EmailContactInfoPopup
    private val readHistoryPopUp: ReadHistoryPopUp
    private val attachmentHistoryPopUp: AttachmentHistoryPopUp
    private val bodyWebView: WebView
    private val zoomLayout: ZoomLayout
    private val horizontalScrollView: HorizontalScrollView
    private val progressBarUnsend: SpinKitView
    private val bodyContainer : LinearLayout
    private val webViewLoader: ProgressBar
    private val attachmentsRecyclerView: RecyclerView

    override fun setListeners(fullEmail: FullEmail,
                     emailListener: FullEmailListAdapter.OnFullEmailEventListener?,
                     adapter: FullEmailListAdapter, position: Int) {
        view.setOnClickListener {

            emailListener?.ontoggleViewOpen(
                    fullEmail = fullEmail,
                    position = position,
                    viewOpen = false)
        }
        moreView.setOnClickListener({
            displayPopMenu(emailListener, fullEmail, adapter, position)
        })

        readView.setOnClickListener({
            readHistoryPopUp.createPopup(fullEmail, null)
        })

        toView.setOnClickListener({
            contactInfoPopUp.createPopup(fullEmail, null)
        })

        attachmentView.setOnClickListener {
            attachmentHistoryPopUp.createPopup(fullEmail, null)
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

        unsendView.setOnClickListener {
            toggleUnsendProgress(isShown = true)
            deactivateElementsForUnsend()
            emailListener?.onUnsendEmail(
                    fullEmail = fullEmail,
                    position = position)
        }

        setAttachments(fullEmail.files, emailListener)
    }
    private fun toggleUnsendProgress(isShown: Boolean) {
        if(isShown)  {
            progressBarUnsend.visibility = View.VISIBLE
            unsendView.visibility = View.GONE
        } else {
            progressBarUnsend.visibility = View.GONE
            unsendView.visibility = View.VISIBLE
        }
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
        val popupMenu = PopupMenu(wrapper , moreView)

    val popuplayout =
            if (fullEmail.email.unread)
                R.menu.mail_options_unread_menu
            else
                R.menu.mail_options_read_menu

        popupMenu.inflate(popuplayout)
        return popupMenu
    }

    override fun bindFullMail(fullEmail: FullEmail) {

        toggleUnsendProgress(isShown = false)

        if(fullEmail.email.delivered != DeliveryTypes.UNSENT) {
            bodyWebView.loadDataWithBaseURL("", HTMLUtils.
                    changedHeaderHtml(fullEmail.email.content), "text/html", "utf-8", "")
            setDefaultBackgroundColors()
        }
        else {
            bodyWebView.loadDataWithBaseURL("", HTMLUtils.
                    changedHeaderHtml("This content was unsent"), "text/html", "utf-8", "")
            deactivateElementsForUnsend()
            DrawableCompat.setTint(unsendView.drawable,
                    ContextCompat.getColor(unsendView.context, R.color.unsend_button_red))
            unsendView.background = ContextCompat.getDrawable(unsendView.context, R.drawable.circle_unsent)
        }

        dateView.text = DateUtils.getFormattedDate(fullEmail.email.date.time)
        headerView.text =
                if(EmailThreadValidator.isLabelInList(fullEmail.labels, SecureEmail.LABEL_DRAFT)) {
                    headerView.setTextColor(ContextCompat.getColor(headerView.context, R.color.colorUnsent))
                    headerView.context.getString(R.string.draft)
                }
                else {
                    headerView.setTextColor(ContextCompat.getColor(headerView.context, R.color.textColorPrimary))
                    fullEmail.from.name
                }
        email_options.visibility = if(fullEmail.email.delivered != DeliveryTypes.NONE)
            View.VISIBLE else View.INVISIBLE

        setToText(fullEmail)
        setDraftIcon(fullEmail)
        setIcons(fullEmail.email.delivered)
    }

    private fun setAttachments(files: List<File>, emailListener: FullEmailListAdapter.OnFullEmailEventListener?){
        val adapter = FileListAdapter(view.context, files)
        val mLayoutManager = LinearLayoutManager(view.context)
        adapter.observer = object: AttachmentViewObserver {
            override fun onViewClick(position: Int) {
                emailListener?.onAttachmentSelect(adapterPosition, position)
            }
            override fun onRemoveClick(position: Int) {}
        }
        attachmentsRecyclerView.layoutManager = mLayoutManager
        attachmentsRecyclerView.adapter = adapter
    }

    private fun setDraftIcon(fullEmail: FullEmail){
        if(fullEmail.labels.contains(Label.defaultItems.draft)){
            continueDraftView.visibility = View.VISIBLE
            replyView.visibility = View.GONE
            moreView.visibility = View.GONE
        }
        else{
            continueDraftView.visibility = View.GONE
            replyView.visibility = View.VISIBLE
            moreView.visibility = View.VISIBLE
        }
    }

    private fun setToText(fullEmail: FullEmail){
        val numberContacts = fullEmail.to.size
        val isFromMe = (fullEmail.email.delivered != DeliveryTypes.NONE
                || EmailThreadValidator.isLabelInList(fullEmail.labels, SecureEmail.LABEL_DRAFT))
        toView.text = when {
            isFromMe && numberContacts == 1 ->
                "${toView.resources.getString(R.string.to)} ${fullEmail.to[0].name}"
            isFromMe && numberContacts > 1 ->
                toView.resources.getString(R.string.to_contacts, numberContacts)
            numberContacts > 1 ->
                toView.resources.getString(R.string.to_me_and, numberContacts  - 1)
            else ->
                toView.resources.getString(R.string.to_me)
        }
    }

    private fun setIcons(deliveryType: DeliveryTypes){

        readView.visibility = View.VISIBLE

        when(deliveryType){
            DeliveryTypes.SENT -> {
                setIconAndColor(R.drawable.read, R.color.sent)
                readView.background = ContextCompat.getDrawable(readView.context, R.drawable.circle_sent)
            }
            DeliveryTypes.DELIVERED -> {
                setIconAndColor(R.drawable.read, R.color.sent)
                readView.background = ContextCompat.getDrawable(readView.context, R.drawable.circle_sent)
            }
            DeliveryTypes.OPENED -> {
                setIconAndColor(R.drawable.read, R.color.azure)
                readView.background = ContextCompat.getDrawable(readView.context, R.drawable.circle_read)
            }
            DeliveryTypes.NONE -> {
                readView.visibility = View.GONE
            }
        }

        //TODO validate if has attachments
        attachmentView.visibility = View.GONE
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
                zoomLayout.visibility = View.GONE
                webViewLoader.visibility = View.VISIBLE
                return true
            }
            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                WebViewUtils.openUrl(bodyWebView.context!!, request.url.toString())
                zoomLayout.visibility = View.GONE
                webViewLoader.visibility = View.VISIBLE
                return true
            }


            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                zoomLayout.visibility = View.VISIBLE
                webViewLoader.visibility = View.GONE
                view?.evaluateJavascript("""window.scrollTo(0,0);""") { }

                val treeObserver = horizontalScrollView.viewTreeObserver

                treeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                         horizontalScrollView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                         horizontalScrollView.scrollTo(0, 0)
                    }
                })
            }
        }
        val javascriptInterface = WebviewJavascriptInterface(
                context, zoomLayout)
        bodyWebView.addJavascriptInterface(javascriptInterface, "CriptextSecureEmail")
    }

    private fun deactivateElementsForUnsend() {
        bodyContainer.alpha = 0.4.toFloat()
        bodyContainer.isEnabled = false
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
        moreView = view.findViewById(R.id.more)
        replyView = view.findViewById(R.id.reply)
        continueDraftView = view.findViewById(R.id.continue_draft)
        attachmentView =  view.findViewById(R.id.attachment)
        readView =  view.findViewById(R.id.read)
        unsendView =  view.findViewById(R.id.unsend)
        email_options = view.findViewById(R.id.container_my_email_options)

        contactInfoPopUp = EmailContactInfoPopup(toView)
        readHistoryPopUp = ReadHistoryPopUp(readView)
        attachmentHistoryPopUp = AttachmentHistoryPopUp(attachmentView)
        bodyWebView = view.findViewById(R.id.email_body)

        bodyWebView.webChromeClient = WebChromeClient()
        zoomLayout = view.findViewById(R.id.full_mail_zoom)
        horizontalScrollView = view.findViewById(R.id.full_mail_scroll)
        progressBarUnsend = view.findViewById(R.id.spin_kit_unsend)
        bodyContainer = view.findViewById(R.id.body_container)
        webViewLoader = view.findViewById(R.id.progress_bar_webview_loading)

        attachmentsRecyclerView = view.findViewById(R.id.attachments_recycler_view)

        setupWebview()
        horizontalScrollView.isHorizontalScrollBarEnabled = false
        zoomLayout.slideContainer = { dx: Int ->
            horizontalScrollView.smoothScrollBy(dx - horizontalScrollView.scrollX, 0)
        }
    }

}

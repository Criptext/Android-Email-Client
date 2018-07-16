package com.email.scenes.emaildetail.ui.holders

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
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
import com.email.db.models.FileDetail
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.composer.ui.holders.AttachmentViewObserver
import com.email.scenes.emaildetail.WebviewJavascriptInterface
import com.email.scenes.emaildetail.ui.*
import com.email.utils.*
import com.email.utils.ui.ZoomLayout
import com.github.ybq.android.spinkit.SpinKitView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailHolder(view: View) : ParentEmailHolder(view) {

    private val context = view.context
    private val layout : FrameLayout
    private val continueDraftView: ImageView
    private val replyView: ImageView
    private val threePointsView: ImageView
    private val moreButton: TextView
    private val toView: TextView
    private val readView: ImageView
    private val contactInfoPopUp: EmailContactInfoPopup
    private val bodyWebView: WebView
    private val zoomLayout: ZoomLayout
    private val horizontalScrollView: HorizontalScrollView
    private val bodyContainer : LinearLayout
    private val webViewLoader: ProgressBar
    private val attachmentsRecyclerView: RecyclerView
    private val leftImageView: CircleImageView

    override fun setListeners(fullEmail: FullEmail, fileDetails: List<FileDetail>,
                     emailListener: FullEmailListAdapter.OnFullEmailEventListener?,
                     adapter: FullEmailListAdapter, position: Int) {
        view.setOnClickListener {

            emailListener?.ontoggleViewOpen(
                    fullEmail = fullEmail,
                    position = position,
                    viewOpen = false)
        }
        threePointsView.setOnClickListener({
            displayPopMenu(emailListener, fullEmail, adapter, position)
        })

        moreButton.setOnClickListener({
            contactInfoPopUp.createPopup(fullEmail, null)
        })

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

    val popuplayout =
            if (fullEmail.email.unread)
                R.menu.mail_options_unread_menu
            else
                R.menu.mail_options_read_menu

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
            bodyWebView.loadDataWithBaseURL("", HTMLUtils.
                    changedHeaderHtml("This content was unsent"), "text/html", "utf-8", "")
            deactivateElementsForUnsend()
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

        if(fullEmail.email.delivered != DeliveryTypes.NONE) {
            //TODO add unsend option in menu
        }

        leftImageView.setImageBitmap(Utility.getBitmapFromText(
                fullEmail.from.name,
                fullEmail.from.name[0].toString().toUpperCase(), 250, 250))

        setToText(fullEmail)
        setDraftIcon(fullEmail)
        setIcons(fullEmail.email.delivered)
    }

    private fun setAttachments(files: List<FileDetail>, emailListener: FullEmailListAdapter.OnFullEmailEventListener?){
        val adapter = FileListAdapter(view.context, files)
        val mLayoutManager = LinearLayoutManager(view.context)
        adapter.observer = object: AttachmentViewObserver {
            override fun onAttachmentViewClick(position: Int) {
                emailListener?.onAttachmentSelected(adapterPosition, position)
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
                || EmailThreadValidator.isLabelInList(fullEmail.labels, SecureEmail.LABEL_DRAFT))
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
            DeliveryTypes.SENDING -> {
                setIconAndColor(R.drawable.clock, R.color.sent)
            }
            DeliveryTypes.SENT -> {
                setIconAndColor(R.drawable.read, R.color.sent)
            }
            DeliveryTypes.DELIVERED -> {
                setIconAndColor(R.drawable.read, R.color.sent)
            }
            DeliveryTypes.READ -> {
                setIconAndColor(R.drawable.read, R.color.azure)
            }
            DeliveryTypes.NONE -> {
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
        threePointsView = view.findViewById(R.id.more)
        moreButton = view.findViewById(R.id.more_text)
        replyView = view.findViewById(R.id.reply)
        continueDraftView = view.findViewById(R.id.continue_draft)
        readView =  view.findViewById(R.id.check)
        contactInfoPopUp = EmailContactInfoPopup(moreButton)
        bodyWebView = view.findViewById(R.id.email_body)
        bodyWebView.webChromeClient = WebChromeClient()
        zoomLayout = view.findViewById(R.id.full_mail_zoom)
        horizontalScrollView = view.findViewById(R.id.full_mail_scroll)
        bodyContainer = view.findViewById(R.id.body_container)
        webViewLoader = view.findViewById(R.id.progress_bar_webview_loading)
        attachmentsRecyclerView = view.findViewById(R.id.attachments_recycler_view)
        leftImageView = view.findViewById(R.id.mail_item_left_name)

        setupWebview()
        horizontalScrollView.isHorizontalScrollBarEnabled = false
        zoomLayout.slideContainer = { dx: Int ->
            horizontalScrollView.smoothScrollBy(dx - horizontalScrollView.scrollX, 0)
        }
    }

}

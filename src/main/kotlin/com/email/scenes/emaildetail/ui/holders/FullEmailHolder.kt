package com.email.scenes.emaildetail.ui.holders

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v7.widget.PopupMenu
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.email.R
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.WebviewJavascriptInterface
import com.email.scenes.emaildetail.ui.AttachmentHistoryPopUp
import com.email.scenes.emaildetail.ui.EmailContactInfoPopup
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.emaildetail.ui.ReadHistoryPopUp
import com.email.utils.Utility
import com.email.utils.ZoomLayout

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailHolder(view: View) : ParentEmailHolder(view) {

    private val context = view.context
    private val layout : FrameLayout
    private val replyView: ImageView
    private val moreView: ImageView
    private val toView: TextView
    private val attachmentView: ImageView
    private val readView: ImageView
    private val unsendView: ImageView
    private val layoutAttachment : RelativeLayout
    private val contactInfoPopUp: EmailContactInfoPopup
    private val readHistoryPopUp: ReadHistoryPopUp
    private val attachmentHistoryPopUp: AttachmentHistoryPopUp
    private val bodyWebView: WebView
    private val zoomLayout: ZoomLayout
    private val horizontalScrollView: HorizontalScrollView

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

        layoutAttachment.setOnClickListener{
            TODO("HANDLE CLICK TO ATTACHMENT")
        }
    }

    private fun displayPopMenu(emailListener: FullEmailListAdapter.OnFullEmailEventListener?, fullEmail: FullEmail,
                               adapter: FullEmailListAdapter, position: Int){
        val popupMenu = createPopupMenu(fullEmail)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.forward ->
                    emailListener?.onReplyOptionSelected(
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
            }
            false
        }

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
        //bodyView.text = fullEmail.email.content
        bodyWebView.loadDataWithBaseURL("", Utility.
                changedHeaderHtml(fullEmail.email.content), "text/html", "utf-8", "")
        val numberContacts = fullEmail.to.size
        if(fullEmail.from != null) {
            headerView.text = fullEmail.from.name
            toView.text = "To me and ${numberContacts  - 1} contacts"
        }
        else {
            headerView.text = "Me"
            replyView.visibility = View.GONE
            toView.text = "To ${numberContacts} contacts"
        }
    }

    fun setupWebview(){
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
                Utility.openUrl(bodyWebView.context!!, url)
                return true
            }
            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                Utility.openUrl(bodyWebView.context!!, request.url.toString())
                return true
            }


            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        val javascriptInterface = WebviewJavascriptInterface(
                context, zoomLayout)
        bodyWebView.addJavascriptInterface(javascriptInterface, "CriptextSecureEmail")
    }

    init {
        layout = view.findViewById(R.id.open_full_mail_item_container)
        toView = view.findViewById(R.id.to)
        moreView = view.findViewById(R.id.more)
        replyView = view.findViewById(R.id.reply)
        attachmentView =  view.findViewById(R.id.attachment)
        readView =  view.findViewById(R.id.read)
        unsendView =  view.findViewById(R.id.unsend)

        layoutAttachment = view.findViewById(R.id.open_full_mail_attachment_container)
        contactInfoPopUp = EmailContactInfoPopup(toView)
        readHistoryPopUp = ReadHistoryPopUp(readView)
        attachmentHistoryPopUp = AttachmentHistoryPopUp(attachmentView)
        bodyWebView = view.findViewById(R.id.email_body)

        bodyWebView.webChromeClient = WebChromeClient()
        zoomLayout = view.findViewById(R.id.full_mail_zoom)
        horizontalScrollView = view.findViewById(R.id.full_mail_scroll)

        setupWebview()

        zoomLayout.slideContainer = { dx: Int ->
            horizontalScrollView.smoothScrollBy(dx - horizontalScrollView.scrollX, 0)
        }
    }

}

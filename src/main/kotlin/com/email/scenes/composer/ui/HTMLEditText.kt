package com.email.scenes.composer.ui

import android.view.View
import com.email.R
import com.email.utils.WebViewUtils
import kotlinx.android.synthetic.main.activity_composer.view.*
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.glideloader.GlideImageLoader
import org.wordpress.aztec.picassoloader.PicassoImageLoader
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener

/**
 * Created by gabriel on 5/23/17.
 */

class HTMLEditText(private val visualEditor: AztecText,
                   private val sourceEditor: SourceViewEditText?,
                   private val toolbar: AztecToolbar, hint: String): IAztecToolbarClickListener {

    var text: String
    set(value) {
        visualEditor.fromHtml(value)
        sourceEditor?.setText(value)
        toolbar.toggleEditorMode()
    }
    get() = visualEditor.toPlainHtml(false).replace("<br><br>", "<br>")
    //I have to do this replace because the library gives me two br on each new line.

    val view: View = visualEditor

    var onFocusChangeListener: View.OnFocusChangeListener?
        set(value) { visualEditor.onFocusChangeListener = value }
        get() = visualEditor.onFocusChangeListener

    init {
        visualEditor.hint = hint
        if(sourceEditor != null) {
            Aztec.with(
                    sourceEditor = sourceEditor,
                    visualEditor = visualEditor,
                    toolbar = toolbar,
                    toolbarClickListener = this)
                    .setImageGetter(PicassoImageLoader(view.context, visualEditor))
        }
        else{
            Aztec.with(
                    visualEditor = visualEditor,
                    toolbar = toolbar,
                    toolbarClickListener = this)
        }
    }

    fun setMinHeight(){
        visualEditor.minHeight = 150
    }

    fun setFocus(){
        visualEditor.requestFocus()
    }

    override fun onToolbarCollapseButtonClicked() {
    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
    }

    override fun onToolbarHeadingButtonClicked() {
    }

    override fun onToolbarHtmlButtonClicked() {
        toolbar.toggleEditorMode()
    }

    override fun onToolbarListButtonClicked() {
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }

}
package com.email.scenes.composer.ui

import android.graphics.Color
import android.view.View
import android.widget.ScrollView
import com.email.R
import jp.wasabeef.richeditor.RichEditor

/**
 * Created by gabriel on 5/23/17.
 */

class HTMLEditText(private val richEditor: RichEditor, scrollView: ScrollView) {

    var text: String
    set(value) {
        richEditor.html = "<div>$value</div>"
    }
    get() = if(richEditor.html != null) richEditor.html else ""

    val view: View = richEditor

    var onFocusChangeListener: View.OnFocusChangeListener?
        set(value) { richEditor.onFocusChangeListener = value }
        get() = richEditor.onFocusChangeListener

    init {
        richEditor.setEditorBackgroundColor(Color.TRANSPARENT)
        richEditor.settings.allowFileAccess = true
        richEditor.setPlaceholder(view.context.resources.getString(R.string.message))
        richEditor.viewTreeObserver.addOnScrollChangedListener {
            scrollView.post {
                scrollView.scrollTo(0, scrollView.bottom);
            }
        }
    }

    fun setMinHeight(){
        richEditor.setEditorHeight(150)
    }

}
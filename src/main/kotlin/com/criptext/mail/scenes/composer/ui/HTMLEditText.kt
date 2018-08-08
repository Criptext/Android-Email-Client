package com.criptext.mail.scenes.composer.ui

import android.graphics.Color
import android.os.Handler
import android.view.View
import com.criptext.mail.utils.WebViewUtils
import jp.wasabeef.richeditor.RichEditor

/**
 * Created by gabriel on 5/23/17.
 */

class HTMLEditText(private val richEditor: RichEditor, hint: String){

    var text: String
    set(value) {
        richEditor.html = value
    }
    get() = richEditor.html

    val view: View = richEditor

    init {
        richEditor.setEditorBackgroundColor(Color.TRANSPARENT)
        richEditor.settings.allowFileAccess = true
        richEditor.setPlaceholder(hint)
        richEditor.setEditorFontSize(17)
    }

    fun setMinHeight(){
        richEditor.setEditorHeight(150)
    }

    fun setFocus(){
        richEditor.focusEditor()
    }

}
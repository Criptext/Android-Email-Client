package com.criptext.mail.scenes.composer.ui

import android.graphics.Color
import android.support.v7.app.AppCompatDelegate
import android.view.View
import com.criptext.mail.R
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
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            richEditor.setEditorFontColor(Color.WHITE)
            richEditor.setTextColor(Color.WHITE)
        }
        richEditor.setOnInitialLoadListener {
            if(it){
                richEditor.evaluateJavascript("javascript:RE.setBackgroundColor('transparent');", null)
            }
        }
    }

    fun setMinHeight(){
        richEditor.setEditorHeight(150)
    }

    fun setFocus(){
        richEditor.focusEditor()
    }

}
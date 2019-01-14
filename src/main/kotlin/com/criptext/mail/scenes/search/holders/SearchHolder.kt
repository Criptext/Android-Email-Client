package com.criptext.mail.scenes.search.holders

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.criptext.mail.R

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchHolder(view: View): RecyclerView.ViewHolder(view){

    private val textViewSubject : TextView = view.findViewById(R.id.textViewSubject)
    val rootView: View = view.findViewById(R.id.rootView)

    fun bindWithSearch(search: String){
        textViewSubject.text = search
    }

}
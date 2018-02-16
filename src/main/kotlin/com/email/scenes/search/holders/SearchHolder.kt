package com.email.scenes.search.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.email.R
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.search.data.SearchResult
import com.squareup.picasso.Picasso

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{

    private val textViewSubject : TextView
    private val textViewRecipients : TextView
    private val imageViewTypeSearch : ImageView

    init {
        view.setOnClickListener(this)
        textViewSubject = view.findViewById(R.id.textViewSubject)
        textViewRecipients = view.findViewById(R.id.textViewRecipients)
        imageViewTypeSearch = view.findViewById(R.id.imageViewTypeSearch)
    }

    override fun onClick(p0: View?) {
    }

    fun bindWithSearch(search: SearchResult){
        textViewSubject.text = search.subject
        if(search.recipients.isEmpty()){
            textViewRecipients.visibility = View.GONE
            Picasso.with(imageViewTypeSearch.context).load(R.drawable.clock).into(imageViewTypeSearch)
        }
        else{
            textViewRecipients.visibility = View.VISIBLE
            textViewRecipients.text = search.recipients
            Picasso.with(imageViewTypeSearch.context).load(R.drawable.ic_mail_2_copy).into(imageViewTypeSearch)
        }
    }

}
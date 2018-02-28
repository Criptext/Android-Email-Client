package com.email.scenes.composer.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.email.DB.models.Contact
import com.email.R
import com.email.utils.Utility
import com.tokenautocomplete.FilteredArrayAdapter
import java.util.*

/**
 * Created by gabriel on 2/28/18.
 */
class ContactsFilterAdapter(context : Context, objects : Array<Contact>)
    : FilteredArrayAdapter<Contact>(context, R.layout.autocomplete_item, 0, objects) {

    override fun keepObject(obj: Contact?, mask: String?): Boolean {
        if(mask?.length ?: 0 < 2 || obj == null ){
            return false
        }
        val newMask = mask!!.toLowerCase()
        return obj.name.contains(newMask) || obj.email.contains(newMask)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(LayoutInflater.from(context), position, convertView, parent, R.layout.autocomplete_item);
    }

    fun createViewFromResource(inflater : LayoutInflater, position : Int, convertView: View?, parent: ViewGroup?, resource: Int) : View{
        val view: View = convertView ?: inflater.inflate(resource, parent, false)

        val nameTextView = view.findViewById<TextView>(R.id.auto_name)
        val mailTextView = view.findViewById<TextView>(R.id.auto_mail) as TextView
        val initialTextView = view.findViewById<TextView>(R.id.auto_initial) as TextView
        val circleView = view.findViewById<ImageView>(R.id.auto_circle) as ImageView

        val item = getItem(position)
        nameTextView.text = item.name
        mailTextView.text = item.email
        if(item.email.contains(item.name)){
            nameTextView.visibility = View.GONE
        }
        initialTextView.text = item.name.get(0).toString().toUpperCase()
        // this should use utility function for user avatars, make sure to replace it
        circleView.setColorFilter(MATERIAL_COLORS[item.hashCode() % 17].toInt())

        return view
    }

    companion object{
        private val MATERIAL_COLORS = Arrays.asList(
                0xffe57373,
                0xfff06292,
                0xffba68c8,
                0xff9575cd,
                0xff7986cb,
                0xff64b5f6,
                0xff4fc3f7,
                0xff4dd0e1,
                0xff4db6ac,
                0xff81c784,
                0xffaed581,
                0xffff8a65,
                0xffd4e157,
                0xffffd54f,
                0xffffb74d,
                0xffa1887f,
                0xff90a4ae
        )
    }
}

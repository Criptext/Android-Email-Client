package com.email.utils.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.widget.ImageViewCompat
import android.view.MenuItem
import android.widget.ImageView
import com.email.R

/**
 * Created by sebas on 2/8/18.
 */

class Tint {

    companion object {

        fun addTintToMenuItem(context: Context, item: MenuItem) {
            var drawable: Drawable? = item.icon

            if (drawable == null) {
                val activityFeed = item.actionView
                        .findViewById(R.id.mailbox_activity_feed) as ImageView
                ImageViewCompat.setImageTintList(activityFeed,
                        ColorStateList.valueOf(ContextCompat.getColor
                        (context, R.color.multiModeTint)))
                return
            }

            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.multiModeTint));
            item.setIcon(drawable)
        }

        fun addTintToImage(context: Context, imageView: ImageView) {
            ImageViewCompat.setImageTintList(imageView,
                    ColorStateList.valueOf(ContextCompat.getColor
                    (context, R.color.multiModeTint)))
        }
    }
}

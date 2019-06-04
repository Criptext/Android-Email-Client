package com.criptext.mail.scenes.mailbox.ui.WelcomeTour

import android.content.Context
import android.content.SyncStatusObserver
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import com.airbnb.lottie.LottieAnimationView
import com.criptext.mail.R
import com.criptext.mail.scenes.mailbox.ui.MailboxUIObserver
import com.viewpagerindicator.CirclePageIndicator


class WelcomeTourDialog(val context: Context) {
    private var dialog: AlertDialog? = null
    private val res = context.resources
    private lateinit var view: View
    private lateinit var viewPager: ViewPager
    private lateinit var okButton: Button
    private val pageArr = listOf(
            WelcomePagerModel("Email.json", R.string.slide_1_title, R.string.slide_1_text),
            WelcomePagerModel("Lock.json", R.string.slide_2_title, R.string.slide_2_text),
            WelcomePagerModel("Arm.json", R.string.slide_3_title, R.string.slide_3_text)
    )

    fun showWelcomeTourDialog(observer: MailboxUIObserver) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.welcome_tour_dialog, null)
        okButton = view.findViewById(R.id.ok_button) as Button


        dialogBuilder.setView(view)


        dialog = createDialog(view, dialogBuilder, observer)

    }

    private fun createDialog(dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             observer: MailboxUIObserver): AlertDialog {
        val width = res.getDimension(R.dimen.welcome_tour_width).toInt()
        val height = res.getDimension(R.dimen.welcome_tour__height).toInt()
        val newWelcomeTourDialog = dialogBuilder.create()
        newWelcomeTourDialog.show()

        val window = newWelcomeTourDialog.window
        window?.setLayout(width, height)
        window?.setGravity(Gravity.CENTER_VERTICAL)

        val drawableBackground = ContextCompat.getDrawable(
                dialogView.context, R.drawable.dialog_label_chooser_shape)
        newWelcomeTourDialog.window?.setBackgroundDrawable(drawableBackground)
        newWelcomeTourDialog.setCanceledOnTouchOutside(false)
        newWelcomeTourDialog.setCancelable(false)





        val adapter = WelcomePagerAdapter(context, pageArr)


        viewPager = newWelcomeTourDialog.findViewById<ViewPager>(R.id.view_pager)!!
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(pageChangeListener)

        val pageIndicator = newWelcomeTourDialog.findViewById<CirclePageIndicator>(R.id.circle_indicator)
        pageIndicator?.setViewPager(viewPager)
        pageIndicator?.setCurrentItem(0)
        assignButtonEvents(newWelcomeTourDialog, observer)



        return newWelcomeTourDialog
    }

    private val pageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            val child = viewPager.getChildAt(viewPager.currentItem)
            if(child != null) {
                val anim = child.findViewById<LottieAnimationView>(R.id.welcome_animation)
                anim.playAnimation()
            }else
            {
                val childLeft = viewPager.findViewById<LottieAnimationView>(R.id.welcome_animation)
                childLeft.playAnimation()
            }

        }
        override fun onPageSelected(position: Int) {
            okButton.isEnabled = position == 2
        }

    }

    fun assignButtonEvents(dialog: AlertDialog, observer: MailboxUIObserver) {
        okButton.setOnClickListener {
            observer.onWelcomeTourHasFinished()
            dialog.dismiss()
        }
    }

}

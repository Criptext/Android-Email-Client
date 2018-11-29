package com.criptext.mail.utils.ui

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptions
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.CirclePromptBackground


/**
 * Prompt background implementation that darkens behind the circle background.
 */
class DimmedPromptBackground : CirclePromptBackground() {
    private val dimBounds = RectF()
    private val dimPaint: Paint

    init {
        dimPaint = Paint()
        dimPaint.color = Color.BLACK
    }

    override fun prepare(options: PromptOptions<*>, clipToBounds: Boolean, clipBounds: Rect) {
        super.prepare(options, clipToBounds, clipBounds)
        val metrics = Resources.getSystem().displayMetrics
        // Set the bounds to display as dimmed to the screen bounds
        dimBounds.set(0f, 0f, metrics.widthPixels.toFloat(), metrics.heightPixels.toFloat())
    }

    override fun update(options: PromptOptions<*>, revealModifier: Float, alphaModifier: Float) {
        super.update(options, revealModifier, alphaModifier)
        // Allow for the dimmed background to fade in and out
        this.dimPaint.alpha = (230 * alphaModifier).toInt()
    }

    override fun draw(canvas: Canvas) {
        // Draw the dimmed background
        canvas.drawRect(this.dimBounds, this.dimPaint)
        // Draw the background
        super.draw(canvas)
    }
}
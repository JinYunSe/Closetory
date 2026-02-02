package com.ssafy.closetory.util.ui

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import com.ssafy.closetory.R
import kotlin.math.max
import kotlin.math.min

class BalloonTooltip(private val ctx: Context) {

    private var popup: PopupWindow? = null

    fun dismiss() {
        popup?.dismiss()
        popup = null
    }

    fun show(anchor: View, message: String, marginDp: Int = 8, autoDismissMs: Long? = 2500) {
        dismiss()

        val root = LayoutInflater.from(ctx).inflate(R.layout.view_balloon_tooltip, null, false)
        val tv = root.findViewById<TextView>(R.id.tv_message)
        val arrowUp = root.findViewById<ImageView>(R.id.arrow_up)
        val arrowDown = root.findViewById<ImageView>(R.id.arrow_down)

        tv.text = message

        root.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val popupW = root.measuredWidth
        val popupH = root.measuredHeight

        val pw = PopupWindow(
            root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(0x00000000))
            elevation = dp(8).toFloat()
        }
        popup = pw

        val anchorRect = Rect().apply { anchor.getGlobalVisibleRect(this) }

        val screenRect = Rect()
        (ctx as? Activity)?.window?.decorView?.getWindowVisibleDisplayFrame(screenRect)
            ?: screenRect.set(0, 0, ctx.resources.displayMetrics.widthPixels, ctx.resources.displayMetrics.heightPixels)

        val marginPx = dp(marginDp)

        val spaceAbove = anchorRect.top - screenRect.top
        val spaceBelow = screenRect.bottom - anchorRect.bottom
        val showAbove = spaceAbove >= popupH + marginPx || spaceAbove > spaceBelow

        val anchorCenterX = (anchorRect.left + anchorRect.right) / 2

        var x = anchorCenterX - popupW / 2
        x = clamp(x, screenRect.left + marginPx, screenRect.right - popupW - marginPx)

        val y = if (showAbove) {
            anchorRect.top - popupH - marginPx
        } else {
            anchorRect.bottom + marginPx
        }

        arrowUp.isVisible = !showAbove
        arrowDown.isVisible = showAbove

        anchor.post {
            if (anchor.windowToken == null) return@post

            pw.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)

            val arrow = if (showAbove) arrowDown else arrowUp
            arrow.post {
                val arrowW = arrow.width
                val desired = anchorCenterX - x - arrowW / 2
                arrow.translationX =
                    clamp(desired, 0, (root.width.takeIf { it > 0 } ?: popupW) - arrowW).toFloat()
            }

            if (autoDismissMs != null) {
                root.postDelayed({ dismiss() }, autoDismissMs)
            }
        }
    }

    private fun dp(dp: Int): Int = (dp * ctx.resources.displayMetrics.density).toInt()

    private fun clamp(v: Int, minV: Int, maxV: Int): Int = max(minV, min(v, maxV))
}

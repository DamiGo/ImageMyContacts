package com.example.imagemycontacts

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class SwipeListener(
    context: Context,
    private val onSwipeRight: () -> Unit,
    private val onSwipeLeft: () -> Unit
) : View.OnTouchListener {

    private val detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null || e2 == null) return false
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            return if (abs(diffX) > abs(diffY) && abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) onSwipeRight() else onSwipeLeft()
                true
            } else {
                false
            }
        }
    })

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return detector.onTouchEvent(event)
    }
}

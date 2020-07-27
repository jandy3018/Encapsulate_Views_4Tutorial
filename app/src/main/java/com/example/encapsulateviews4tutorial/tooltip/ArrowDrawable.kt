package com.example.encapsulateviews4tutorial.tooltip

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import androidx.annotation.ColorInt

/**
 * Implementation arrow drawable for [Tooltip]
 */
internal class ArrowDrawable(
    @ColorInt foregroundColor: Int,
    private val mGravity: Int,
    private val mBorderArrowEnabled: Boolean,
    @ColorInt borderColorArrow: Int
) : ColorDrawable() {
    private val mPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBackgroundColor: Int
    private var mPath: Path? = null
    var fillPaint = Paint()
    var strokePaint = Paint()
    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updatePath(bounds)
    }

    @Synchronized
    private fun updatePath(bounds: Rect) {
        mPath = Path()
        var x1 = bounds.width() * 5 / 100.toFloat()
        var y1 = bounds.height() * 5 / 100.toFloat()
        var x2: Float
        val y2: Float
        when (mGravity) {
            Gravity.LEFT -> {
                y1 *= 2.5f
                x2 = x1 * 10f
                y2 = y1 * 3f
                x1 /= 4f
                y1 *= 2f
                mPath!!.moveTo(0f, 0f)
                mPath!!.cubicTo(0f, 0f, x1, y1, x2, y2)
                mPath!!.cubicTo(
                    x2,
                    y2,
                    bounds.width() * 1.5f,
                    bounds.height() / 2.toFloat(),
                    x2,
                    bounds.height() - y2
                )
                mPath!!.cubicTo(
                    x2,
                    bounds.height() - y2,
                    x1,
                    bounds.height() - y1,
                    0f,
                    bounds.height().toFloat()
                )
            }
            Gravity.TOP -> {
                val point1_draw1 = Point(30, 0)
                val point2_draw2 = Point(0, 0)
                val point3_draw3 = Point(15, 30)
                mPath!!.moveTo(point1_draw1.x.toFloat(), point1_draw1.y.toFloat())
                mPath!!.lineTo(point2_draw2.x.toFloat(), point2_draw2.y.toFloat())
                mPath!!.lineTo(point3_draw3.x.toFloat(), point3_draw3.y.toFloat())
                mPath!!.lineTo(point1_draw1.x.toFloat(), point1_draw1.y.toFloat())
            }
            Gravity.RIGHT -> {
                y1 *= 2.5f
                x2 = x1 * 10f
                y2 = y1 * 3f
                x1 /= 4f
                y1 *= 2f
                x1 = bounds.width() - x1
                x2 = bounds.width() - x2
                mPath!!.moveTo(bounds.width().toFloat(), 0f)
                mPath!!.cubicTo(bounds.width().toFloat(), 0f, x1, y1, x2, y2)
                mPath!!.cubicTo(
                    x2,
                    y2,
                    -x2,
                    bounds.height() / 2.toFloat(),
                    x2,
                    bounds.height() - y2
                )
                mPath!!.cubicTo(
                    x2,
                    bounds.height() - y2,
                    x1,
                    bounds.height() - y1,
                    bounds.width().toFloat(),
                    bounds.height().toFloat()
                )
            }
            Gravity.BOTTOM -> {
                val point1_draw = Point(30, 30)
                val point2_draw = Point(16, 0)
                val point3_draw = Point(0, 30)
                mPath!!.moveTo(point1_draw.x.toFloat(), point1_draw.y.toFloat())
                mPath!!.lineTo(point2_draw.x.toFloat(), point2_draw.y.toFloat())
                mPath!!.lineTo(point3_draw.x.toFloat(), point3_draw.y.toFloat())
                mPath!!.lineTo(point1_draw.x.toFloat(), point1_draw.y.toFloat())
            }
        }
        mPath!!.close()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(mBackgroundColor)
        if (mPath == null) {
            updatePath(copyBounds())
        }
        if (mBorderArrowEnabled) {
            canvas.drawPath(mPath!!, fillPaint)
            canvas.drawPath(mPath!!, strokePaint)
        } else {
            canvas.drawPath(mPath!!, mPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColor(@ColorInt color: Int) {
        mPaint.color = color
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        if (mPaint.colorFilter != null) {
            return PixelFormat.TRANSLUCENT
        }
        when (mPaint.color ushr 24) {
            255 -> return PixelFormat.OPAQUE
            0 -> return PixelFormat.TRANSPARENT
        }
        return PixelFormat.TRANSLUCENT
    }

    init {
        mBackgroundColor = Color.TRANSPARENT
        if (mBorderArrowEnabled) {
            fillPaint.style = Paint.Style.FILL
            fillPaint.color = foregroundColor
            strokePaint.style = Paint.Style.STROKE
            strokePaint.color = borderColorArrow
            strokePaint.strokeWidth = 2f
        } else {
            mPaint.color = foregroundColor
        }
    }
}
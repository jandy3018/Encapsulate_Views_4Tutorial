package com.example.encapsulateviews4tutorial.tooltip

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import com.example.encapsulateviews4tutorial.R
import com.example.encapsulateviews4tutorial.tooltip.Tooltip
import com.example.encapsulateviews4tutorial.tooltip.TooltipUtils.calculateRectInWindow
import com.example.encapsulateviews4tutorial.tooltip.TooltipUtils.calculateRectOnScreen
import com.example.encapsulateviews4tutorial.tooltip.TooltipUtils.dpToPx

class Tooltip private constructor(builder: Builder) {
    private var isDismissOnClick: Boolean? = null
    private var mGravity: Int = 0
    private var mMargin: Float = 0f
    private var mAnchorView: View? = null
    private var mPopupWindow: PopupWindow? = null
    private var mOnClickListener: OnTooltipClickListener? = null
    private var mOnDismissListener: OnDismissListener?
    private var mContentView: LinearLayout? = null
    private var mArrowView: ImageView? = null
    private fun getContentView(builder: Builder): View {
        val textView = TextView(builder.mContext)
        TextViewCompat.setTextAppearance(textView, builder.mTextAppearance)
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            textView,
            builder.mDrawableStart,
            builder.mDrawableTop,
            builder.mDrawableEnd,
            builder.mDrawableBottom
        )
        textView.text = builder.mText
        textView.setPadding(builder.mPadding, builder.mPadding, builder.mPadding, builder.mPadding)
        textView.setLineSpacing(builder.mLineSpacingExtra, builder.mLineSpacingMultiplier)
        textView.setTypeface(builder.mTypeface, builder.mTextStyle)
        textView.compoundDrawablePadding = builder.mDrawablePadding
        if (builder.mMaxWidth >= 0) {
            textView.maxWidth = builder.mMaxWidth
        }
        if (builder.mTextSize >= 0) {
            textView.setTextSize(TypedValue.TYPE_NULL, builder.mTextSize)
        }
        if (builder.mTextColor != null) {
            textView.setTextColor(builder.mTextColor)
        }
        val textViewParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            0F
        )
        textViewParams.gravity = Gravity.CENTER
        textView.layoutParams = textViewParams
        val drawable = GradientDrawable()
        drawable.setColor(builder.mBackgroundColor)
        drawable.cornerRadius = builder.mCornerRadius
        drawable.setStroke(2, builder.mBorderColor)
        ViewCompat.setBackground(textView, drawable)
        mContentView = LinearLayout(builder.mContext)
        mContentView!!.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mContentView!!.orientation =
            if (Gravity.isHorizontal(mGravity)) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        if (builder.isArrowEnabled) {
            mArrowView = ImageView(builder.mContext)
            mArrowView!!.setImageDrawable(
                if (builder.mArrowDrawable == null) ArrowDrawable(
                    builder.mBackgroundColor,
                    mGravity,
                    builder.isBorderArrowEnabled,
                    builder.mBorderColorArrow
                ) else builder.mArrowDrawable
            )
            val arrowLayoutParams: LinearLayout.LayoutParams
            arrowLayoutParams = if (Gravity.isVertical(mGravity)) {
                LinearLayout.LayoutParams(
                    builder.mArrowWidth.toInt(),
                    builder.mArrowHeight.toInt(),
                    0F
                )
            } else {
                LinearLayout.LayoutParams(
                    builder.mArrowHeight.toInt(),
                    builder.mArrowWidth.toInt(),
                    0F
                )
            }
            arrowLayoutParams.gravity = Gravity.CENTER
            mArrowView!!.layoutParams = arrowLayoutParams
            if (mGravity == Gravity.TOP || mGravity == Gravity.getAbsoluteGravity(
                    Gravity.START,
                    ViewCompat.getLayoutDirection(mAnchorView!!)
                )
            ) {
                mContentView!!.addView(textView)
                mContentView!!.addView(mArrowView)
            } else {
                mContentView!!.addView(mArrowView)
                mContentView!!.addView(textView)
            }
        } else {
            mContentView!!.addView(textView)
        }
        val padding: Int = dpToPx(25f).toInt()
        when (mGravity) {
            Gravity.LEFT -> mContentView!!.setPadding(padding, 0, 0, 0)
            Gravity.TOP, Gravity.BOTTOM -> mContentView!!.setPadding(padding, 0, padding, 0)
            Gravity.RIGHT -> mContentView!!.setPadding(0, 0, padding, 0)
        }
        mContentView!!.setOnClickListener(mClickListener)
        return mContentView!!
    }

    /**
     * Indicate whether this [Tooltip] is showing on screen
     *
     * @return true if [Tooltip] is showing, false otherwise
     */
    val isShowing: Boolean
        get() = mPopupWindow!!.isShowing

    /**
     * Display the [Tooltip] anchored to the custom gravity of the anchor view
     *
     * @see .dismiss
     */
    fun show() {
        if (!isShowing) {
            mContentView!!.viewTreeObserver.addOnGlobalLayoutListener(mLocationLayoutListener)
            mAnchorView!!.addOnAttachStateChangeListener(mOnAttachStateChangeListener)
            mAnchorView!!.post {
                if (mAnchorView!!.isShown) {
                    mPopupWindow!!.showAsDropDown(mAnchorView)
                } else {
                    Log.e(
                        TAG,
                        "Tooltip cannot be shown, root view is invalid or has been closed"
                    )
                }
            }
        }
    }

    /**
     * Disposes of the [Tooltip]. This method can be invoked only after
     * [.show] has been executed. Failing that, calling this method
     * will have no effect
     *
     * @see .show
     */
    fun dismiss() {
        mPopupWindow!!.dismiss()
    }

    /**
     * Sets listener to be called when the [Tooltip] is clicked.
     *
     * @param listener The listener.
     */
    fun setOnClickListener(listener: OnTooltipClickListener?) {
        mOnClickListener = listener
    }

    /**
     * Sets listener to be called when [Tooltip] is dismissed.
     *
     * @param listener The listener.
     */
    fun setOnDismissListener(listener: OnDismissListener?) {
        mOnDismissListener = listener
    }

    private fun calculateLocation(): PointF {
        val location = PointF()
        val anchorRect = calculateRectInWindow(mAnchorView!!)
        val anchorCenter = PointF(anchorRect.centerX(), anchorRect.centerY())
        when (mGravity) {
            Gravity.LEFT -> {
                location.x = anchorRect.left - mContentView!!.width - mMargin
                location.y = anchorCenter.y - mContentView!!.height / 2f
            }
            Gravity.RIGHT -> {
                location.x = anchorRect.right + mMargin
                location.y = anchorCenter.y - mContentView!!.height / 2f
            }
            Gravity.TOP -> {
                location.x = anchorCenter.x - mContentView!!.width / 2f
                location.y = anchorRect.top - mContentView!!.height - mMargin
            }
            Gravity.BOTTOM -> {
                location.x = anchorCenter.x - mContentView!!.width / 2f
                location.y = anchorRect.bottom + mMargin
            }
        }
        return location
    }

    private val mClickListener =
        View.OnClickListener {
            if (mOnClickListener != null) {
                mOnClickListener!!.onClick(this@Tooltip)
            }
            if (isDismissOnClick!!) {
                dismiss()
            }
        }
    private val mLocationLayoutListener = OnGlobalLayoutListener {
        val vto = mAnchorView!!.viewTreeObserver
        vto?.addOnScrollChangedListener(mOnScrollChangedListener)
        if (mArrowView != null) {
            mContentView!!.viewTreeObserver.addOnGlobalLayoutListener(mArrowLayoutListener)
        }
        val location = calculateLocation()
        mPopupWindow!!.isClippingEnabled = true
        mPopupWindow!!.update(
            location.x.toInt(),
            location.y.toInt(),
            mPopupWindow!!.width,
            mPopupWindow!!.height
        )
    }
    private val mArrowLayoutListener = OnGlobalLayoutListener {
        val anchorRect = calculateRectOnScreen(mAnchorView!!)
        val contentViewRect = calculateRectOnScreen(mContentView!!)
        var x: Float
        var y: Float
        if (Gravity.isVertical(mGravity!!)) {
            x = mContentView!!.paddingLeft + dpToPx(2f)
            val centerX =
                contentViewRect.width() / 2f - mArrowView!!.width / 2f
            val newX =
                centerX - (contentViewRect.centerX() - anchorRect.centerX())
            if (newX > x) {
                x = if (newX + mArrowView!!.width + x > contentViewRect.width()) {
                    contentViewRect.width() - mArrowView!!.width - x
                } else {
                    newX
                }
            }
            y = mArrowView!!.top.toFloat()
            y = y + if (mGravity == Gravity.TOP) -1 else +1
        } else {
            y = mContentView!!.paddingTop + dpToPx(2f)
            val centerY =
                contentViewRect.height() / 2f - mArrowView!!.height / 2f
            val newY =
                centerY - (contentViewRect.centerY() - anchorRect.centerY())
            if (newY > y) {
                y = if (newY + mArrowView!!.height + y > contentViewRect.height()) {
                    contentViewRect.height() - mArrowView!!.height - y
                } else {
                    newY
                }
            }
            x = mArrowView!!.left.toFloat()
            x = x + if (mGravity == Gravity.LEFT) -1 else +1
        }
        mArrowView!!.x = x + 16
        mArrowView!!.y = y
    }
    private val mOnScrollChangedListener =
        OnScrollChangedListener {
            val location = calculateLocation()
            mPopupWindow!!.update(
                location.x.toInt(),
                location.y.toInt(),
                mPopupWindow!!.width,
                mPopupWindow!!.height
            )
        }
    private val mOnAttachStateChangeListener: View.OnAttachStateChangeListener =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                dismiss()
            }
        }

    class Builder @JvmOverloads constructor(
        anchorView: View,
        @StyleRes resId: Int = 0
    ) {
        var isDismissOnClick = false
        var isCancelable = false
        var isArrowEnabled = false
        var isBorderArrowEnabled = false
        var mBackgroundColor = 0
        var mBorderColor = 0
        var mBorderColorArrow = 0
        var mGravity = 0
        var mTextAppearance = 0
        var mTextStyle = 0
        var mPadding = 0
        var mMaxWidth = 0
        var mDrawablePadding = 0
        var mCornerRadius = 0f
        var mArrowHeight = 0f
        var mArrowWidth = 0f
        var mMargin = 0f
        var mTextSize = 0f
        var mLineSpacingExtra = 0f
        var mLineSpacingMultiplier = 1f
        var mDrawableBottom: Drawable? = null
        var mDrawableEnd: Drawable? = null
        var mDrawableStart: Drawable? = null
        var mDrawableTop: Drawable? = null
        var mArrowDrawable: Drawable? = null
        var mText: CharSequence? = null
        var mTextColor: ColorStateList? = null
        var mTypeface = Typeface.DEFAULT
        var mContext: Context? = null
        var mAnchorView: View? = null
        var mOnClickListener: OnTooltipClickListener? = null
        var mOnDismissListener: OnDismissListener? =
            null

        private fun init(
            context: Context,
            anchorView: View,
            @StyleRes resId: Int
        ) {
            mContext = context
            mAnchorView = anchorView
            val a = context.obtainStyledAttributes(resId, R.styleable.Tooltip)
            isCancelable = a.getBoolean(R.styleable.Tooltip_cancelable, false)
            isDismissOnClick = a.getBoolean(R.styleable.Tooltip_dismissOnClick, false)
            isArrowEnabled = a.getBoolean(R.styleable.Tooltip_arrowEnabled, true)
            isBorderArrowEnabled = a.getBoolean(R.styleable.Tooltip_borderArrowEnabled, false)
            mBackgroundColor =
                a.getColor(R.styleable.Tooltip_backgroundColor, Color.GRAY)
            mBorderColor = a.getColor(
                R.styleable.Tooltip_borderColor,
                ContextCompat.getColor(mContext!!, R.color.color_grey)
            )
            mBorderColorArrow =
                a.getColor(R.styleable.Tooltip_borderColor, Color.GRAY)
            mCornerRadius = a.getDimension(R.styleable.Tooltip_cornerRadius, -1f)
            mArrowHeight = a.getDimension(R.styleable.Tooltip_arrowHeight, -1f)
            mArrowWidth = a.getDimension(R.styleable.Tooltip_arrowWidth, -1f)
            mArrowDrawable = a.getDrawable(R.styleable.Tooltip_arrowDrawable)
            mMargin = a.getDimension(R.styleable.Tooltip_margin, -1f)
            mPadding = a.getDimensionPixelSize(R.styleable.Tooltip_android_padding, -1)
            mGravity = a.getInteger(R.styleable.Tooltip_android_gravity, Gravity.BOTTOM)
            mMaxWidth = a.getDimensionPixelSize(R.styleable.Tooltip_android_maxWidth, -1)
            mDrawablePadding =
                a.getDimensionPixelSize(R.styleable.Tooltip_android_drawablePadding, 0)
            mDrawableBottom = a.getDrawable(R.styleable.Tooltip_android_drawableBottom)
            mDrawableEnd = a.getDrawable(R.styleable.Tooltip_android_drawableEnd)
            mDrawableStart = a.getDrawable(R.styleable.Tooltip_android_drawableStart)
            mDrawableTop = a.getDrawable(R.styleable.Tooltip_android_drawableTop)
            mTextAppearance = a.getResourceId(R.styleable.Tooltip_textAppearance, -1)
            mText = a.getString(R.styleable.Tooltip_android_text)
            mTextSize = a.getDimension(R.styleable.Tooltip_android_textSize, -1f)
            mTextColor = a.getColorStateList(R.styleable.Tooltip_android_textColor)
            mTextStyle = a.getInteger(R.styleable.Tooltip_android_textStyle, -1)
            mLineSpacingExtra =
                a.getDimensionPixelSize(R.styleable.Tooltip_android_lineSpacingExtra, 0).toFloat()
            mLineSpacingMultiplier = a.getFloat(
                R.styleable.Tooltip_android_lineSpacingMultiplier,
                mLineSpacingMultiplier
            )
            val fontFamily = a.getString(R.styleable.Tooltip_android_fontFamily)
            val typefaceIndex = a.getInt(R.styleable.Tooltip_android_typeface, -1)
            mTypeface = getTypefaceFromAttr(fontFamily, typefaceIndex, mTextStyle)
            a.recycle()
        }

        /**
         * Sets whether [Tooltip] is cancelable or not. Default is `false`
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setCancelable(cancelable: Boolean): Builder {
            isCancelable = cancelable
            return this
        }

        /**
         * Sets whether [Tooltip] is dismissing on click or not. Default is `false`
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDismissOnClick(isDismissOnClick: Boolean): Builder {
            this.isDismissOnClick = isDismissOnClick
            return this
        }

        /**
         * Sets whether [Tooltip] is arrow enabled. Default is `true`
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setArrowEnabled(isArrowEnabled: Boolean): Builder {
            this.isArrowEnabled = isArrowEnabled
            return this
        }

        /**
         * Sets [Tooltip] background color
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setBackgroundColor(@ColorInt color: Int): Builder {
            mBackgroundColor = color
            return this
        }

        fun setBorderColor(@ColorInt color: Int): Builder {
            mBorderColor = color
            return this
        }

        fun setBorderColorArrow(
            border: Boolean,
            @ColorInt color: Int
        ): Builder {
            isBorderArrowEnabled = border
            mBorderColorArrow = color
            return this
        }

        /**
         * Sets [Tooltip] background drawable corner radius from resource
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setCornerRadius(@DimenRes resId: Int): Builder {
            return setCornerRadius(mContext!!.resources.getDimension(resId))
        }

        /**
         * Sets [Tooltip] background drawable corner radius
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setCornerRadius(radius: Float): Builder {
            mCornerRadius = radius
            return this
        }

        /**
         * Sets [Tooltip] arrow height from resource
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setArrowHeight(@DimenRes resId: Int): Builder {
            return setArrowHeight(mContext!!.resources.getDimension(resId))
        }

        /**
         * Sets [Tooltip] arrow height
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setArrowHeight(height: Float): Builder {
            mArrowHeight = height
            return this
        }

        /**
         * Sets [Tooltip] arrow width from resource
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setArrowWidth(@DimenRes resId: Int): Builder {
            return setArrowWidth(mContext!!.resources.getDimension(resId))
        }

        /**
         * Sets [Tooltip] arrow width
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setArrowWidth(width: Float): Builder {
            mArrowWidth = width
            return this
        }

        /**
         * Sets [Tooltip] arrow drawable from resources
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setArrow(@DrawableRes resId: Int): Builder {
            return setArrow(ResourcesCompat.getDrawable(mContext!!.resources, resId, null))
        }

        /**
         * Sets [Tooltip] arrow drawable
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setArrow(arrowDrawable: Drawable?): Builder {
            mArrowDrawable = arrowDrawable
            return this
        }

        /**
         * Sets [Tooltip] margin from resource
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setMargin(@DimenRes resId: Int): Builder {
            return setMargin(mContext!!.resources.getDimension(resId))
        }

        /**
         * Sets [Tooltip] margin
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setMargin(margin: Float): Builder {
            mMargin = margin
            return this
        }

        /**
         * Sets [Tooltip] padding
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setPadding(padding: Int): Builder {
            mPadding = padding
            return this
        }

        /**
         * Sets [Tooltip] padding
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         *
         */
        @Deprecated("Use {@link #setPadding(int)} instead")
        fun setPadding(padding: Float): Builder {
            return setPadding(padding.toInt())
        }

        /**
         * Sets [Tooltip] gravity
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setGravity(gravity: Int): Builder {
            mGravity = gravity
            return this
        }

        /***
         * Sets [Tooltip] max width
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setMaxWidth(maxWidth: Int): Builder {
            mMaxWidth = maxWidth
            return this
        }

        /**
         * Sets the size of the padding between the drawables and
         * the [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawablePadding(padding: Int): Builder {
            mDrawablePadding = padding
            return this
        }

        /**
         * Sets drawable from resource to the bottom of [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawableBottom(@DrawableRes resId: Int): Builder {
            return setDrawableBottom(
                ResourcesCompat.getDrawable(
                    mContext!!.resources,
                    resId,
                    null
                )
            )
        }

        /**
         * Sets drawable to the bottom of [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawableBottom(drawable: Drawable?): Builder {
            mDrawableBottom = drawable
            return this
        }

        /**
         * Sets drawable from resource to the end of [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawableEnd(@DrawableRes resId: Int): Builder {
            return setDrawableBottom(
                ResourcesCompat.getDrawable(
                    mContext!!.resources,
                    resId,
                    null
                )
            )
        }

        /**
         * Sets drawable to the end of [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawableEnd(drawable: Drawable?): Builder {
            mDrawableEnd = drawable
            return this
        }

        /**
         * Sets drawable from resource to the start of [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawableStart(@DrawableRes resId: Int): Builder {
            return setDrawableStart(
                ResourcesCompat.getDrawable(
                    mContext!!.resources,
                    resId,
                    null
                )
            )
        }

        /**
         * Sets drawable to the start of [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawableStart(drawable: Drawable?): Builder {
            mDrawableStart = drawable
            return this
        }

        /**
         * Sets drawable from resource to the top of [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawableTop(@DrawableRes resId: Int): Builder {
            return setDrawableTop(ResourcesCompat.getDrawable(mContext!!.resources, resId, null))
        }

        /**
         * Sets drawable to the top of [Tooltip] text.
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setDrawableTop(drawable: Drawable?): Builder {
            mDrawableTop = drawable
            return this
        }

        /**
         * Sets [Tooltip] text appearance from the specified style resource
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setTextAppearance(@StyleRes resId: Int): Builder {
            mTextAppearance = resId
            return this
        }

        /**
         * Sets [Tooltip] text from resource
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setText(@StringRes resId: Int): Builder {
            return setText(mContext!!.getString(resId))
        }

        /**
         * Sets [Tooltip] text
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setText(text: CharSequence?): Builder {
            mText = text
            return this
        }

        /**
         * Sets [Tooltip] text size from resource
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setTextSize(@DimenRes resId: Int): Builder {
            mTextSize = mContext!!.resources.getDimension(resId)
            return this
        }

        /**
         * Sets [Tooltip] text size
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setTextSize(size: Float): Builder {
            mTextSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                size,
                mContext!!.resources.displayMetrics
            )
            return this
        }

        /**
         * Sets [Tooltip] text color
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setTextColor(@ColorInt color: Int): Builder {
            mTextColor = ColorStateList.valueOf(color)
            return this
        }

        /**
         * Sets [Tooltip] text style
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setTextStyle(style: Int): Builder {
            mTextStyle = style
            return this
        }

        /**
         * Sets [Tooltip] line spacing. Each line will have its height
         * multiplied by `mult` and have `add` added to it
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setLineSpacing(
            @DimenRes addResId: Int,
            mult: Float
        ): Builder {
            mLineSpacingExtra = mContext!!.resources.getDimensionPixelSize(addResId).toFloat()
            mLineSpacingMultiplier = mult
            return this
        }

        /**
         * Sets [Tooltip] line spacing. Each line will have its height
         * multiplied by `mult` and have `add` added to it
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setLineSpacing(
            add: Float,
            mult: Float
        ): Builder {
            mLineSpacingExtra = add
            mLineSpacingMultiplier = mult
            return this
        }

        /**
         * Sets [Tooltip] text typeface
         *
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setTypeface(typeface: Typeface?): Builder {
            mTypeface = typeface
            return this
        }

        /**
         * Sets listener to be called when the [Tooltip] is clicked
         *
         * @param listener The listener
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setOnClickListener(listener: OnTooltipClickListener?): Builder {
            mOnClickListener = listener
            return this
        }

        /**
         * Sets listener to be called when the [Tooltip] is dismissed
         *
         * @param listener The listener
         * @return This [Builder] object to allow for chaining of calls to set methods
         */
        fun setOnDismissListener(listener: OnDismissListener?): Builder {
            mOnDismissListener = listener
            return this
        }

        /**
         * Creates a [Tooltip] with the arguments supplied to this builder. It does not
         * [Tooltip.show] the tooltip. This allows the user to do any extra processing
         * before displaying the `Tooltip`. Use [.show] if you don't have any other processing
         * to do and want this to be created and displayed
         */
        fun build(): Tooltip {
            if (mArrowHeight == -1f) {
                mArrowHeight = mContext!!.resources.getDimension(R.dimen.margin10dp)
            }
            if (mArrowWidth == -1f) {
                mArrowWidth = mContext!!.resources.getDimension(R.dimen.margin20dp)
            }
            if (mMargin == -1f) {
                mMargin = mContext!!.resources.getDimension(R.dimen.margin0dp)
            }
            if (mPadding == -1) {
                mPadding = mContext!!.resources.getDimensionPixelSize(R.dimen.margin14dp)
            }
            return Tooltip(this)
        }

        /**
         * Builds a [Tooltip] with builder attributes and [Tooltip.show]'s the `Tooltip`
         */
        fun show(): Tooltip {
            val tooltip = build()
            tooltip.show()
            return tooltip
        }

        private fun getTypefaceFromAttr(
            familyName: String?,
            typefaceIndex: Int,
            styleIndex: Int
        ): Typeface? {
            var tf: Typeface? = null
            if (familyName != null) {
                tf = Typeface.create(familyName, styleIndex)
                if (tf != null) {
                    return tf
                }
            }
            when (typefaceIndex) {
                1 -> tf = Typeface.SANS_SERIF
                2 -> tf = Typeface.SERIF
                3 -> tf = Typeface.MONOSPACE
            }
            return tf
        }

        init {
            init(anchorView.context, anchorView, resId)
        }
    }

    companion object {
        private val TAG = Tooltip::class.java.simpleName
    }

    init {
        isDismissOnClick = builder.isDismissOnClick
        mGravity = Gravity.getAbsoluteGravity(
            builder.mGravity,
            ViewCompat.getLayoutDirection(builder.mAnchorView!!)
        )
        mMargin = builder.mMargin
        mAnchorView = builder.mAnchorView
        mOnClickListener = builder.mOnClickListener
        mOnDismissListener = builder.mOnDismissListener
        mPopupWindow = PopupWindow(builder.mContext)
        mPopupWindow!!.isClippingEnabled = false
        mPopupWindow!!.width = ViewGroup.LayoutParams.WRAP_CONTENT
        mPopupWindow!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mPopupWindow!!.contentView = getContentView(builder)
        mPopupWindow!!.setBackgroundDrawable(ColorDrawable())
        mPopupWindow!!.isOutsideTouchable = builder.isCancelable
        mPopupWindow!!.setOnDismissListener {
            mAnchorView!!.viewTreeObserver
                .removeOnScrollChangedListener(mOnScrollChangedListener)
            mAnchorView!!.removeOnAttachStateChangeListener(mOnAttachStateChangeListener)
            if (mOnDismissListener != null) {
                mOnDismissListener!!.onDismiss()
            }
        }
    }
}
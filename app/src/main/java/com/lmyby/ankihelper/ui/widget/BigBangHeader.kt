package com.lmyby.ankihelper.ui.widget

import android.animation.ObjectAnimator
import android.animation.RectEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.util.ViewUtil

class BigBangHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener {

    private lateinit var mSearch: ImageView
    private lateinit var mShare: ImageView
    private lateinit var mCopy: ImageView
    private lateinit var mTrans: ImageView
    private lateinit var mClose: ImageView

    private lateinit var mBorder: Drawable
    private val mActionGap: Int
    private val mContentPadding: Int
    private var mActionListener: ActionListener? = null
    private var dragMode = false
    private var stickHeader = false

    init {
        initSubViews()
        setWillNotDraw(false)
        mActionGap = ViewUtil.dp2px(5f).toInt()
        mContentPadding = ViewUtil.dp2px(10f).toInt()
    }

    private fun initSubViews() {
        val context = context

        mBorder = ContextCompat.getDrawable(context, R.drawable.bigbang_action_bar_bg)!!
        mBorder.callback = this

        mSearch = ImageView(context).apply {
            setImageResource(R.drawable.bigbang_action_search)
            setOnClickListener(this@BigBangHeader)
            contentDescription = context.getString(R.string.app_name)
        }

        mShare = ImageView(context).apply {
            setImageResource(R.drawable.bigbang_action_share)
            setOnClickListener(this@BigBangHeader)
            contentDescription = context.getString(R.string.app_name)
        }

        mCopy = ImageView(context).apply {
            setImageResource(R.drawable.bigbang_action_copy)
            setOnClickListener(this@BigBangHeader)
            contentDescription = context.getString(R.string.app_name)
        }

        mTrans = ImageView(context).apply {
            setImageResource(R.drawable.ic_compare_arrows_white_36dp)
            setOnClickListener(this@BigBangHeader)
            contentDescription = context.getString(R.string.app_name)
        }

        mClose = ImageView(context).apply {
            setImageResource(R.drawable.ic_close_capture)
            setOnClickListener(this@BigBangHeader)
            contentDescription = context.getString(R.string.app_name)
        }

        addView(mSearch, createLayoutParams())
        addView(mShare, createLayoutParams())
        addView(mCopy, createLayoutParams())
        addView(mTrans, createLayoutParams())
        addView(mClose, createLayoutParams())
    }

    private fun createLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val childCount = childCount
        val measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.measure(measureSpec, measureSpec)
        }

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(width, height + mContentPadding + mSearch.measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = measuredWidth
        val height = measuredHeight

        layoutSubView(mSearch, mActionGap, 0)
        layoutSubView(mShare, 2 * mActionGap + mSearch.measuredWidth, 0)
        layoutSubView(mTrans, 3 * mActionGap + mTrans.measuredWidth + mShare.measuredWidth, 0)

        layoutSubView(mCopy, width - mActionGap - mCopy.measuredWidth, 0)
        layoutSubView(
            mClose,
            width - (mActionGap * 2) - mCopy.measuredWidth - mClose.measuredHeight,
            0
        )

        val oldBounds = mBorder.bounds
        val newBounds = Rect(0, mSearch.measuredHeight / 2, width, height)

        if (!stickHeader && oldBounds != newBounds) {
            ObjectAnimator.ofObject(
                BoundWrapper(oldBounds),
                "bound",
                RectEvaluator(),
                oldBounds,
                newBounds
            ).setDuration(200).start()
        }
    }

    private inner class BoundWrapper(initialBound: Rect) {
        var bound: Rect = initialBound
            set(value) {
                field = value
                mBorder.bounds = value
                postInvalidate()
            }
    }

    private fun layoutSubView(view: View, l: Int, t: Int) {
        view.layout(l, t, view.measuredWidth + l, view.measuredHeight + t)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!stickHeader) {
            mBorder.draw(canvas)
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == mBorder
    }

    fun setStickHeader(stickHeader: Boolean) {
        this.stickHeader = stickHeader
    }

    fun getContentPadding(): Int {
        return mContentPadding
    }

    fun setActionListener(actionListener: ActionListener?) {
        mActionListener = actionListener
    }

    override fun onClick(v: View) {
        if (mActionListener == null) {
            return
        }

        when (v) {
            mSearch -> mActionListener?.onSearch()
            mShare -> mActionListener?.onShare()
            mCopy -> mActionListener?.onCopy()
            mTrans -> mActionListener?.onTrans()
            mClose -> mActionListener?.onCancel()
        }
    }

    interface ActionListener {
        fun onSearch()
        fun onShare()
        fun onCopy()
        fun onTrans()
        fun onCancel()
    }
}

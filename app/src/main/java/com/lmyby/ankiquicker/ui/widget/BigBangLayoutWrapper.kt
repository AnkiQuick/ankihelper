package com.lmyby.ankiquicker.ui.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ScrollView
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.util.ViewUtil

class BigBangLayoutWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var mBigBangLayout: BigBangLayout
    private lateinit var mBottom: BigBangBottom
    private lateinit var mHeader: BigBangHeader
    private lateinit var mScrollView: ScrollView

    private var fullScreenMode = false
    private var stickHeader = false
    private var mActionListener: ActionListener? = null

    init {
        init()
    }

    fun setBackgroundColorWithAlpha(color: Int, alpha: Int) {
        val adjustedAlpha = ((alpha / 100.0f) * 255).toInt()
        setBackgroundColor(
            Color.argb(
                adjustedAlpha,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        )
    }

    fun addTextItem(text: String) {
        mBigBangLayout.addTextItem(text)
    }

    fun reset() {
        mBigBangLayout.reset()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.bigbang_layout, this)

        mBigBangLayout = findViewById(R.id.bigbang)
        mBottom = findViewById(R.id.bottom)
        mBottom.visibility = GONE
        mHeader = findViewById(R.id.header)
        mHeader.visibility = GONE
        mScrollView = findViewById(R.id.bigbang_scroll)

        mBigBangLayout.setActionListener(object : BigBangLayout.ActionListener {
            override fun onSelected(text: String) {
                mActionListener?.onSelected(text)
            }

            override fun onSearch(text: String) {
                mActionListener?.onSearch(text)
            }

            override fun onShare(text: String) {
                mActionListener?.onShare(text)
            }

            override fun onCopy(text: String) {
                mActionListener?.onCopy(text)
            }

            override fun onTrans(text: String) {
                mActionListener?.onTrans(text)
            }

            override fun onDrag() {
                mActionListener?.onDrag()
            }

            override fun onDragSelectEnd() {
                mBottom.onDragSelectEnd()
            }

            override fun onCancel() {
                mActionListener?.onCancel()
            }
        })

        mHeader.setActionListener(object : BigBangHeader.ActionListener {
            override fun onSearch() {
                mBigBangLayout.onSearch()
            }

            override fun onShare() {
                mBigBangLayout.onShare()
            }

            override fun onCopy() {
                mBigBangLayout.onCopy()
            }

            override fun onTrans() {
                mBigBangLayout.onTrans()
            }

            override fun onCancel() {
                mBigBangLayout.onCancel()
            }
        })

        mBottom.setActionListener(object : BigBangBottom.ActionListener {
            override fun onDrag() {
                mBigBangLayout.onDrag()
            }

            override fun onDragSelect(isDragSelect: Boolean) {
                mBigBangLayout.onDragSelect(isDragSelect)
                mActionListener?.onDragSelection()
            }

            override fun onSwitchType(isLocal: Boolean) {
                mActionListener?.onSwitchType(isLocal)
            }

            override fun onSelectOther() {
                mBigBangLayout.onSelectOther()
            }

            override fun onSwitchSymbol(isShow: Boolean) {
                mBigBangLayout.setShowSymbol(isShow)
                mActionListener?.onSwitchSymbol(isShow)
            }

            override fun onSwitchSection(isShow: Boolean) {
                mBigBangLayout.setShowSection(isShow)
                mActionListener?.onSwitchSection(isShow)
            }
        })
    }

    fun onSwitchType(isLocal: Boolean) {
        mBottom.setIsLocal(isLocal)
    }

    fun setShowSymbol(showSymbol: Boolean) {
        mBottom.setShowSymbol(showSymbol)
    }

    fun setShowSection(showSection: Boolean) {
        mBottom.setShowSection(showSection)
    }

    fun setFullScreenMode(fullScreenMode: Boolean) {
        this.fullScreenMode = fullScreenMode
    }

    fun setStickHeader(stickHeader: Boolean) {
        this.stickHeader = stickHeader
        mBigBangLayout.setStickHeader(stickHeader)
        mHeader.setStickHeader(stickHeader)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeader.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))

        val childHeight = (mBottom.measuredHeight + mBigBangLayout.measuredHeight).toInt()

        if (fullScreenMode) {
            setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(rootView.height, MeasureSpec.AT_MOST)
            )
        } else {
            if (height > 0) {
                setMeasuredDimension(measuredWidth, Math.min(childHeight, height))
            } else {
                setMeasuredDimension(measuredWidth, Math.max(childHeight, height))
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var topPadding = 0
        var adjustedTop = top

        if (stickHeader) {
            topPadding += (mHeader.measuredHeight * 1.0 / 3).toInt()
            if (fullScreenMode) {
                topPadding += (mHeader.measuredHeight * 2.0 / 3).toInt() +
                        ViewUtil.getNavigationBarHeight(context as Activity)
            }
        } else {
            if (fullScreenMode) {
                topPadding += (mHeader.measuredHeight * 2.0 / 3).toInt() +
                        ViewUtil.getNavigationBarHeight(context as Activity)
                adjustedTop = top + topPadding
            }
        }

        if (fullScreenMode) {
            if (mBigBangLayout.measuredHeight < bottom - adjustedTop - mBottom.measuredHeight * 3.0 / 3) {
                // Display in center
                val layoutBottom = ((bottom - mBottom.measuredHeight * 3.0 / 3 + adjustedTop) / 2 +
                        mBigBangLayout.measuredHeight / 2).toInt()
                val layoutTop = ((bottom - mBottom.measuredHeight * 3.0 / 3 + adjustedTop) / 2 -
                        mBigBangLayout.measuredHeight / 2).toInt()
                mScrollView.layout(left, layoutTop, right, layoutBottom)
            } else {
                mScrollView.layout(
                    left,
                    adjustedTop,
                    right,
                    (bottom - mBottom.measuredHeight * 3.0 / 3).toInt()
                )
            }
            mBottom.layout(
                left,
                (bottom - mBottom.measuredHeight * 3.0 / 3).toInt(),
                right,
                (bottom - mBottom.measuredHeight * 0.0 / 3).toInt()
            )
        } else {
            mScrollView.layout(left, adjustedTop, right, bottom - mBottom.measuredHeight)
            mBottom.layout(left, bottom - mBottom.measuredHeight, right, bottom)
        }
    }

    fun setBottomVibility(visibility: Int) {
        mBottom.visibility = visibility
    }

    fun setActionListener(actionListener: ActionListener?) {
        mActionListener = actionListener
    }

    interface ActionListener {
        fun onSelected(text: String)
        fun onSearch(text: String)
        fun onShare(text: String)
        fun onCopy(text: String)
        fun onTrans(text: String)
        fun onDrag()
        fun onSwitchType(isLocal: Boolean)
        fun onSwitchSymbol(isShow: Boolean)
        fun onSwitchSection(isShow: Boolean)
        fun onDragSelection()
        fun onCancel()
    }
}

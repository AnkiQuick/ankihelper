package com.lmyby.ankiquicker.ui.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.util.ViewUtil

class BigBangBottom @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener {

    private lateinit var mDragSelect: ImageView
    private lateinit var mDrag: ImageView
    private lateinit var mSelectOther: ImageView
    private lateinit var mType: ImageView
    private lateinit var mSection: ImageView
    private lateinit var mSymbol: ImageView

    private val mActionGap: Int
    private val mContentPadding: Int
    private var mActionListener: ActionListener? = null
    private var dragMode = false
    private var dragSelectionMode = false
    private var isLocal = false
    private var showSymbol = false
    private var showSection = false

    init {
        initSubViews()
        setWillNotDraw(false)
        mActionGap = ViewUtil.dp2px(5f).toInt()
        mContentPadding = ViewUtil.dp2px(10f).toInt()
    }

    private fun initSubViews() {
        val context = context

        mDragSelect = ImageView(context).apply {
            setImageResource(R.drawable.ic_drag_select_36dp_n)
            setOnClickListener(this@BigBangBottom)
            contentDescription = context.getString(R.string.app_name)
        }

        mDrag = ImageView(context).apply {
            setImageResource(R.drawable.ic_sort_white_36dp)
            setOnClickListener(this@BigBangBottom)
            contentDescription = context.getString(R.string.app_name)
        }

        mType = ImageView(context).apply {
            setImageResource(R.drawable.bigbang_action_cloud)
            setOnClickListener(this@BigBangBottom)
            contentDescription = context.getString(R.string.app_name)
        }

        mSelectOther = ImageView(context).apply {
            setImageResource(R.drawable.bigbang_action_select_other)
            setOnClickListener(this@BigBangBottom)
            contentDescription = context.getString(R.string.app_name)
        }

        mSymbol = ImageView(context).apply {
            setImageResource(R.drawable.bigbang_action_symbol)
            setOnClickListener(this@BigBangBottom)
            contentDescription = context.getString(R.string.app_name)
        }

        mSection = ImageView(context).apply {
            setImageResource(R.drawable.bigbang_action_enter)
            setOnClickListener(this@BigBangBottom)
            contentDescription = context.getString(R.string.app_name)
        }

        addView(mDragSelect, createLayoutParams())
        addView(mDrag, createLayoutParams())
        addView(mType, createLayoutParams())
        addView(mSelectOther, createLayoutParams())
        addView(mSection, createLayoutParams())
        addView(mSymbol, createLayoutParams())
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

        setMeasuredDimension(width, mContentPadding * 2 + mDrag.measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = measuredWidth

        layoutSubView(mSymbol, mActionGap, mContentPadding)
        layoutSubView(mSection, mActionGap * 2 + mSymbol.measuredWidth, mContentPadding)
        layoutSubView(mType, mActionGap * 3 + mSymbol.measuredWidth * 2, mContentPadding)

        layoutSubView(
            mSelectOther,
            width - mActionGap * 3 - mSelectOther.measuredWidth - mDragSelect.measuredWidth - mDrag.measuredWidth,
            mContentPadding
        )
        layoutSubView(
            mDrag,
            width - mActionGap * 2 - mDragSelect.measuredWidth - mDrag.measuredWidth,
            mContentPadding
        )
        layoutSubView(
            mDragSelect,
            width - mActionGap - mDragSelect.measuredWidth,
            mContentPadding
        )
    }

    private fun layoutSubView(view: View, l: Int, t: Int) {
        view.layout(l, t, view.measuredWidth + l, view.measuredHeight + t)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    fun getContentPadding(): Int {
        return mContentPadding
    }

    fun setActionListener(actionListener: ActionListener?) {
        mActionListener = actionListener
    }

    fun onDragSelectEnd() {
        mDragSelect.setImageResource(R.drawable.ic_drag_select_36dp_n)
        mDragSelect.contentDescription = context.getString(R.string.app_name)
        mActionListener?.onDragSelect(false)
        mDrag.visibility = VISIBLE
        dragSelectionMode = false
    }

    override fun onClick(v: View) {
        if (mActionListener == null) {
            return
        }

        when (v) {
            mDrag -> {
                dragMode = !dragMode
                if (dragMode) {
                    mDrag.setImageResource(R.drawable.ic_done_white_36dp)
                    mDrag.contentDescription = context.getString(R.string.app_name)
                    mDragSelect.visibility = INVISIBLE
                } else {
                    mDrag.setImageResource(R.drawable.ic_sort_white_36dp)
                    mDrag.contentDescription = context.getString(R.string.app_name)
                    mDragSelect.visibility = VISIBLE
                }
                mActionListener?.onDrag()
            }
            mType -> {
                isLocal = !isLocal
                setIsLocal(isLocal)
            }
            mSelectOther -> {
                mActionListener?.onSelectOther()
            }
            mSection -> {
                showSection = !showSection
                setShowSection(showSection)
            }
            mSymbol -> {
                showSymbol = !showSymbol
                setShowSymbol(showSymbol)
            }
            mDragSelect -> {
                if (dragSelectionMode) {
                    onDragSelectEnd()
                } else {
                    mDragSelect.setImageResource(R.drawable.ic_drag_select_36dp_p)
                    mDrag.visibility = INVISIBLE
                    mDragSelect.contentDescription = context.getString(R.string.app_name)
                    mActionListener?.onDragSelect(true)
                    dragSelectionMode = true
                }
            }
        }
    }

    fun setIsLocal(isLocal: Boolean) {
        this.isLocal = isLocal
        mActionListener?.onSwitchType(isLocal)
        if (isLocal) {
            mType.setImageResource(R.drawable.bigbang_action_local)
            mType.contentDescription = context.getString(R.string.app_name)
        } else {
            mType.setImageResource(R.drawable.bigbang_action_cloud)
            mType.contentDescription = context.getString(R.string.app_name)
        }
    }

    fun setShowSymbol(showSymbol: Boolean) {
        this.showSymbol = showSymbol
        mActionListener?.onSwitchSymbol(showSymbol)
        if (showSymbol) {
            mSymbol.setImageResource(R.drawable.bigbang_action_symbol)
            mSymbol.contentDescription = context.getString(R.string.app_name)
        } else {
            mSymbol.setImageResource(R.drawable.bigbang_action_no_symbol)
            mSymbol.contentDescription = context.getString(R.string.app_name)
        }
    }

    fun setShowSection(showSection: Boolean) {
        this.showSection = showSection
        mActionListener?.onSwitchSection(showSection)
        if (showSection) {
            mSection.setImageResource(R.drawable.bigbang_action_enter)
            mSection.contentDescription = context.getString(R.string.app_name)
        } else {
            mSection.setImageResource(R.drawable.bigbang_action_no_enter)
            mSection.contentDescription = context.getString(R.string.app_name)
        }
    }

    interface ActionListener {
        fun onDrag()
        fun onDragSelect(isDragSelect: Boolean)
        fun onSwitchType(isLocal: Boolean)
        fun onSelectOther()
        fun onSwitchSymbol(isShow: Boolean)
        fun onSwitchSection(isShow: Boolean)
    }
}

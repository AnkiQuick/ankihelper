package com.lmyby.ankiquicker.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ClipData
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.NestedScrollingChild
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.util.Constant
import com.lmyby.ankiquicker.util.ConstantUtil
import com.lmyby.ankiquicker.util.RegexUtil
import com.lmyby.ankiquicker.util.StringUtil
import com.lmyby.ankiquicker.util.ViewUtil
import java.util.Collections

class BigBangLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), BigBangHeader.ActionListener,
    NestedScrollingChild {

    private var mLineSpace: Int = 0
    private var mItemSpace: Int = 0
    private var mTextColorRes: Int = 0
    private var mSectionTextBgRes: Int = 0
    private var mSymbolTextBgRes: Int = 0
    private var mTextSize: Int = 0
    private var mTextPadding: Int = ViewUtil.dp2px(ConstantUtil.DEFAULT_ITEM_PADDING.toFloat()).toInt()
    private var mSymbolTextPadding: Int = ViewUtil.dp2px(0f).toInt()
    private var mTextPaddingPort: Int = ViewUtil.dp2px(5f).toInt()
    private var mTextBgRes: Int = 0

    private var mTargetItem: Item? = null
    private val mLines: MutableList<Line> = ArrayList()
    private lateinit var mSectionIndex: MutableList<Int>
    private var mActionBarTopHeight: Int = 0
    private var mActionBarBottomHeight: Int = 0
    private lateinit var mHeader: BigBangHeader

    private var showAnimation = false
    private lateinit var dragPaint: Paint
    private var dragMode = false
    private var dragModeSelect = false
    private var dragItem: Item? = null

    private var mColorStateList: ColorStateList? = null
    private var stickHeader = false
    private var mOriginActionBarTopHeight: Int = 0
    private var autoAddBlanks = false

    private val mActionBarAnimationListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            if (findFirstSelectedLine() == null) {
                mHeader.visibility = GONE
            } else {
                requestLayout()
            }
        }
    }

    private var mActionListener: ActionListener? = null
    private var mScaledTouchSlop: Int = 0
    private var mDownX: Float = 0f
    private var mDownY: Float = 0f
    private var mDisallowedParentIntercept: Boolean = false

    private var showSymbol = false
    private var showSection = false
    private var showSpace = false
    private var symbolSelectable = false

    private lateinit var mDragSelectRect: Rect
    private lateinit var mDragSelectPaint: Paint
    private var mDragSelectX: Float = 0f
    private var mDragSelectY: Float = 0f
    private lateinit var mDragSelectSet: MutableSet<ItemState>
    private var mDragSelection: Boolean = false
    private var mDragSelectionSetted: Boolean = false

    private var mNeedReDetectInMeasure = true

    private val longPressedHandler = Handler(Looper.getMainLooper())
    private val mLongPressedRunnable = LongPressedRunnable()
    private var mItemState: ItemState? = null

    init {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BigBangLayout)
            mItemSpace = typedArray.getDimensionPixelSize(
                R.styleable.BigBangLayout_itemSpace,
                resources.getDimensionPixelSize(R.dimen.big_bang_default_item_space)
            )
            mLineSpace = typedArray.getDimensionPixelSize(
                R.styleable.BigBangLayout_lineSpace,
                resources.getDimensionPixelSize(R.dimen.big_bang_default_line_space)
            )

            mTextColorRes = typedArray.getResourceId(
                R.styleable.BigBangLayout_textColor,
                DEFAULT_TEXT_COLOR_RES
            )
            mTextSize = ViewUtil.px2sp(
                typedArray.getDimension(
                    R.styleable.BigBangLayout_textSize,
                    ViewUtil.sp2px(DEFAULT_TEXT_SIZE.toFloat())
                )
            ).toInt()
            mTextBgRes = typedArray.getResourceId(
                R.styleable.BigBangLayout_textBackground,
                DEFAULT_TEXT_BG_RES
            )
            mSectionTextBgRes = typedArray.getResourceId(
                R.styleable.BigBangLayout_sectionTextBackground,
                DEFAULT_SECTION_TEXT_BG_RES
            )
            mSymbolTextBgRes = typedArray.getResourceId(
                R.styleable.BigBangLayout_symbolTextBackground,
                DEFAULT_SYMBOL_TEXT_BG_RES
            )
            typedArray.recycle()
            mActionBarBottomHeight = mLineSpace
            mActionBarTopHeight = resources.getDimensionPixelSize(R.dimen.big_bang_action_bar_height)
            mOriginActionBarTopHeight = mActionBarTopHeight
        }

        mHeader = BigBangHeader(context)
        mHeader.visibility = GONE
        mHeader.setActionListener(this)

        dragPaint = Paint().apply {
            isAntiAlias = true
        }

        addView(mHeader, 0)
        clipChildren = false

        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        setOnDragListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            val eventType = event.action

            try {
                if (dragItem == null || dragItem?.getText() != event.clipDescription.label) {
                    return@setOnDragListener false
                }
            } catch (e: Throwable) {
                return@setOnDragListener false
            }

            var item = findItemByPoint(x, y)
            Log.e("findItemIndexByPoint", "item=$item,${item?.index ?: -1}")
            if (item == null) {
                item = findItemIndexByPoint(x, y)
                if (item == null) {
                    if (eventType == DragEvent.ACTION_DRAG_ENDED) {
                        dragItem?.let {
                            mNeedReDetectInMeasure = true
                            removeView(it.view)
                            addView(it.view, it.index)
                            mTargetItem = null
                        }
                    }
                    return@setOnDragListener true
                }
            }
            if (mTargetItem != null && mTargetItem?.view == item.view) {
                return@setOnDragListener true
            } else {
                dragItem?.let {
                    mNeedReDetectInMeasure = true
                    removeView(it.view)
                    addView(it.view, item.index)
                    it.index = item.index
                    mTargetItem = item
                }
            }
            true
        }

        mSectionIndex = ArrayList()
        mDragSelectRect = Rect()
        mDragSelectPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            style = Paint.Style.STROKE
            strokeWidth = ViewUtil.dp2px(2f).toFloat()
            pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
            isAntiAlias = true
        }
        mDragSelectSet = HashSet()

        setWillNotDraw(false)
    }

    fun setStickHeader(stickHeader: Boolean) {
        this.stickHeader = stickHeader
        mHeader.setStickHeader(stickHeader)
        mActionBarTopHeight = if (stickHeader) 0 else mOriginActionBarTopHeight
    }

    fun getLineSpace(): Int {
        return mLineSpace
    }

    fun setLineSpace(lineSpace: Int) {
        this.mLineSpace = lineSpace
        requestLayout()
    }

    fun getItemSpace(): Int {
        return mItemSpace
    }

    fun setItemSpace(itemSpace: Int) {
        this.mItemSpace = itemSpace
        requestLayout()
    }

    fun getTextColorStateList(): ColorStateList? {
        return mColorStateList
    }

    fun setTextColorStateList(colorStateList: ColorStateList?) {
        this.mColorStateList = colorStateList
    }

    fun getTextSize(): Int {
        return mTextSize
    }

    fun setTextSize(textSize: Int) {
        this.mTextSize = textSize
        mNeedReDetectInMeasure = true
        mLines.forEach { line ->
            line.getItems()?.forEach { item ->
                (item.view as TextView).textSize = textSize.toFloat()
                if (!item.isSymbol()) {
                    item.view.setPadding(mTextPadding, mTextPaddingPort, mTextPadding, mTextPaddingPort)
                } else {
                    item.view.setPadding(
                        mSymbolTextPadding,
                        mTextPaddingPort,
                        mSymbolTextPadding,
                        mTextPaddingPort
                    )
                }
            }
        }
    }

    fun setSymbolTextPadding(padding: Int) {
        mSymbolTextPadding = padding
        mNeedReDetectInMeasure = true
        mLines.forEach { line ->
            line.getItems()?.forEach { item ->
                if (item.isSymbol()) {
                    item.view.setPadding(
                        mSymbolTextPadding,
                        mTextPaddingPort,
                        mSymbolTextPadding,
                        mTextPaddingPort
                    )
                }
            }
        }
    }

    fun setTextPadding(padding: Int) {
        mTextPadding = padding
        mNeedReDetectInMeasure = true
        mLines.forEach { line ->
            line.getItems()?.forEach { item ->
                if (!item.isSymbol()) {
                    item.view.setPadding(mTextPadding, mTextPaddingPort, mTextPadding, mTextPaddingPort)
                }
            }
        }
    }

    fun setTextPaddingPort(padding: Int) {
        mTextPaddingPort = padding
        mNeedReDetectInMeasure = true
        mLines.forEach { line ->
            line.getItems()?.forEach { item ->
                if (!item.isSymbol()) {
                    item.view.setPadding(mTextPadding, mTextPaddingPort, mTextPadding, mTextPaddingPort)
                }
            }
        }
    }

    fun getTextBgRes(): Int {
        return mTextBgRes
    }

    fun setTextBgRes(textBgRes: Int) {
        this.mTextBgRes = textBgRes
    }

    fun addTextItem(text: String) {
        mNeedReDetectInMeasure = true
        if (TextUtils.isEmpty(text) || text.contains(TAB)) {
            return
        }

        val view = TextView(context).apply {
            this.text = text
            setBackgroundResource(mTextBgRes)
            if (mColorStateList == null) {
                setTextColor(ContextCompat.getColorStateList(context, mTextColorRes))
            } else {
                setTextColor(mColorStateList)
            }
            textSize = mTextSize.toFloat()
            if (RegexUtil.isSymbol(text)) {
                setPadding(mSymbolTextPadding, mTextPaddingPort, mSymbolTextPadding, mTextPaddingPort)
            } else {
                setPadding(mTextPadding, mTextPaddingPort, mTextPadding, mTextPaddingPort)
            }
            gravity = Gravity.CENTER
        }
        addView(view)
    }

    fun reset() {
        mNeedReDetectInMeasure = true
        showAnimation = false
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            if (mHeader == child) {
                mHeader.visibility = GONE
                continue
            }
            removeView(child)
        }
    }

    fun isShowSymbol(): Boolean {
        return showSymbol
    }

    fun setShowSymbol(showSymbol: Boolean) {
        this.showSymbol = showSymbol
        mNeedReDetectInMeasure = true
        requestLayout()
    }

    fun isShowSpace(): Boolean {
        return showSpace
    }

    fun setShowSpace(showSpace: Boolean) {
        this.showSpace = showSpace
        mNeedReDetectInMeasure = true
        requestLayout()
    }

    fun isSymbolSelectable(): Boolean {
        return symbolSelectable
    }

    fun setSymbolSelectable(symbolSelectable: Boolean) {
        this.symbolSelectable = symbolSelectable
    }

    fun isShowSection(): Boolean {
        return showSection
    }

    fun setShowSection(showSection: Boolean) {
        this.showSection = showSection
        mNeedReDetectInMeasure = true
        requestLayout()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (dragModeSelect) {
            canvas.drawRect(mDragSelectRect, mDragSelectPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        if (mNeedReDetectInMeasure) {
            val contentWidthSize = widthSize - mHeader.getContentPadding()
            val childCount = childCount
            val measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

            mLines.clear()
            mSectionIndex.clear()
            var currentLine: Line? = null
            var currentLineWidth = contentWidthSize
            var isEnter = true
            var isBold = false

            for (i in 0 until childCount) {
                val v = getChildAt(i)
                if (mHeader == v) {
                    continue
                }
                val child = v as View
                val content = (child as TextView).text.toString()

                child.visibility = VISIBLE

                if (content == Constant.LEFT_BOLD_SUBSTITUDE) {
                    isBold = true
                    continue
                }
                if (content == Constant.RIGHT_BOLD_SUBSTITUDE) {
                    isBold = false
                    continue
                }

                if (!showSymbol && RegexUtil.isSymbol(content)) {
                    child.visibility = GONE
                    continue
                }

                if (!showSpace && StringUtil.isSpace(content)) {
                    child.visibility = GONE
                    continue
                }

                if (content.contains(ENTER) || content == ENTER_SYMBOL) {
                    child.visibility = GONE
                    val item = Item(currentLine!!)
                    item.view = child
                    currentLine.addItem(item)
                    mSectionIndex.add(i)
                    isEnter = true
                    continue
                }

                child.measure(measureSpec, measureSpec)

                if (currentLineWidth > 0) {
                    currentLineWidth += mItemSpace
                }
                currentLineWidth += child.measuredWidth

                if (mLines.isEmpty() || currentLineWidth > contentWidthSize || (isEnter && showSection)) {
                    currentLineWidth = child.measuredWidth
                    currentLine = Line(mLines.size)
                    mLines.add(currentLine)
                }

                val item = Item(currentLine!!)
                item.view = child
                item.index = i
                item.width = child.measuredWidth
                item.height = child.measuredHeight

                if (currentLine.getItems() == null && (isEnter && showSection)) {
                    val padding = child.paddingLeft
                    child.setBackgroundResource(mTextBgRes)
                    child.setPadding(padding, mTextPaddingPort, padding, mTextPaddingPort)
                    if (isBold) {
                        item.setSelected(true)
                    }
                } else if (item.isSymbol()) {
                    child.setBackgroundResource(mSymbolTextBgRes)
                    child.setPadding(
                        mSymbolTextPadding,
                        mTextPaddingPort,
                        mSymbolTextPadding,
                        mTextPaddingPort
                    )
                } else {
                    val padding = child.paddingLeft
                    child.setBackgroundResource(mTextBgRes)
                    child.setPadding(padding, mTextPaddingPort, padding, mTextPaddingPort)
                    if (isBold) {
                        item.setSelected(true)
                    }
                }
                currentLine.addItem(item)
                isEnter = false
            }
            mNeedReDetectInMeasure = false
        }

        val firstSelectedLine = findFirstSelectedLine()
        val lastSelectedLine = findLastSelectedLine()
        if (firstSelectedLine != null && lastSelectedLine != null) {
            val selectedLineHeight = (lastSelectedLine.maxIndex - firstSelectedLine.maxIndex + 1) *
                    (firstSelectedLine.getHeight() + mLineSpace)
            mHeader.measure(
                MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(selectedLineHeight, MeasureSpec.UNSPECIFIED)
            )
        }

        val size = (if (mLines.isNotEmpty()) mLines.size * mLines[0].getHeight() else 0) +
                paddingTop + paddingBottom +
                mLines.size * mLineSpace +
                mActionBarTopHeight + mActionBarBottomHeight

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val lastSelectedLine = findLastSelectedLine()
        val firstSelectedLine = findFirstSelectedLine()

        for (i in mLines.indices) {
            val line = mLines[i]
            val items = line.getItems()
            var left = paddingLeft + mHeader.getContentPadding()

            val offsetTop = when {
                firstSelectedLine != null && firstSelectedLine.maxIndex > line.maxIndex -> -mActionBarTopHeight
                lastSelectedLine != null && lastSelectedLine.maxIndex < line.maxIndex -> mActionBarBottomHeight
                else -> 0
            }

            items?.forEach { item ->
                val top = paddingTop + i * (item.height + mLineSpace) + offsetTop + mActionBarTopHeight
                val child = item.view
                val oldTop = child.top
                child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
                if (showAnimation && oldTop != top) {
                    val translationY = oldTop - top
                    child.translationY = translationY.toFloat()
                    child.animate().translationYBy(-translationY.toFloat()).setDuration(200).start()
                }
                left += child.measuredWidth + mItemSpace
            }
        }

        if (!stickHeader) {
            if (firstSelectedLine != null && lastSelectedLine != null) {
                mHeader.visibility = VISIBLE
                mHeader.alpha = 1f
                val oldTop = mHeader.top
                val actionBarTop =
                    firstSelectedLine.maxIndex * (firstSelectedLine.getHeight() + mLineSpace) + paddingTop
                mHeader.layout(
                    paddingLeft,
                    actionBarTop,
                    paddingLeft + mHeader.measuredWidth,
                    actionBarTop + mHeader.measuredHeight
                )
                if (oldTop != actionBarTop) {
                    val translationY = oldTop - actionBarTop
                    mHeader.translationY = translationY.toFloat()
                    mHeader.animate().translationYBy(-translationY.toFloat()).setDuration(200).start()
                }
            } else {
                if (mHeader.visibility == VISIBLE && !dragMode) {
                    mHeader.animate().alpha(0f).setDuration(200)
                        .setListener(mActionBarAnimationListener).start()
                }
            }
        } else {
            mHeader.visibility = GONE
        }
    }

    private fun findLastSelectedLine(): Line? {
        var result: Line? = null
        for (line in mLines) {
            if (line.hasSelected()) {
                result = line
            }
        }
        return result
    }

    private fun findFirstSelectedLine(): Line? {
        for (line in mLines) {
            if (line.hasSelected()) {
                return line
            }
        }
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.e("onTouchEvent", "onTouchEvent:$event")
        val actionMasked = event.actionMasked
        if (dragModeSelect) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            when (actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    showAnimation = true
                    mDownX = x.toFloat()
                    mDownY = y.toFloat()
                    mDisallowedParentIntercept = false
                    mDragSelectSet.clear()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!mDisallowedParentIntercept) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        mDisallowedParentIntercept = true
                    }
                    mDragSelectX = x.toFloat().coerceIn(0f, width.toFloat())
                    mDragSelectY = y.toFloat().coerceIn(0f, height.toFloat())
                    mDragSelectRect.set(
                        Math.min(mDownX, mDragSelectX).toInt(),
                        Math.min(mDownY, mDragSelectY).toInt(),
                        Math.max(mDownX, mDragSelectX).toInt(),
                        Math.max(mDownY, mDragSelectY).toInt()
                    )
                    setSelectionByRect(mDragSelectRect)
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    mDragSelectionSetted = false
                    dragModeSelect = false
                    mItemState = null
                    mActionListener?.onDragSelectEnd()
                    requestLayout()
                    invalidate()
                    if (mDisallowedParentIntercept) {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
            return true
        }

        if (dragMode) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            when (actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    showAnimation = true
                    mDownX = x.toFloat()
                    mDownY = y.toFloat()
                    mDisallowedParentIntercept = false
                    val item = findItemByPoint(x, y)
                    if (item != null) {
                        dragItem = Item(item)
                        val clipData = ClipData.newPlainText(item.getText(), item.getText())
                        val myShadow = DragShadowBuilder(dragItem?.view)
                        // Use startDragAndDrop for API 24+
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            dragItem?.view?.startDragAndDrop(clipData, myShadow, null, 0)
                        } else {
                            @Suppress("DEPRECATION")
                            dragItem?.view?.startDrag(clipData, myShadow, null, 0)
                        }
                        mNeedReDetectInMeasure = true
                        dragItem?.view?.let { removeView(it) }
                    } else {
                        dragItem = null
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!mDisallowedParentIntercept && Math.abs(x - mDownX) > mScaledTouchSlop) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        mDisallowedParentIntercept = true
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (mTargetItem == null && dragItem != null) {
                        mNeedReDetectInMeasure = true
                        dragItem?.let {
                            removeView(it.view)
                            addView(it.view, it.index)
                        }
                    }
                    mTargetItem = null
                    requestLayout()
                    invalidate()
                    if (mDisallowedParentIntercept) {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
        } else {
            val x = event.x.toInt()
            val y = event.y.toInt()
            when (actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    longPressedHandler.removeCallbacks(mLongPressedRunnable)
                    mLongPressedRunnable.setPosition(x, y)
                    longPressedHandler.postDelayed(mLongPressedRunnable, 500)
                    showAnimation = true
                    mDownX = x.toFloat()
                    mDownY = y.toFloat()
                    mDisallowedParentIntercept = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (Math.abs(y - mDownY) > mScaledTouchSlop || Math.abs(x - mDownX) > mScaledTouchSlop) {
                        longPressedHandler.removeCallbacks(mLongPressedRunnable)
                    }
                    if (!mDisallowedParentIntercept && Math.abs(x - mDownX) > mScaledTouchSlop) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        mDisallowedParentIntercept = true
                    }
                    val item = findItemByPoint(x, y)
                    if (mTargetItem != item) {
                        mTargetItem = item
                        if (item != null) {
                            item.setSelected(!item.isSelected())
                            val state = ItemState(item, item.isSelected())
                            if (mItemState == null) {
                                mItemState = state
                            } else {
                                state.next = mItemState
                                mItemState = state
                            }
                        }
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    // Handled below
                }
                MotionEvent.ACTION_UP -> {
                    longPressedHandler.removeCallbacks(mLongPressedRunnable)
                    requestLayout()
                    invalidate()
                    mTargetItem = null
                    if (mDisallowedParentIntercept) {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                    mItemState = null
                    val selected = makeSelectedText()
                    if (!TextUtils.isEmpty(selected)) {
                        mActionListener?.onSelected(selected)
                    }
                }
            }
        }
        return true
    }

    private inner class LongPressedRunnable : Runnable {
        var x: Int = 0
        var y: Int = 0

        fun setPosition(x: Int, y: Int) {
            this.x = x
            this.y = y
        }

        override fun run() {
            val item = findItemByPoint(x, y)
            if (item != null) {
                for (line in getLines()) {
                    line.getItems()?.forEach { it.setSelected(false) }
                }
                item.view.isSelected = true
            }
        }
    }

    fun setBackgroundColorAlpha(value: Int) {
        val adjustedValue = ((value / 100.0f) * 255).toInt()
        setBackgroundColor(Color.argb(adjustedValue, 0, 0, 0))
    }

    inner class ItemState(val item: Item, val isSelected: Boolean) {
        var next: ItemState? = null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ItemState) return false
            return item == other.item
        }

        override fun hashCode(): Int {
            return item.hashCode()
        }
    }

    private fun findItemByPoint(x: Int, y: Int): Item? {
        for (line in mLines) {
            line.getItems()?.forEach { item ->
                if (item.getRect().contains(x, y)) {
                    return item
                }
            }
        }
        return null
    }

    private fun setSelectionByRect(rect: Rect) {
        for (line in mLines) {
            line.getItems()?.forEach { item ->
                if (item.getRect().intersect(rect)) {
                    if (!mDragSelectionSetted) {
                        mDragSelectionSetted = true
                        mDragSelection = !item.view.isSelected
                    }
                    val state = ItemState(item, item.view.isSelected)
                    if (!mDragSelectSet.contains(state)) {
                        mDragSelectSet.add(state)
                    }
                    item.view.isSelected = mDragSelection
                }
            }
        }
        for (item in mDragSelectSet) {
            if (!item.item.getRect().intersect(rect)) {
                item.item.view.isSelected = item.isSelected
            }
        }
    }

    private fun findItemIndexByPoint(x: Int, y: Int): Item? {
        val length = mLines.size
        if (mLines.isEmpty()) return null

        if (y > mLines[0].getHeight() / 2 + mActionBarTopHeight &&
            y < height - mLines[0].getHeight() - mLineSpace
        ) {
            return null
        }

        val lineNum = when {
            mLines[0].hasSelected() && y <= mActionBarTopHeight -> 0
            !mLines[0].hasSelected() && y <= mLines[0].getHeight() / 2 + mActionBarTopHeight -> 0
            y >= height - mLines[0].getHeight() - mLineSpace -> mLines.size - 1
            else -> return null
        }

        val items = mLines[lineNum].getItems() ?: return null
        var height = 0

        for (i in items.indices) {
            if (height <= x - items[i].view.measuredWidth / 2 + mItemSpace / 2 &&
                height >= x - items[i].view.measuredWidth + mItemSpace / 2
            ) {
                return items[i]
            }
            height += items[i].view.measuredWidth + mItemSpace
        }

        if (height <= x - items[items.size - 1].view.measuredWidth / 2) {
            return items[items.size - 1]
        }
        return null
    }

    private fun findChildByPoint(x: Int, y: Int): View? {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val rect = Rect()
            child.getHitRect(rect)
            if (rect.contains(x, y)) {
                return child
            }
        }
        return null
    }

    private fun makeSelectedText(): String {
        val builder = StringBuilder()
        val length = mLines.size
        for (i in 0 until length) {
            val line = mLines[i]
            var containEnter = false
            if (i < length - 1) {
                val nextLine = mLines[i + 1]
                val items = line.getItems()
                val nextItems = nextLine.getItems()
                if (items != null && nextItems != null && items.isNotEmpty() && nextItems.isNotEmpty()) {
                    val thisLineLastIndex = items[items.size - 1].index
                    val nextLineFirstIndex = nextItems[nextItems.size - 1].index
                    for (j in thisLineLastIndex until nextLineFirstIndex) {
                        if (mSectionIndex.contains(j)) {
                            containEnter = true
                        }
                    }
                }
            }
            builder.append(line.getSelectedText())
            if (containEnter && showSection) {
                builder.append("\n")
            }
        }
        return builder.toString().replace(Regex("[\\n]+"), "\n").trim()
    }

    override fun onSearch() {
        mActionListener?.onSearch(makeSelectedText())
    }

    override fun onShare() {
        mActionListener?.onShare(makeSelectedText())
    }

    override fun onCopy() {
        mActionListener?.onCopy(makeSelectedText())
    }

    fun onDrag() {
        dragMode = !dragMode
        mActionListener?.onDrag()
    }

    fun onDragSelect(isDragSelect: Boolean) {
        dragModeSelect = isDragSelect
    }

    override fun onTrans() {
        mActionListener?.onTrans(makeSelectedText())
    }

    override fun onCancel() {
        for (line in mLines) {
            line.getItems()?.forEach { it.setSelected(false) }
        }
        requestLayout()
    }

    fun onSelectOther() {
        for (line in mLines) {
            line.getItems()?.forEach { it.triggerSelected() }
        }
        requestLayout()
    }

    fun setActionListener(actionListener: ActionListener?) {
        mActionListener = actionListener
    }

    fun getLines(): List<Line> {
        return Collections.unmodifiableList(mLines)
    }

    inner class Line(val maxIndex: Int) {
        private var items: MutableList<Item>? = null

        fun addItem(item: Item) {
            if (items == null) {
                items = ArrayList()
            }
            items?.add(item)
        }

        fun getItems(): List<Item>? {
            return items
        }

        fun hasSelected(): Boolean {
            return items?.any { it.isSelected() } ?: false
        }

        fun getHeight(): Int {
            return items?.firstOrNull()?.view?.measuredHeight ?: 0
        }

        fun getSelectedText(): String {
            val builder = StringBuilder()
            items?.forEach { item ->
                if (item.isSelected()) {
                    val txt = item.getText().toString()
                    builder.append(txt)
                    if (autoAddBlanks && txt.matches(Regex("[a-zA-Z0-9]*"))) {
                        builder.append(" ")
                    }
                }
            }
            return builder.toString().replace("  ", " ")
        }
    }

    inner class Item {
        var line: Line
        var index: Int = 0
        var height: Int = 0
        var width: Int = 0
        lateinit var view: View
        private val rect = Rect()

        constructor(line: Line) {
            this.line = line
        }

        constructor(item: Item) {
            this.line = item.line
            this.index = item.index
            this.height = item.height
            this.width = item.width
            this.view = item.view
        }

        fun getRect(): Rect {
            view.getHitRect(rect)
            return rect
        }

        fun isSymbol(): Boolean {
            return RegexUtil.isSymbol((view as TextView).text.toString()) ||
                    StringUtil.isSpace((view as TextView).text.toString())
        }

        fun isSelected(): Boolean {
            return view.isSelected
        }

        fun setSelected(selected: Boolean) {
            if (symbolSelectable || !isSymbol()) {
                view.isSelected = selected
            }
        }

        fun triggerSelected() {
            view.isSelected = !view.isSelected
        }

        fun getText(): CharSequence {
            return (view as TextView).text.toString()
        }

        fun longPressed() {
            if (!(view as TextView).text.toString().matches(Regex("[a-zA-Z]*"))) {
                if (isSymbol()) {
                    return
                }
            }
        }
    }

    interface ActionListener {
        fun onSelected(text: String)
        fun onSearch(text: String)
        fun onShare(text: String)
        fun onCopy(text: String)
        fun onTrans(text: String)
        fun onDrag()
        fun onDragSelectEnd()
        fun onCancel()
    }

    companion object {
        const val ENTER = "_Enter_"
        const val ENTER_SYMBOL = "\n"
        const val TAB = "_Tab_"

        private const val DEFAULT_TEXT_SIZE = 14
        private val DEFAULT_TEXT_COLOR_RES = R.color.bigbang_item_text
        private val DEFAULT_TEXT_BG_RES = R.drawable.item_background
        private val DEFAULT_SECTION_TEXT_BG_RES = R.drawable.item_background_section
        private val DEFAULT_SYMBOL_TEXT_BG_RES = R.drawable.item_background_symbol
    }
}

package com.lmyby.ankiquicker.ui.plan.helper

/**
 * Interface for drag-and-drop item touch helper
 * Converted to Kotlin as part of Phase 4 adapter migration
 */
interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onMoveFinished()
}

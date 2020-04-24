package dev.lucasnlm.antimine.common.level.widget

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

// Based on `FixedGridLayoutManager` from https://github.com/devunwired/recyclerview-playground

class FixedGridLayoutManager(
    private var maxColumnCount: Int,
    private val horizontalPadding: Int,
    private val verticalPadding: Int
) : RecyclerView.LayoutManager() {

    private var firstVisiblePosition = 0

    private var decoratedChildWidth = 0
    private var decoratedChildHeight = 0

    private var visibleColumnCount = 0
    private var visibleRowCount = 0

    private var firstChangedPosition = 0
    private var changedPositionCount = 0

    override fun supportsPredictiveItemAnimations(): Boolean = false

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        firstChangedPosition = positionStart
        changedPositionCount = itemCount
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (childCount == 0 && state.isPreLayout) {
            return
        }

        if (!state.isPreLayout) {
            changedPositionCount = 0
            firstChangedPosition = 0
        }

        if (childCount == 0) {
            recycler.getViewForPosition(0).run {
                addView(this)
                measureChildWithMargins(this, 0, 0)

                decoratedChildWidth = getDecoratedMeasuredWidth(this)
                decoratedChildHeight = getDecoratedMeasuredHeight(this)
                detachAndScrapView(this, recycler)
            }
        }

        updateWindowSizing()
        var removedCache: SparseIntArray? = null

        if (state.isPreLayout) {
            removedCache = SparseIntArray(childCount)

            (0 until childCount)
                .mapNotNull { getChildAt(it) }
                .map { it.layoutParams as LayoutParams }
                .filter { it.isItemRemoved }
                .forEach { removedCache.put(it.viewLayoutPosition, REMOVE_VISIBLE) }

            if (removedCache.size() == 0 && changedPositionCount > 0) {
                (firstChangedPosition until firstChangedPosition + changedPositionCount)
                    .forEach { index ->
                        removedCache.put(index, REMOVE_INVISIBLE)
                    }
            }
        }

        var childLeft: Int? = null
        var childTop: Int? = null

        if (childCount == 0) {
            firstVisiblePosition = 0
            childLeft = horizontalPadding
            childTop = verticalPadding
        } else if (!state.isPreLayout && visibleChildCount >= state.itemCount) {
            // Data set is too small to scroll fully, just reset position
            firstVisiblePosition = 0
            childLeft = horizontalPadding
            childTop = verticalPadding
        } else {
            getChildAt(0)?.let { topChild ->
                childLeft = getDecoratedLeft(topChild)
                childTop = getDecoratedTop(topChild)
            }

            if (!state.isPreLayout && verticalSpace > totalRowCount * decoratedChildHeight) {
                firstVisiblePosition %= totalColumnCount
                childTop = verticalPadding

                if (firstVisiblePosition + visibleColumnCount > state.itemCount) {
                    firstVisiblePosition = (state.itemCount - visibleColumnCount).coerceAtLeast(0)
                    childLeft = horizontalPadding
                }
            }

            val maxFirstRow = totalRowCount - (visibleRowCount - 1)
            val maxFirstCol = totalColumnCount - (visibleColumnCount - 1)
            val isOutOfRowBounds = firstVisibleRow > maxFirstRow
            val isOutOfColBounds = firstVisibleColumn > maxFirstCol

            if (isOutOfRowBounds || isOutOfColBounds) {
                val firstRow: Int = if (isOutOfRowBounds) {
                    maxFirstRow
                } else {
                    firstVisibleRow
                }
                val firstCol: Int = if (isOutOfColBounds) {
                    maxFirstCol
                } else {
                    firstVisibleColumn
                }

                firstVisiblePosition = firstRow * totalColumnCount + firstCol
                childLeft = horizontalSpace - decoratedChildWidth * visibleColumnCount
                childTop = verticalSpace - decoratedChildHeight * visibleRowCount

                if (firstVisibleRow == 0) {
                    childTop = childTop?.coerceAtMost(verticalPadding) ?: verticalSpace
                }
                if (firstVisibleColumn == 0) {
                    childLeft = childLeft?.coerceAtMost(horizontalPadding) ?: horizontalSpace
                }
            }
        }

        // Clear all attached views into the recycle bin
        detachAndScrapAttachedViews(recycler)

        // Fill the grid for the initial layout of views
        val finalChildLeft = childLeft
        val finalChildTop = childTop
        if (finalChildLeft != null && finalChildTop != null) {
            fillGrid(DIRECTION_NONE, finalChildLeft, finalChildTop, recycler, state, removedCache)
        }

        // Evaluate any disappearing views that may exist
        if (!state.isPreLayout && recycler.scrapList.isNotEmpty()) {
            recycler.scrapList
                .map { it.itemView }
                .filterNot { (it.layoutParams as LayoutParams).isItemRemoved }
                .forEach { layoutDisappearingView(it) }
        }
    }

    override fun onAdapterChanged(oldAdapter: RecyclerView.Adapter<*>?, newAdapter: RecyclerView.Adapter<*>?) {
        // Completely scrap the existing layout
        removeAllViews()
    }

    private fun updateWindowSizing() {
        visibleColumnCount = horizontalSpace / decoratedChildWidth + 1
        if (horizontalSpace % decoratedChildWidth > 0) {
            visibleColumnCount++
        }

        // Allow minimum value for small data sets
        visibleColumnCount = visibleColumnCount.coerceAtMost(totalColumnCount)

        visibleRowCount = verticalSpace / decoratedChildHeight + 1
        if (verticalSpace % decoratedChildHeight > 0) {
            visibleRowCount++
        }

        visibleRowCount.coerceAtMost(totalRowCount)
    }

    private fun fillGrid(direction: Int, recycler: Recycler, state: RecyclerView.State) {
        fillGrid(direction, 0, 0, recycler, state, null)
    }

    private fun fillGrid(
        direction: Int,
        emptyLeft: Int,
        emptyTop: Int,
        recycler: Recycler,
        state: RecyclerView.State,
        removedPositions: SparseIntArray?
    ) {
        firstVisiblePosition = firstVisiblePosition.coerceIn(0, itemCount - 1)

        var startLeftOffset = emptyLeft
        var startTopOffset = emptyTop
        val viewCache = SparseArray<View?>(childCount)

        if (childCount != 0) {
            getChildAt(0)?.let { topView ->
                startLeftOffset = getDecoratedLeft(topView)
                startTopOffset = getDecoratedTop(topView)
            }

            when (direction) {
                DIRECTION_START -> startLeftOffset -= decoratedChildWidth
                DIRECTION_END -> startLeftOffset += decoratedChildWidth
                DIRECTION_UP -> startTopOffset -= decoratedChildHeight
                DIRECTION_DOWN -> startTopOffset += decoratedChildHeight
            }

            // Cache all views by their existing position, before updating counts
            (0 until childCount).forEach { index ->
                val position = positionOfIndex(index)
                val child = getChildAt(index)
                viewCache.put(position, child)
            }

            // Temporarily detach all views.
            // Views we still need will be added back at the proper index.
            (0 until viewCache.size())
                .mapNotNull { index -> viewCache.valueAt(index) }
                .forEach(::detachView)
        }

        when (direction) {
            DIRECTION_START -> firstVisiblePosition--
            DIRECTION_END -> firstVisiblePosition++
            DIRECTION_UP -> firstVisiblePosition -= totalColumnCount
            DIRECTION_DOWN -> firstVisiblePosition += totalColumnCount
        }

        /*
         * Next, we supply the grid of items that are deemed visible.
         * If these items were previously there, they will simply be
         * re-attached. New views that must be created are obtained
         * from the Recycler and added.
         */
        var leftOffset = startLeftOffset
        var topOffset = startTopOffset
        for (index in 0 until visibleChildCount) {
            var nextPosition = positionOfIndex(index)

            var offsetPositionDelta = 0
            if (state.isPreLayout) {
                var offsetPosition = nextPosition

                removedPositions?.let {
                    for (offset in 0 until it.size()) {
                        // Look for off-screen removals that are less-than this
                        if (removedPositions.valueAt(offset) ==
                            REMOVE_INVISIBLE && removedPositions.keyAt(offset) < nextPosition) {
                            // Offset position to match
                            offsetPosition--
                        }
                    }
                }

                offsetPositionDelta = nextPosition - offsetPosition
                nextPosition = offsetPosition
            }

            if (nextPosition < 0 || nextPosition >= state.itemCount) {
                // Item space beyond the data set, don't attempt to add a view
                continue
            }

            // Layout this position
            var view = viewCache[nextPosition]
            if (view == null) {
                view = recycler.getViewForPosition(nextPosition)
                addView(view)

                if (!state.isPreLayout) {
                    val layoutParams = view.layoutParams as LayoutParams
                    layoutParams.apply {
                        row = getGlobalRowOfPosition(nextPosition)
                        column = getGlobalColumnOfPosition(nextPosition)
                    }
                }

                measureChildWithMargins(view, 0, 0)
                layoutDecorated(
                    view, leftOffset, topOffset,
                    leftOffset + decoratedChildWidth,
                    topOffset + decoratedChildHeight
                )
            } else {
                // Re-attach the cached view at its new index
                attachView(view)
                viewCache.remove(nextPosition)
            }

            if (index % visibleColumnCount == visibleColumnCount - 1) {
                leftOffset = startLeftOffset
                topOffset += decoratedChildHeight

                if (state.isPreLayout) {
                    layoutAppearingViews(
                        recycler,
                        view,
                        nextPosition,
                        removedPositions?.size() ?: 0,
                        offsetPositionDelta
                    )
                }
            } else {
                leftOffset += decoratedChildWidth
            }
        }

        /*
         * Finally, we ask the Recycler to scrap and store any views
         * that we did not re-attach. These are views that are not currently
         * necessary because they are no longer visible.
         */for (i in 0 until viewCache.size()) {
            val removingView = viewCache.valueAt(i)
            recycler.recycleView(removingView!!)
        }
    }

    override fun scrollToPosition(position: Int) {
        if (position >= itemCount) {
            Log.e(TAG, "Cannot scroll to $position, item count is $itemCount")
            return
        }

        firstVisiblePosition = position
        removeAllViews()
        requestLayout()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        if (position >= itemCount) {
            Log.e(TAG, "Cannot scroll to $position, item count is $itemCount")
            return
        }

        val scroller: LinearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                val rowOffset =
                    (getGlobalRowOfPosition(targetPosition) - getGlobalRowOfPosition(firstVisiblePosition))
                val columnOffset =
                    (getGlobalColumnOfPosition(targetPosition) - getGlobalColumnOfPosition(firstVisiblePosition))
                return PointF(
                    (columnOffset * decoratedChildWidth).toFloat(),
                    (rowOffset * decoratedChildHeight).toFloat()
                )
            }
        }
        scroller.targetPosition = position
        startSmoothScroll(scroller)
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        if (childCount == 0) {
            return 0
        }

        val leftView = getChildAt(0)
        val rightView = getChildAt(visibleColumnCount - 1)

        val viewSpan = getDecoratedRight(rightView!!) - getDecoratedLeft(leftView!!)
        if (viewSpan < horizontalSpace) {
            return 0
        }

        val leftBoundReached = firstVisibleColumn == 0
        val rightBoundReached = lastVisibleColumn >= totalColumnCount
        val delta: Int = if (dx > 0) {
            if (rightBoundReached) {
                val rightOffset = horizontalSpace - getDecoratedRight(rightView) + horizontalPadding
                (-dx).coerceAtLeast(rightOffset)
            } else {
                -dx
            }
        } else {
            if (leftBoundReached) {
                val leftOffset = -getDecoratedLeft(leftView) + horizontalPadding
                (-dx).coerceAtMost(leftOffset)
            } else {
                -dx
            }
        }

        offsetChildrenHorizontal(delta)
        if (dx > 0) {
            if (getDecoratedRight(leftView) < 0 && !rightBoundReached) {
                fillGrid(DIRECTION_END, recycler, state)
            } else if (!rightBoundReached) {
                fillGrid(DIRECTION_NONE, recycler, state)
            }
        } else {
            if (getDecoratedLeft(leftView) > 0 && !leftBoundReached) {
                fillGrid(DIRECTION_START, recycler, state)
            } else if (!leftBoundReached) {
                fillGrid(DIRECTION_NONE, recycler, state)
            }
        }

        return -delta
    }

    override fun canScrollHorizontally(): Boolean = true

    override fun canScrollVertically(): Boolean = true

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        if (childCount == 0) {
            return 0
        }

        val topView = getChildAt(0)
        val bottomView = getChildAt(childCount - 1)

        val viewSpan = getDecoratedBottom(bottomView!!) - getDecoratedTop(topView!!)
        if (viewSpan < verticalSpace) {
            return 0
        }

        val maxRowCount = totalRowCount
        val topBoundReached = firstVisibleRow == 0
        val bottomBoundReached = lastVisibleRow >= maxRowCount
        val delta: Int = if (dy > 0) {
            if (bottomBoundReached) {
                val bottomOffset: Int = if (rowOfIndex(childCount - 1) >= maxRowCount - 1) {
                    (verticalSpace - getDecoratedBottom(bottomView) + verticalPadding)
                } else {
                    verticalSpace - (getDecoratedBottom(bottomView) + decoratedChildHeight) + verticalPadding
                }
                (-dy).coerceAtLeast(bottomOffset)
            } else {
                -dy
            }
        } else {
            if (topBoundReached) {
                val topOffset = -getDecoratedTop(topView) + verticalPadding
                (-dy).coerceAtMost(topOffset)
            } else {
                -dy
            }
        }

        offsetChildrenVertical(delta)

        if (dy > 0) {
            if (getDecoratedBottom(topView) < 0 && !bottomBoundReached) {
                fillGrid(DIRECTION_DOWN, recycler, state)
            } else if (!bottomBoundReached) {
                fillGrid(DIRECTION_NONE, recycler, state)
            }
        } else {
            if (getDecoratedTop(topView) > 0 && !topBoundReached) {
                fillGrid(DIRECTION_UP, recycler, state)
            } else if (!topBoundReached) {
                fillGrid(DIRECTION_NONE, recycler, state)
            }
        }

        return -delta
    }

    override fun findViewByPosition(position: Int): View? =
        (0 until childCount).firstOrNull { positionOfIndex(it) == position }?.let { getChildAt(it) }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(context: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(layoutParams: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return if (layoutParams is MarginLayoutParams) {
            LayoutParams(layoutParams)
        } else {
            LayoutParams(layoutParams)
        }
    }

    override fun checkLayoutParams(layoutParams: RecyclerView.LayoutParams): Boolean {
        return layoutParams is LayoutParams
    }

    private fun layoutAppearingViews(
        recycler: Recycler,
        referenceView: View,
        referencePosition: Int,
        extraCount: Int,
        offset: Int
    ) {
        if (extraCount > 1) {
            // FIXME: This code currently causes double layout of views that are still visible…
            (1..extraCount)
                .map { extra -> referencePosition + extra }
                .filterNot { extraPosition ->
                    extraPosition < 0 || extraPosition >= itemCount
                }
                .forEach { extraPosition ->
                    val appearing = recycler.getViewForPosition(extraPosition)
                    addView(appearing)

                    val newRow = getGlobalRowOfPosition(extraPosition + offset)
                    val rowDelta = newRow - getGlobalRowOfPosition(referencePosition + offset)
                    val newCol = getGlobalColumnOfPosition(extraPosition + offset)
                    val colDelta = newCol - getGlobalColumnOfPosition(referencePosition + offset)
                    layoutTempChildView(appearing, rowDelta, colDelta, referenceView)
                }
        }
    }

    private fun layoutDisappearingView(disappearingChild: View) {
        addDisappearingView(disappearingChild)

        val layoutParams = disappearingChild.layoutParams as LayoutParams
        val newRow = getGlobalRowOfPosition(layoutParams.viewAdapterPosition)
        val rowDelta = newRow - layoutParams.row
        val newCol = getGlobalColumnOfPosition(layoutParams.viewAdapterPosition)
        val colDelta = newCol - layoutParams.column
        layoutTempChildView(disappearingChild, rowDelta, colDelta, disappearingChild)
    }

    private fun layoutTempChildView(
        child: View,
        rowDelta: Int,
        colDelta: Int,
        referenceView: View
    ) {
        val layoutTop = getDecoratedTop(referenceView) + rowDelta * decoratedChildHeight
        val layoutLeft = getDecoratedLeft(referenceView) + colDelta * decoratedChildWidth
        measureChildWithMargins(child, 0, 0)
        layoutDecorated(
            child, layoutLeft, layoutTop,
            layoutLeft + decoratedChildWidth,
            layoutTop + decoratedChildHeight
        )
    }

    private fun getGlobalColumnOfPosition(position: Int): Int =
        position % maxColumnCount

    private fun getGlobalRowOfPosition(position: Int): Int =
        position / maxColumnCount

    private fun positionOfIndex(childIndex: Int): Int {
        val row = childIndex / visibleColumnCount
        val column = childIndex % visibleColumnCount
        return firstVisiblePosition + row * totalColumnCount + column
    }

    private fun rowOfIndex(childIndex: Int): Int =
        positionOfIndex(childIndex) / totalColumnCount

    private val firstVisibleColumn: Int
        get() = firstVisiblePosition % totalColumnCount

    private val lastVisibleColumn: Int
        get() = firstVisibleColumn + visibleColumnCount

    private val firstVisibleRow: Int
        get() = firstVisiblePosition / totalColumnCount

    private val lastVisibleRow: Int
        get() = firstVisibleRow + visibleRowCount

    private val visibleChildCount: Int
        get() = visibleColumnCount * visibleRowCount

    private val totalColumnCount: Int
        get() = if (itemCount < maxColumnCount) {
            itemCount
        } else {
            maxColumnCount
        }

    private val totalRowCount: Int
        get() {
            return if (itemCount == 0 || maxColumnCount == 0) {
                0
            } else {
                val maxRow = itemCount / maxColumnCount

                // Bump the row count if it's not exactly even
                if (itemCount % maxColumnCount != 0) {
                    maxRow + 1
                } else {
                    maxRow
                }
            }
        }

    private val horizontalSpace: Int
        get() = width

    private val verticalSpace: Int
        get() = height

    companion object {
        private val TAG = FixedGridLayoutManager::class.simpleName

        // View Removal Constants
        private const val REMOVE_VISIBLE = 0
        private const val REMOVE_INVISIBLE = 1

        // Fill Direction Constants
        private const val DIRECTION_NONE = -1
        private const val DIRECTION_START = 0
        private const val DIRECTION_END = 1
        private const val DIRECTION_UP = 2
        private const val DIRECTION_DOWN = 3
    }

    class LayoutParams : RecyclerView.LayoutParams {
        // Current row in the grid
        var row = 0

        // Current column in the grid
        var column = 0

        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: MarginLayoutParams?) : super(source)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
    }
}

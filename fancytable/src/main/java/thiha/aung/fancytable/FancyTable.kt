//package thiha.aung.fancytable
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.database.DataSetObserver
//import android.util.AttributeSet
//import android.util.Log
//import android.view.MotionEvent
//import android.view.VelocityTracker
//import android.view.View
//import android.view.ViewConfiguration
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.Scroller
//import com.fancytable.R
//
///**
// * This view shows a table which can scroll in both directions.
// * Can define how many rows or columns needs to be fixed/docked: rows fixed on the top and columns fixed on the left
// * Can add headers. Headers are not horizontally scrollable
// */
//class FancyTable @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
//    ViewGroup(context, attrs) {
//    private val shadows: Array<ImageView?>
//    private val shadowSize: Int
//    private val minimumVelocity: Int
//    private val maximumVelocity: Int
//    private val flinger: Flinger
//    private var currentX = 0
//    private var currentY = 0
//
//    /**
//     * Returns the adapter currently associated with this widget.
//     *
//     * @return The adapter used to provide this view's content.
//     */
//    var adapter: FancyTableAdapter? = null
//        private set
//    private var scrollX = 0
//    private var scrollY = 0
//    private var firstScrollableRow = 0
//    private var firstScrollableColumn = 0
//    private var dockedRowCount = 0
//    private var dockedColumnCount = 0
//    private var widths = mutableListOf<Int>()
//    private var heights = mutableListOf<Int>()
//    private val bodyViewTable: MutableList<MutableList<View?>>
//    private var rowCount = 0
//    private var columnCount = 0
//    private var width = 0
//    private var height = 0
//    private var recycler: Recycler? = null
//    private var tableAdapterDataSetObserver: TableAdapterDataSetObserver? = null
//    private var needRelayout: Boolean
//    private var velocityTracker: VelocityTracker? = null
//    private val touchSlop: Int
//    /**
//     * Constructor that is called when inflating a view from XML. This is called
//     * when a view is being constructed from an XML file, supplying attributes
//     * that were specified in the XML file. This version uses a default style of
//     * 0, so the only attribute values applied are those in the Context's Theme
//     * and the given AttributeSet.
//     *
//     * The method onFinishInflate() will be called after all children have been
//     * added.
//     *
//     * @param context The Context the view is running in, through which it can
//     * access the current theme, resources, etc.
//     * @param attrs   The attributes of the XML tag that is inflating the view.
//     */
//    /**
//     * Simple constructor to use when creating a view from code.
//     *
//     * @param context The Context the view is running in, through which it can
//     * access the current theme, resources, etc.
//     */
//    init {
//        bodyViewTable = ArrayList()
//        needRelayout = true
//        shadows = arrayOfNulls(4)
//        shadows[0] = ImageView(context)
//        shadows[0]!!.setImageResource(R.drawable.shadow_left)
//        shadows[1] = ImageView(context)
//        shadows[1]!!.setImageResource(R.drawable.shadow_top)
//        shadows[2] = ImageView(context)
//        shadows[2]!!.setImageResource(R.drawable.shadow_right)
//        shadows[3] = ImageView(context)
//        shadows[3]!!.setImageResource(R.drawable.shadow_bottom)
//        shadowSize = resources.getDimensionPixelSize(R.dimen.shadow_size)
//        isVerticalScrollBarEnabled = false
//        isHorizontalScrollBarEnabled = false
//        flinger = Flinger(context)
//        val configuration = ViewConfiguration.get(context!!)
//        touchSlop = configuration.scaledTouchSlop
//        minimumVelocity = configuration.scaledMinimumFlingVelocity
//        maximumVelocity = configuration.scaledMaximumFlingVelocity
//        setWillNotDraw(false)
//    }
//
//    /**
//     * Sets the data behind this TableFixHeaders.
//     *
//     * @param adapter The TableAdapter which is responsible for maintaining the data
//     * backing this list and for producing a view to represent an
//     * item in that data set.
//     */
//    fun setTableAdapter(adapter: FancyTableAdapter) {
//        if (this.adapter != null) {
//            this.adapter!!.unregisterDataSetObserver(tableAdapterDataSetObserver)
//        }
//        this.adapter = adapter
//        tableAdapterDataSetObserver = TableAdapterDataSetObserver()
//        this.adapter!!.registerDataSetObserver(tableAdapterDataSetObserver!!)
//        recycler = Recycler(adapter.viewTypeCount)
//        scrollX = 0
//        scrollY = 0
//        firstScrollableRow = adapter.dockedRowCount
//        firstScrollableColumn = adapter.dockedColumnCount
//        needRelayout = true
//        requestLayout()
//    }
//
//    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
//        var intercept = false
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                currentX = event.rawX.toInt()
//                currentY = event.rawY.toInt()
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                val x2 = Math.abs(currentX - event.rawX.toInt())
//                val y2 = Math.abs(currentY - event.rawY.toInt())
//                if (x2 > touchSlop || y2 > touchSlop) {
//                    intercept = true
//                }
//            }
//        }
//        return intercept
//    }
//
//    override fun removeView(view: View) {
//        super.removeView(view)
//        val typeView = view.getTag(R.id.tag_type_view) as Int
//        if (typeView != FancyTableAdapter.Companion.IGNORE_ITEM_VIEW_TYPE) {
//            recycler!!.addRecycledView(view, typeView)
//        }
//    }
//
//    @SuppressLint("DrawAllocation")
//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        if (needRelayout || changed) {
//            needRelayout = false
//            resetTable()
//            if (adapter != null) {
//                width = r - l
//                height = b - t
//                var left: Int
//                var top: Int
//                var right: Int
//                var bottom: Int
//                right = Math.min(width, sumArray(widths))
//                bottom = Math.min(height, sumArray(heights))
//                if (adapter!!.isColumnShadowShown) {
//                    val columnShadowLeft = sumArray(widths, 0, dockedColumnCount)
//                    addShadow(
//                        shadows[0],
//                        columnShadowLeft,
//                        0,
//                        columnShadowLeft + shadowSize,
//                        bottom
//                    )
//                }
//                if (adapter!!.isRowShadowShown) {
//                    val rowShadowTop = sumArray(heights, 0, dockedRowCount)
//                    addShadow(shadows[1], 0, rowShadowTop, right, rowShadowTop + shadowSize)
//                }
//                //                addShadow(shadows[2], right - shadowSize, 0, right, bottom);
//                //                addShadow(shadows[3], 0, bottom - shadowSize, right, bottom);
//                scrollBounds()
//                adjustFirstCellsAndScroll()
//
//
//                // add table view
//                top = 0
//                var i = 0
//                while (i < rowCount && top < height) {
//                    bottom = top + (heights.getOrNull(i) ?: 0)
////                    bottom = top + heights[i]
//                    left = 0
//                    val list: MutableList<View?> = ArrayList()
//                    if (adapter!!.isRowOneColumn(i)) {
//                        val view = makeAndSetup(i, 0, 0, top, width, bottom)
//                        view!!.setTag(
//                            R.id.tag_column_type,
//                            FancyTableAdapter.Companion.FILL_WIDTH_VIEW
//                        )
//                        list.add(view)
//                    } else {
//                        var j = 0
//                        while (j < columnCount && left < width) {
//                            right = left + widths[j]
//                            val view = makeAndSetup(i, j, left, top, right, bottom)
//                            view!!.setTag(R.id.tag_column_type, null)
//                            list.add(view)
//                            left = right
//                            j++
//                        }
//                    }
//                    bodyViewTable.add(list)
//                    top = bottom
//                    i++
//                }
//                shadowsVisibility()
//            }
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (velocityTracker == null) { // If we do not have velocity tracker
//            velocityTracker = VelocityTracker.obtain() // then get one
//        }
//        velocityTracker!!.addMovement(event) // add this movement to it
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                if (!flinger.isFinished) { // If scrolling, then stop now
//                    flinger.forceFinished()
//                }
//                currentX = event.rawX.toInt()
//                currentY = event.rawY.toInt()
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                val x2 = event.rawX.toInt()
//                val y2 = event.rawY.toInt()
//                val diffX = currentX - x2
//                val diffY = currentY - y2
//                Log.d(
//                    FancyTable::class.java.simpleName, String.format(
//                        "scrollX currentX=%d, currentY=%d, x2=%d, y2=%d, diffX=%d, diffY=%d",
//                        currentX,
//                        currentY,
//                        x2,
//                        y2,
//                        diffX,
//                        diffY
//                    )
//                )
//                currentX = x2
//                currentY = y2
//                try {
//                    scrollBy(diffX, diffY)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//
//            MotionEvent.ACTION_UP -> {
//                val velocityTracker = velocityTracker
//                velocityTracker!!.computeCurrentVelocity(1000, maximumVelocity.toFloat())
//                val velocityX = velocityTracker.xVelocity.toInt()
//                val velocityY = velocityTracker.yVelocity.toInt()
//                if (Math.abs(velocityX) > minimumVelocity || Math.abs(velocityY) > minimumVelocity) {
//                    flinger.start(
//                        actualScrollX,
//                        actualScrollY,
//                        velocityX,
//                        velocityY,
//                        maxScrollX,
//                        maxScrollY
//                    )
//                } else {
//                    if (this.velocityTracker != null) { // If the velocity less than threshold
//                        this.velocityTracker!!.recycle() // recycle the tracker
//                        this.velocityTracker = null
//                    }
//                }
//            }
//        }
//        return true
//    }
//
//    override fun scrollTo(x: Int, y: Int) {
//        if (needRelayout) {
//            scrollX = x
////            firstScrollableColumn = if (adapter == null) 0 else adapter.getDockedColumnCount()
//            firstScrollableColumn = if (adapter == null) 0 else adapter?.dockedColumnCount ?: 0
//            scrollY = y
////            firstScrollableRow = if (adapter == null) 0 else adapter.getDockedRowCount()
//            firstScrollableRow = if (adapter == null) 0 else adapter?.dockedRowCount ?: 0
//        } else {
//            scrollBy(
//                x - sumArray(widths, dockedColumnCount, firstScrollableColumn) - scrollX,
//                y - sumArray(heights, dockedRowCount, firstScrollableRow) - scrollY
//            )
//        }
//    }
//
//    override fun scrollBy(x: Int, y: Int) {
//        Log.d(
//            FancyTable::class.java.simpleName, String.format(
//                "original scrollX=%d, scrollY=%d",
//                scrollX,
//                scrollY
//            )
//        )
//        scrollX += x
//        scrollY += y
//        Log.d(
//            FancyTable::class.java.simpleName, String.format(
//                "before scrollX=%d, scrollY=%d",
//                scrollX,
//                scrollY
//            )
//        )
//        if (needRelayout) {
//            return
//        }
//        scrollBounds()
//        Log.d(
//            FancyTable::class.java.simpleName, String.format(
//                "before scrollX=%d, scrollY=%d",
//                scrollX,
//                scrollY
//            )
//        )
//
//        /*
//         * TODO Improve the algorithm. Think big diagonal movements. If we are
//         * in the top left corner and scrollBy to the opposite corner. We will
//         * have created the views from the top right corner on the X part and we
//         * will have eliminated to generate the right at the Y.
//         */
//        val rowViewList = rowViewList
//        if (scrollX == 0 || rowViewList.size < dockedColumnCount) {
//            // no op
//        } else if (scrollX > 0) {
//            while (widths[firstScrollableColumn] < scrollX) {
//                if (!rowViewList.isEmpty()) {
//                    removeLeft()
//                }
//                scrollX -= widths[firstScrollableColumn]
//                firstScrollableColumn++
//            }
//            while (filledWidth < width) {
//                addRight()
//            }
//        } else {
//            val currentLastColumn = firstScrollableColumn - dockedColumnCount + rowViewList.size - 1
//            Log.d(
//                FancyTable::class.java.simpleName, String.format(
//                    "firstScrollableColumn=%d, currentLastColumn=%d, rowViewList.size()=%d",
//                    firstScrollableColumn,
//                    currentLastColumn,
//                    rowViewList.size
//                )
//            )
//            while (!rowViewList.isEmpty() && filledWidth - widths[currentLastColumn] >= width) {
//                removeRight()
//            }
//            if (rowViewList.isEmpty()) {
//                while (scrollX < 0) {
//                    scrollX += widths[firstScrollableColumn]
//                    firstScrollableColumn--
//                }
//                while (filledWidth < width) {
//                    addRight()
//                }
//            } else {
//                while (0 > scrollX) {
//                    addLeft()
//                    firstScrollableColumn--
//                    scrollX += widths[firstScrollableColumn]
//                }
//            }
//        }
//        if (scrollY == 0 || bodyViewTable.size < dockedRowCount) {
//            // no op
//        } else if (scrollY > 0) {
//            while (heights[firstScrollableRow] < scrollY) {
//                if (!bodyViewTable.isEmpty()) {
//                    removeTop()
//                }
//                scrollY -= heights[firstScrollableRow]
//                firstScrollableRow++
//            }
//            while (filledHeight < height) {
//                addBottom()
//            }
//        } else {
//            val currentLastRow = firstScrollableRow - dockedRowCount + bodyViewTable.size - 1
//            while (!bodyViewTable.isEmpty()
//                && filledHeight - heights[currentLastRow] >= height
//            ) {
//                removeBottom()
//            }
//            if (bodyViewTable.isEmpty()) {
//                while (scrollY < 0) {
//                    scrollY += heights[firstScrollableRow]
//                    firstScrollableRow--
//                }
//                while (filledHeight < height) {
//                    addBottom()
//                }
//            } else {
//                while (0 > scrollY) {
//                    addTop()
//                    firstScrollableRow--
//                    scrollY += heights[firstScrollableRow]
//                }
//            }
//        }
//        repositionViews()
//        shadowsVisibility()
//        awakenScrollBars()
//    }
//
//    private val rowViewList: List<View?>
//        private get() {
//            for (list in bodyViewTable) {
//
////                if (!(list.size() > 0 && FILL_WIDTH_VIEW.equals(list.get(0).getTag(R.id.tag_column_type)))){
//
//                if (list.isNotEmpty() && FancyTableAdapter.FILL_WIDTH_VIEW != list.getOrNull(0)
//                        ?.getTag(R.id.tag_column_type)
//                ) {
//                    return list
//                }
//            }
//            return bodyViewTable.getOrNull(0)?.toList() ?: listOf()
//        }
//
//    /*
//     * The base measure
//     */
//    override fun computeHorizontalScrollRange(): Int {
//        return width
//    }
//
//    /*
//     * The expected value is between 0 and computeHorizontalScrollRange() - computeHorizontalScrollExtent()
//     */
//    override fun computeHorizontalScrollOffset(): Int {
//        val maxScrollX = (sumArray(widths) - width).toFloat()
//        val percentageOfViewScrolled = actualScrollX / maxScrollX
//        val scrollStartLeft = sumArray(widths, 0, dockedColumnCount)
//        val maxHorizontalScrollOffset = width - scrollStartLeft - computeHorizontalScrollExtent()
//        return scrollStartLeft + Math.round(percentageOfViewScrolled * maxHorizontalScrollOffset)
//    }
//
//    /*
//     * The expected value is: percentageOfViewScrolled * computeHorizontalScrollRange()
//     */
//    override fun computeHorizontalScrollExtent(): Int {
//        val scrollStartLeft = sumArray(widths, 0, dockedColumnCount)
//        val tableSize = (width - scrollStartLeft).toFloat()
//        val contentSize = sumArray(widths, dockedColumnCount, widths.size).toFloat()
//        val percentageOfVisibleView = tableSize / contentSize
//        return Math.round(percentageOfVisibleView * tableSize)
//    }
//
//    /*
//     * The base measure
//     */
//    override fun computeVerticalScrollRange(): Int {
//        return height
//    }
//
//    /*
//     * The expected value is between 0 and computeVerticalScrollRange() - computeVerticalScrollExtent()
//     */
//    override fun computeVerticalScrollOffset(): Int {
//        val maxScrollY = (sumArray(heights) - height).toFloat()
//        val percentageOfViewScrolled = actualScrollY / maxScrollY
//        val scrollStartTop = sumArray(heights, 0, dockedRowCount)
//        val maxHorizontalScrollOffset = height - scrollStartTop - computeVerticalScrollExtent()
//        return scrollStartTop + Math.round(percentageOfViewScrolled * maxHorizontalScrollOffset)
//    }
//
//    /*
//     * The expected value is: percentageOfViewScrolled * computeVerticalScrollRange()
//     */
//    override fun computeVerticalScrollExtent(): Int {
//        val scrollStartTop = sumArray(heights, 0, dockedRowCount)
//        val tableSize = (height - scrollStartTop).toFloat()
//        val contentSize = sumArray(heights, dockedRowCount, heights.size).toFloat()
//        val percentageOfVisibleView = tableSize / contentSize
//        return Math.round(percentageOfVisibleView * tableSize)
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
//        val w: Int
//        val h: Int
//        if (adapter != null) {
////            rowCount = adapter.getRowCount()
//            rowCount = adapter?.rowCount ?: 0
////            columnCount = adapter.getColumnCount()
//            columnCount = adapter?.columnCount ?: 0
////            dockedRowCount = adapter.getDockedRowCount()
//            dockedRowCount = adapter?.dockedRowCount ?: 0
////            dockedColumnCount = adapter.getDockedColumnCount()
//            dockedColumnCount = adapter?.dockedColumnCount ?: 0
//            widths = mutableListOf(columnCount)
//            heights = mutableListOf(rowCount)
//
//            widths.forEachIndexed { index, i ->
//                widths[index] += adapter?.getWidth(index) ?: 0
//            }
//            heights.forEachIndexed { index, i ->
//                heights[index] += adapter?.getHeight(index) ?: 0
//            }
////            widths.forEachIndexed { index, i -> }
//
////            for (i in 0 until columnCount) {
//////                widths[i] += adapter!!.getWidth(i)
//////                widths[i] += adapter?.getWidth(i) ?: 0
////
////                widths?.getOrNull(i)
////                widths[i] += adapter?.getWidth(i) ?: 0
////            }
////            for (i in 0 until rowCount) {
////                heights[i] += adapter!!.getHeight(i)
////            }
//            if (widthMode == MeasureSpec.AT_MOST) {
//                w = Math.min(widthSize, sumArray(widths))
//            } else if (widthMode == MeasureSpec.UNSPECIFIED) {
//                w = sumArray(widths)
//            } else {
//                w = widthSize
//                val sumArray = sumArray(widths)
//                if (sumArray < widthSize) {
//                    val factor = widthSize / sumArray.toFloat()
//                    for (i in 1 until widths.size) {
//                        widths[i] = Math.round(widths[i] * factor)
//                    }
//                    widths[0] = widthSize - sumArray(widths, 1, widths.size)
//                }
//            }
//            h = if (heightMode == MeasureSpec.AT_MOST) {
//                Math.min(heightSize, sumArray(heights))
//            } else if (heightMode == MeasureSpec.UNSPECIFIED) {
//                sumArray(heights)
//            } else {
//                heightSize
//            }
//        } else {
//            if (heightMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
//                w = 0
//                h = 0
//            } else {
//                w = widthSize
//                h = heightSize
//            }
//        }
//        if (firstScrollableRow >= rowCount || maxScrollY - actualScrollY < 0) {
////            firstScrollableRow = if (adapter == null) 0 else adapter.getDockedRowCount()
//            firstScrollableRow = if (adapter == null) 0 else adapter?.dockedRowCount ?: 0
//            scrollY = Int.MAX_VALUE
//        }
//        if (firstScrollableColumn >= columnCount || maxScrollX - actualScrollX < 0) {
////            firstScrollableColumn = if (adapter == null) 0 else adapter.getDockedColumnCount()
//            firstScrollableColumn = if (adapter == null) 0 else adapter?.dockedColumnCount ?: 0
//            scrollX = Int.MAX_VALUE
//        }
//        setMeasuredDimension(w, h)
//    }
//
//    val actualScrollX: Int
//        get() = scrollX + sumArray(widths, dockedColumnCount, firstScrollableColumn)
//    val actualScrollY: Int
//        get() = scrollY + sumArray(heights, dockedRowCount, firstScrollableRow)
//    private val maxScrollX: Int
//        private get() = Math.max(0, sumArray(widths) - width)
//    private val maxScrollY: Int
//        private get() = Math.max(0, sumArray(heights) - height)
//    private val filledWidth: Int
//        private get() {
//            val rowViewList = rowViewList
//            val columnSize = rowViewList.size
//            val firstRemovedRight = columnSize + firstScrollableColumn - dockedColumnCount
//            Log.d(
//                FancyTable::class.java.simpleName, String.format(
//                    "firstScrollableColumn=%d, firstRemovedRight=%d",
//                    firstScrollableColumn,
//                    firstRemovedRight
//                )
//            )
//            return sumArray(widths, 0, dockedColumnCount) + sumArray(
//                widths,
//                firstScrollableColumn,
//                firstRemovedRight
//            ) - scrollX
//        }
//    private val filledHeight: Int
//        private get() {
//            val rowSize = bodyViewTable.size
//            return sumArray(heights, 0, dockedRowCount) + sumArray(
//                heights,
//                firstScrollableRow,
//                rowSize + firstScrollableRow - dockedRowCount
//            ) - scrollY
//        }
//
//    private fun addLeft() {
//        val column = firstScrollableColumn - 1
//        val index = dockedColumnCount
//        addLeftOrRight(column, index)
//    }
//
//    private fun addTop() {
//        val row = firstScrollableRow - 1
//        val index = dockedRowCount
//        addTopAndBottom(row, index)
//    }
//
//    private fun addRight() {
//        val index = rowViewList.size
//        val column = firstScrollableColumn - dockedColumnCount + index
//        addLeftOrRight(column, index)
//    }
//
//    private fun addBottom() {
//        val index = bodyViewTable.size
//        val row = firstScrollableRow - dockedRowCount + index
//        addTopAndBottom(row, index)
//    }
//
//    private fun addLeftOrRight(column: Int, index: Int) {
//        var view: View?
//        var list = mutableListOf<View?>()
//        var row: Int
//        row = 0
//        while (row < dockedRowCount) {
//            list = bodyViewTable[row].toMutableList()
//            if (!adapter!!.isRowOneColumn(row)) {
//                view = makeView(row, column, widths[column], heights[row])
//                view!!.setTag(R.id.tag_column_type, null)
////                list.add(index, view)
//                list.add(index, view)
//            }
//            row++
//        }
//        row = firstScrollableRow
//        while (row < bodyViewTable.size + firstScrollableRow - dockedRowCount) {
//            list = bodyViewTable[row - firstScrollableRow + dockedRowCount].toMutableList()
//            if (!adapter!!.isRowOneColumn(row)) {
//                view = makeView(row, column, widths[column], heights[row])
//                view!!.setTag(R.id.tag_column_type, null)
//                list.add(index, view)
//            }
//            row++
//        }
//    }
//
//    private fun addTopAndBottom(row: Int, index: Int) {
//        var view: View?
//        val list: MutableList<View?> = ArrayList()
//        val rowViewList = rowViewList
//        if (adapter!!.isRowOneColumn(row)) {
//            view = makeView(row, 0, width, heights[row])
//            view!!.setTag(R.id.tag_column_type, FancyTableAdapter.Companion.FILL_WIDTH_VIEW)
//            list.add(view)
//        } else {
//            var column: Int
//            column = 0
//            while (column < dockedColumnCount) {
//                view = makeView(row, column, widths[column], heights[row])
//                view!!.setTag(R.id.tag_column_type, null)
//                list.add(view)
//                column++
//            }
//            column = firstScrollableColumn
//            while (column < rowViewList.size + firstScrollableColumn - dockedColumnCount) {
//                view = makeView(row, column, widths[column], heights[row])
//                view!!.setTag(R.id.tag_column_type, null)
//                list.add(view)
//                column++
//            }
//        }
//        bodyViewTable.add(index, list)
//    }
//
//    private fun removeLeft() {
//        removeLeftOrRight(dockedColumnCount)
//    }
//
//    private fun removeTop() {
//        removeTopOrBottom(dockedRowCount)
//    }
//
//    private fun removeRight() {
//        val rowViewList = rowViewList
//        removeLeftOrRight(rowViewList.size - 1)
//    }
//
//    private fun removeBottom() {
//        removeTopOrBottom(bodyViewTable.size - 1)
//    }
//
//    private fun removeLeftOrRight(position: Int) {
//        for (list in bodyViewTable) {
//            // shouldn't remove a header
//            if (list.size > 0 && FancyTableAdapter.FILL_WIDTH_VIEW != list[0]?.getTag(R.id.tag_column_type)) {
////            if ((list.size > 0 && FancyTableAdapter.Companion.FILL_WIDTH_VIEW) != list[0].getTag(R.id.tag_column_type)) {
////                removeView(list.removeAt(position))
//                list.removeAt(position)?.let { removeView(it) }
//            }
//        }
//    }
//
//    private fun removeTopOrBottom(position: Int) {
//        val remove = bodyViewTable.removeAt(position)
//        for (view in remove) {
//            view?.let { removeView(it) }
//        }
//    }
//
//    private fun repositionViews() {
//        var left: Int
//        var top: Int
//        var right: Int
//        var bottom: Int
//        var row: Int
//        var column: Int
//        val scrollStartLeft = sumArray(widths, 0, dockedColumnCount)
//        val scrollStartTop = sumArray(heights, 0, dockedRowCount)
//        var columnCells: List<View?>
//        var view: View?
//
//
//        // reposition fixed rows
//        top = 0
//        row = 0
//        while (row < dockedRowCount && row < bodyViewTable.size) {
//            bottom = top + heights[row]
//            left = scrollStartLeft - scrollX
//            columnCells = bodyViewTable[row]
//            if (adapter!!.isRowOneColumn(row)) {
//                view = columnCells[0]
//                view!!.layout(0, top, width, bottom)
//            } else {
//                column = firstScrollableColumn
//                while (column < columnCells.size + firstScrollableColumn - dockedColumnCount) {
//                    view = columnCells[column - firstScrollableColumn + dockedColumnCount]
//                    right = left + widths[column]
//                    view!!.layout(left, top, right, bottom)
//                    Log.d(
//                        FancyTable::class.java.simpleName, String.format(
//                            "columnIndex=%d, left=%d, right=%d",
//                            column,
//                            left,
//                            right
//                        )
//                    )
//                    left = right
//                    column++
//                }
//            }
//            top = bottom
//            row++
//        }
//
//        // reposition fixed columns
//        top = scrollStartTop - scrollY
//        row = firstScrollableRow
//        while (row < bodyViewTable.size + firstScrollableRow - dockedRowCount) {
//            bottom = top + heights[row]
//            left = 0
//            columnCells = bodyViewTable[row - firstScrollableRow + dockedRowCount]
//            if (adapter!!.isRowOneColumn(row)) {
//                view = columnCells[0]
//                view!!.layout(0, top, width, bottom)
//            } else {
//                column = 0
//                while (column < dockedColumnCount && column < columnCells.size) {
//                    view = columnCells[column]
//                    right = left + widths[column]
//                    view!!.layout(left, top, right, bottom)
//                    left = right
//                    column++
//                }
//            }
//            top = bottom
//            row++
//        }
//
//        // reposition two way scrollable cells
//        top = scrollStartTop - scrollY
//        row = firstScrollableRow
//        while (row < bodyViewTable.size + firstScrollableRow - dockedRowCount) {
//            bottom = top + heights[row]
//            left = scrollStartLeft - scrollX
//            columnCells = bodyViewTable[row - firstScrollableRow + dockedRowCount]
//            if (adapter!!.isRowOneColumn(row)) {
//                view = columnCells[0]
//                view!!.layout(0, top, width, bottom)
//            } else {
//                column = firstScrollableColumn
//                while (column < columnCells.size + firstScrollableColumn - dockedColumnCount) {
//                    view = columnCells[column - firstScrollableColumn + dockedColumnCount]
//                    right = left + widths[column]
//                    view!!.layout(left, top, right, bottom)
//                    left = right
//                    column++
//                }
//            }
//            top = bottom
//            row++
//        }
//        invalidate()
//    }
//
//    //    private fun sumArray(array: IntArray, firstIndex: Int = 0, length: Int = array.length): Int {
//    private fun sumArray(array: List<Int>, firstIndex: Int = 0, length: Int = array.size): Int {
//        var sum = 0
//        for (i in firstIndex until length) {
//            sum += array[i]
//        }
//        return sum
//    }
//
//    private fun scrollBounds() {
////        scrollX = scrollBounds(scrollX, firstScrollableColumn, dockedColumnCount, widths, width)
////        scrollY = scrollBounds(scrollY, firstScrollableRow, dockedRowCount, heights, height)
//    }
//
//    private fun scrollBounds(
//        desiredScroll: Int,
//        firstCell: Int,
//        numFixedCells: Int,
//        sizes: List<Int>,
//        viewSize: Int
//    ): Int {
//        var desiredScroll = desiredScroll
//        Log.d(
//            FancyTable::class.java.simpleName, String.format(
//                "before desiredScroll=%d",
//                desiredScroll
//            )
//        )
//        if (desiredScroll == 0) {
//            // no op
//        } else if (desiredScroll < 0) {
//            desiredScroll = Math.max(desiredScroll, -sumArray(sizes, numFixedCells, firstCell))
//        } else {
//
//            /*desiredScroll = Math.min(desiredScroll, Math.max(0,
//					sumArray(sizes, firstCell + numFixedCells, sizes.length - numFixedCells - firstCell) + sumArray(sizes, 0, numFixedCells) - viewSize
//					)
//			);*/
//            desiredScroll = Math.min(
//                desiredScroll, Math.max(
//                    0,
//                    sumArray(sizes, firstCell, sizes.size) + sumArray(
//                        sizes,
//                        0,
//                        numFixedCells
//                    ) - viewSize
//                )
//            )
//        }
//        Log.d(
//            FancyTable::class.java.simpleName, String.format(
//                "after desiredScroll=%d",
//                desiredScroll
//            )
//        )
//        return desiredScroll
//    }
//
//    private fun adjustFirstCellsAndScroll() {
//        var values: List<Int>
//        values = adjustFirstCellsAndScroll(scrollX, firstScrollableColumn, widths)
//        scrollX = values[0]
//        firstScrollableColumn = values[1]
//        values = adjustFirstCellsAndScroll(scrollY, firstScrollableRow, heights)
//        scrollY = values[0]
//        firstScrollableRow = values[1]
//    }
//
//    private fun adjustFirstCellsAndScroll(scroll: Int, firstCell: Int, sizes: List<Int>): List<Int> {
//        var scroll = scroll
//        var firstCell = firstCell
//        if (scroll == 0) {
//            // no op
//        } else if (scroll > 0) {
//            while (sizes[firstCell + 1] < scroll) {
//                firstCell++
//                scroll -= sizes[firstCell]
//            }
//        } else {
//            while (scroll < 0) {
//                scroll += sizes[firstCell]
//                firstCell--
//            }
//        }
//        return mutableListOf(scroll, firstCell)
//    }
//
//    private fun shadowsVisibility() {
//        val actualScrollX = actualScrollX
//        val actualScrollY = actualScrollY
//        val remainPixels = mutableListOf(
//            actualScrollX, actualScrollY, maxScrollX - actualScrollX, maxScrollY - actualScrollY
//        )
//        for (i in shadows.indices) {
//            setAlpha(shadows[i], Math.min(remainPixels[i] / shadowSize.toFloat(), 1f))
//        }
//    }
//
//    private fun setAlpha(imageView: ImageView?, alpha: Float) {
//        imageView!!.alpha = alpha
//    }
//
//    private fun addShadow(imageView: ImageView?, l: Int, t: Int, r: Int, b: Int) {
//        imageView!!.layout(l, t, r, b)
//        addView(imageView)
//    }
//
//    private fun resetTable() {
//        bodyViewTable.clear()
//        removeAllViews()
//    }
//
//    private fun makeAndSetup(
//        row: Int,
//        column: Int,
//        left: Int,
//        top: Int,
//        right: Int,
//        bottom: Int
//    ): View? {
//        val view = makeView(row, column, right - left, bottom - top)
//        view!!.layout(left, top, right, bottom)
//        return view
//    }
//
//    private fun makeView(row: Int, column: Int, w: Int, h: Int): View? {
//        val itemViewType = adapter!!.getItemViewType(row, column)
//        val recycledView: View?
//        recycledView = if (itemViewType == FancyTableAdapter.Companion.IGNORE_ITEM_VIEW_TYPE) {
//            null
//        } else {
//            recycler!!.getRecycledView(itemViewType)
//        }
//        val view = adapter!!.getTableView(row, column, recycledView, this)
//        view!!.setTag(R.id.tag_type_view, itemViewType)
//        view.setTag(R.id.tag_row, row)
//        view.setTag(R.id.tag_column, column)
//        view.measure(
//            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
//            MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
//        )
//        addTableView(view, row, column)
//        return view
//    }
//
//    private fun addTableView(view: View?, row: Int, column: Int) {
//        if (row < dockedRowCount && column < dockedColumnCount) {
//            addView(view)
//        } else if (row < dockedRowCount || column < dockedColumnCount && !adapter!!.isRowOneColumn(
//                row
//            )
//        ) {
//            var shadows = 0
//            if (adapter!!.isColumnShadowShown) shadows++
//            if (adapter!!.isRowShadowShown) shadows++
//            val index = childCount - dockedRowCount * dockedColumnCount - shadows
//            addView(view, if (index < 0) 0 else index)
//        } else {
//            addView(view, 0)
//        }
//    }
//
//    private inner class TableAdapterDataSetObserver : DataSetObserver() {
//        override fun onChanged() {
//            needRelayout = true
//            requestLayout()
//        }
//
//        override fun onInvalidated() {
//            // Do nothing
//        }
//    }
//
//    // http://stackoverflow.com/a/6219382/842697
//    private inner class Flinger internal constructor(context: Context?) : Runnable {
//        private val scroller: Scroller
//        private var lastX = 0
//        private var lastY = 0
//
//        init {
//            scroller = Scroller(context)
//        }
//
//        fun start(
//            initX: Int,
//            initY: Int,
//            initialVelocityX: Int,
//            initialVelocityY: Int,
//            maxX: Int,
//            maxY: Int
//        ) {
//            scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY)
//            lastX = initX
//            lastY = initY
//            post(this)
//        }
//
//        override fun run() {
//            if (scroller.isFinished) {
//                return
//            }
//            val more = scroller.computeScrollOffset()
//            val x = scroller.currX
//            val y = scroller.currY
//            val diffX = lastX - x
//            val diffY = lastY - y
//            if (diffX != 0 || diffY != 0) {
//                scrollBy(diffX, diffY)
//                lastX = x
//                lastY = y
//            }
//            if (more) {
//                post(this)
//            }
//        }
//
//        val isFinished: Boolean
//            get() = scroller.isFinished
//
//        fun forceFinished() {
//            if (!scroller.isFinished) {
//                scroller.forceFinished(true)
//            }
//        }
//    }
//}
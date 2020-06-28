//package thiha.aung.fancytable
//
//import android.database.DataSetObserver
//import android.view.View
//import android.view.ViewGroup
//
///**
// * The FancyTableAdapter object acts as a bridge between an DockedRowsColsTable and the
// * underlying data for that view. The Adapter provides access to the data items.
// * The Adapter is also responsible for making a View for each item in the data
// * set.
// *
// * @see FancyTable
// */
//interface FancyTableAdapter {
//    /**
//     * Register an observer that is called when changes happen to the data used
//     * by this adapter.
//     *
//     * @param observer the object that gets notified when the data set changes.
//     */
//    fun registerDataSetObserver(observer: DataSetObserver)
//
//    /**
//     * Unregister an observer that has previously been registered with this
//     * adapter via [.registerDataSetObserver].
//     *
//     * @param observer the object to unregister.
//     */
//    fun unregisterDataSetObserver(observer: DataSetObserver?)
//
//    /**
//     * How many rows are in the data table represented by this Adapter.
//     *
//     * @return count of rows.
//     */
//    val rowCount: Int
//
//    /**
//     * How many columns are in the data table represented by this Adapter.
//     *
//     * @return count of columns.
//     */
//    val columnCount: Int
//
//    /**
//     * How many rows are rows fixed on the top meaning the number of rows that are not part of scrollable.
//     *
//     * @return number of rows.
//     */
//    val dockedRowCount: Int
//
//    /**
//     * How many columns are rows fixed on the top meaning the number of columns that are not part of scrollable.
//     *
//     * @return number of columns.
//     */
//    val dockedColumnCount: Int
//
//    /**
//     * Get a View that displays the data at the specified row and column in the
//     * data table. You can either create a View manually or inflate it from an
//     * XML layout file.
//     * Important: All views need to have a background. The background cannot be transparent
//     * If it's transparent, the views scrolled to the under of docked area will be overlapping with
//     * views in the docked area
//     *
//     * @param row The row of the item within the adapter's data table of the
//     * @param column The column of the item within the adapter's data table of the
//     * @param convertView view to be recycled
//     * @param parent The parent that this view will eventually be attached to.
//     * @return A View corresponding to the data at the specified row and column.
//     */
//    fun getTableView(row: Int, column: Int, convertView: View?, parent: ViewGroup?): View?
//
//    /**
//     * Return the width of the column.
//     *
//     * @param column the column
//     * @return The width of the column, in pixels.
//     */
//    fun getWidth(column: Int): Int
//
//    /**
//     * Return the height of the row.
//     *
//     * @param row the row
//     * @return The height of the row, in pixels.
//     */
//    fun getHeight(row: Int): Int
//
//    /**
//     * Is the row one-column row and that column fill width of the parent
//     *
//     * @param row index
//     *
//     * @return if the row should has one column and that column should fill the width of the parent
//     */
//    fun isRowOneColumn(row: Int): Boolean
//
//    /**
//     * should the shadow under docked rows shown
//     *
//     * @return should there be a shadow under horizontal docked area
//     */
//    val isRowShadowShown: Boolean
//
//    /**
//     * should the shadow right to docked columns shown
//     *
//     * @return should there be a shadow right to vertical docked area
//     */
//    val isColumnShadowShown: Boolean
//
//    /**
//     * Get the type of View that will be created by
//     * [.getView] for the specified item.
//     *
//     * @param row The row of the item within the adapter's data table of the
//     * @param column The column of the item within the adapter's data table of the
//     * @return An integer representing the type of View. Two views should share
//     * the same type if one can be converted to the other in
//     * [.getView]). Note: Integers must
//     * be in the range 0 to [.getViewTypeCount] - 1.
//     * [.IGNORE_ITEM_VIEW_TYPE] can also be returned.
//     */
//    fun getItemViewType(row: Int, column: Int): Int
//
//    /**
//     * Returns the number of types of Views that will be created by
//     * [.getView]. Each type represents a set
//     * of views that can be converted in
//     * [.getView]. If the adapter always
//     * returns the same type of View for all items, this method should return 1.
//     *
//     * This method will only be called when when the adapter is set on the the
//     * AdapterView.
//     *
//     * @return The number of types of Views that will be created by this adapter
//     */
//    val viewTypeCount: Int
//
//    companion object {
//        /**
//         * An item view type that causes the AdapterView to ignore the item view.
//         * For example, this can be used if the client does not want a particular
//         * view to be given for conversion in
//         * [.getView].
//         *
//         * @see .getItemViewType
//         */
//        const val IGNORE_ITEM_VIEW_TYPE = -1
//
//        /**
//         * Used as a tag on one-column rows that fill width of the parent
//         * Makes it easy to check if a view fills the width of the parent
//         */
//        const val FILL_WIDTH_VIEW = "FILL_WIDTH_VIEW"
//    }
//}
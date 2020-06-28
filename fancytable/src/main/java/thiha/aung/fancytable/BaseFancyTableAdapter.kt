//package thiha.aung.fancytable
//
//import android.database.DataSetObservable
//import android.database.DataSetObserver
//
//abstract class BaseFancyTableAdapter : FancyTableAdapter {
//
//    private val mDataSetObservable = DataSetObservable()
//
//    override fun registerDataSetObserver(observer: DataSetObserver) {
//        mDataSetObservable.registerObserver(observer)
//    }
//
//    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
//        mDataSetObservable.unregisterObserver(observer)
//    }
//
////    override fun unregisterDataSetObserver(observer: DataSetObserver) {
////        mDataSetObservable.unregisterObserver(observer)
////    }
//
//    /**
//     * Notifies the attached observers that the underlying data has been changed
//     * and any View reflecting the data set should refresh itself.
//     */
//    fun notifyDataSetChanged() {
//        mDataSetObservable.notifyChanged()
//    }
//
//    /**
//     * Notifies the attached observers that the underlying data is no longer
//     * valid or available. Once invoked this adapter is no longer valid and
//     * should not report further data set changes.
//     */
//    fun notifyDataSetInvalidated() {
//        mDataSetObservable.notifyInvalidated()
//    }
//
//    override val dockedRowCount: Int
//        get() = 1
//    override val dockedColumnCount: Int
//        get() = 1
//
//    override fun isRowOneColumn(row: Int): Boolean {
//        return false
//    }
//
//    override val isRowShadowShown: Boolean
//        get() = true
//    override val isColumnShadowShown: Boolean
//        get() = true
//}
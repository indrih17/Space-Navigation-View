package com.luseen.spacelib

/**
 * LongClickListener для кнопок BottomNavView.
 */
interface SpaceOnLongClickListener {
    /** Долгий клик на центральную кнопку. */
    fun onCenterButtonLongClick()

    /**
     * Долгий клик на элемент space nav view.
     * @param index Индекс элемента.
     */
    fun onSpaceItemLongClick(index: Int)
}

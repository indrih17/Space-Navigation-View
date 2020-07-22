package com.luseen.spacelib

/**
 * ClickListener для кнопок BottomNavView.
 */
interface SpaceOnClickListener {
    /** Клик по центральной кнопке. */
    fun onCenterButtonClick()

    /**
     * Клик на элемент space nav view.
     * @param index индекс элемента.
     */
    fun onSpaceItemClick(index: Int)

    /**
     * Повторный клик на элемент space nav view, который выбран на данный момент.
     * @param index индекс элемента.
     */
    fun onSpaceItemReselected(index: Int)
}

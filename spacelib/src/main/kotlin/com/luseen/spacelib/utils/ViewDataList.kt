package com.luseen.spacelib.utils

import android.os.Bundle
import android.os.Parcelable
import android.view.View

/**
 * Класс, призванный упростить контроль над данными и вьюхой.
 */
class ViewDataList<V : View, D : Parcelable> {
    private val viewList = ArrayList<V>()
    private val dataList = ArrayList<D>()

    /** Размер списка (он одинаковый для списка данных и списка вью). */
    val size: Int get() = dataList.size

    /** Индексы списка (он одинаковые для списка данных и списка вью). */
    val indices: IntRange get() = dataList.indices

    /** Вью по индексу, без данных.*/
    fun getView(index: Int): V = viewList[index]

    /** Данные по индексу, без вью. */
    fun getData(index: Int): D = dataList[index]

    /** Получает все данные, которые есть на данный момент. */
    fun getAllData(): List<D> = dataList.toList() // защита от сайдэффекта при изменении dataList

    /** Добавить данные и вью в концы списков. */
    fun add(view: V, data: D) {
        viewList.add(view)
        dataList.add(data)
    }

    /** @return `true`, если элемент с таким индексом уже есть. */
    fun exists(index: Int): Boolean = index in dataList.indices

    /** @see [Iterable.indexOfFirst] */
    fun indexOfFirst(predicate: (D) -> Boolean): Int =
        dataList.indexOfFirst(predicate)

    /** @see [Iterable.indexOf] */
    fun indexOf(d: D): Int =
        dataList.indexOf(d)

    /** Обновляет список данных, обновляя при этом вью. */
    fun addDataListWithView(newDataList: List<D>, block: (view: V, data: D) -> Unit) {
        dataList.clear()
        dataList += newDataList
        for ((index, view) in viewList.withIndex())
            block(view, dataList[index])
    }

    /** Сохраняет вью для [data] с тем же индексом. */
    fun updateViewForData(view: V, data: D) =
        viewList.add(dataList.indexOf(data), view)

    /** Обновляет данные и вью с соответствующим [index]. */
    fun updateDataWithView(index: Int, block: (view: V, data: D) -> D) {
        dataList[index] = block(viewList[index], dataList[index])
    }

    /** Удаляет данные с индексом [index]. */
    fun removeDataWithView(index: Int, block: (view: V, data: D) -> Unit) =
        block(viewList.removeAt(index), dataList.removeAt(index))

    /** Удаляет все данные и вью, обновляя список вью перед удалением. */
    fun removeDataWithView(block: (viewList: List<V>) -> Unit) {
        block(viewList)
        viewList.clear()
        dataList.clear()
    }

    /** Сохраняет данные в [bundle] по ключу [key]. */
    fun saveData(bundle: Bundle, key: String) =
        bundle.putParcelableArrayList(key, dataList)

    /** Восстанавливает данные из [bundle] и обновляет вью. */
    fun restoreData(bundle: Bundle, key: String, block: (view: V, data: D) -> Unit) =
        addDataListWithView(
            newDataList = bundle.getParcelableArrayList(key)!!,
            block = block
        )
}

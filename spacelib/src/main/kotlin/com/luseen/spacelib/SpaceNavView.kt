package com.luseen.spacelib

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import androidx.core.content.res.use
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.space_button_info_view.view.*
import com.luseen.spacelib.extensions.dpToPx
import com.luseen.spacelib.extensions.getDimensionPixelSizeOrNull
import com.luseen.spacelib.extensions.getDimension
import com.luseen.spacelib.extensions.getColor
import com.luseen.spacelib.extensions.getColorOrNull
import com.luseen.spacelib.extensions.getResourceIdOrNull
import com.luseen.spacelib.utils.ViewDataList
import com.luseen.spacelib.utils.hotInvalidatable
import com.luseen.spacelib.utils.invalidatable
import kotlin.math.roundToInt

/**
 * Bottom navigation view, в центре которого находится большая кнопка.
 * @see <a href="https://github.com/armcha/Space-Navigation-View">Исиходная версия библиотеки на Github</a>.
 */
@SuppressLint("Recycle")
class SpaceNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    /** Ширина контента центральной кнопки. */
    private val centerContentWight = getDimension(R.dimen.center_content_width).toInt()

    /** Высота контента центральной кнопки. */
    private val centerContentHeight = getDimension(R.dimen.center_content_height).toInt()

    /** Ширина бэкграунда центральной кнопки. */
    private val centerBackgroundWight = getDimension(R.dimen.center_background_width).toInt()

    /** Ширина и высота центральной кнопки. */
    private val centerButtonSize = getDimension(R.dimen.center_button_size).toInt()

    /** Отступ снизу от кнопки. */
    private val centerBottomMargin = getDimension(R.dimen.center_button_margin).toInt()

    /** Высота основного контента, на котором будут располагаться все элементы. */
    private val mainContentHeight = getDimension(R.dimen.main_content_height).toInt()

    /** @see SpaceItem */
    private val spaceItemList = ViewDataList<SpaceItemView, SpaceItem>()

    /** Размер иконок, если текста нет. [isIconOnlyMode] == `true`. */
    var iconOnlySize: Int by invalidatable(getDimension(R.dimen.space_item_icon_only_size).toInt())

    /** Размер иконок, если также есть текст. [isIconOnlyMode] == `false`. */
    var iconSize: Int by invalidatable(getDimension(R.dimen.space_item_icon_default_size).toInt())

    /** Размер текста. */
    var spaceItemTextSize: Int by invalidatable(
        getDimension(R.dimen.space_item_text_default_size).toInt()
    )

    /** Бэкграунд всей вьюшки. */
    var spaceBackgroundColor: Int by hotInvalidatable(getColor(R.color.space_default_color)) {
        it.also(::updateBackground)
    }

    /** Цвет центральной кнопки. */
    var centerButtonColor: Int by hotInvalidatable(getColor(R.color.center_button_color)) { color ->
        with(centerButton) {
            rippleColor = color
            backgroundTintList = ColorStateList.valueOf(color)
        }
        color
    }

    /** Иконка центральной кнопки. */
    var centerButtonIcon: Int by hotInvalidatable(R.drawable.ic_plus_center_button) {
        it.also(centerButton::setImageResource)
    }

    /** Если `false`, над центральной кнопкой появится дуга. */
    var isCenterPartLinear: Boolean by hotInvalidatable(true) {
        bezierView.isLinear = it
        it
    }

    /** Цвет активной кнопки. */
    var activeSpaceItemColor: Int by invalidatable(getColor(R.color.default_active_item_color))

    /** Цвет неактивной кнопки. */
    var inactiveSpaceItemColor: Int by invalidatable(getColor(R.color.default_inactive_item_color))

    /** @see SpaceOnClickListener */
    var spaceOnClickListener: SpaceOnClickListener? = null

    /** @see SpaceOnLongClickListener */
    var spaceOnLongClickListener: SpaceOnLongClickListener? = null

    /** Текущая выбранная кнопка. */
    var selectedItem = 0
        private set

    val selectedSpaceItem: SpaceItem
        get() = getItem(selectedItem)

    /** Если `true`, то будет отображаться только текст. */
    var isTextOnlyMode by invalidatable(false)

    /** Если `true`, то будут отображаться только картинки. */
    var isIconOnlyMode by invalidatable(false)

    /** Реализация центральной кнопки. */
    private val centerButton = CenterButton(context).apply {
        scaleType = ImageView.ScaleType.CENTER
        useCompatPadding = false
        compatElevation = 0f
        setOnClickListener {
            setCenterButtonSelected()
        }
        setOnLongClickListener {
            spaceOnLongClickListener?.onCenterButtonLongClick()
            true
        }
    }

    /** Градус поворота картинки центральной кнопки по часовой стрелке. */
    var centerButtonRotation: Float
        get() = centerButton.rotation
        set(value) {
            centerButton.rotation = value
        }

    /** @see BezierView */
    private val bezierView = BezierView(
        context = context,
        bezierWidth = centerContentWight.toFloat(),
        bezierHeight = centerContentHeight - mainContentHeight.toFloat()
    )

    // Основные вьюшки
    private val mainContent = RelativeLayout(context)
    private val centerBackgroundView = LinearLayout(context)
    private val leftContent = LinearLayout(context).apply { id = ViewCompat.generateViewId() }
    private val rightContent = LinearLayout(context)
    private val shadowView = LinearLayout(context).apply {
        setBackgroundColor(getColor(R.color.line_gray))
    }

    /** Добавляет элементы на вьюху. */
    fun setItems(items: List<SpaceItem>) {
        // Выбрасываем исключения, если количество кнопок больше 4 или меньше 2
        if (items.size < MIN_BUTTON_COUNT) {
            throw NullPointerException(
                "Your button count must be greater than 1, current: ${items.size}"
            )
        }
        if (items.size > MAX_BUTTON_COUNT) {
            throw IndexOutOfBoundsException(
                "Your button count maximum can be 4, current: ${items.size}"
            )
        }
        if (items.size % 2 != 0) {
            throw IndexOutOfBoundsException(
                "The number of elements must be even, current: ${items.size}"
            )
        }
        spaceItemList.addDataListWithView(items, ::update)
    }

    /** Получает [SpaceItem] с указанным [index]. */
    fun getItem(index: Int): SpaceItem =
        spaceItemList.getData(index)

    /** Получает все [SpaceItem]. */
    fun getAllItems(): List<SpaceItem> =
        spaceItemList.getAllData()

    init {
        context
            .obtainStyledAttributes(attrs, R.styleable.SpaceNavView)
            .use { typedArray ->
                typedArray
                    .getDimensionPixelSizeOrNull(R.styleable.SpaceNavView_space_item_icon_size)
                    ?.let { iconSize = it }
                typedArray
                    .getDimensionPixelSizeOrNull(R.styleable.SpaceNavView_space_item_icon_only_size)
                    ?.let { iconOnlySize = it }
                typedArray
                    .getDimensionPixelSizeOrNull(R.styleable.SpaceNavView_space_item_text_size)
                    ?.let { spaceItemTextSize = it }
                typedArray
                    .getColorOrNull(R.styleable.SpaceNavView_space_background_color)
                    ?.let { spaceBackgroundColor = it }
                typedArray
                    .getColorOrNull(R.styleable.SpaceNavView_center_button_color)
                    ?.let { centerButtonColor = it }
                typedArray
                    .getColorOrNull(R.styleable.SpaceNavView_active_item_color)
                    ?.let { activeSpaceItemColor = it }
                typedArray
                    .getColorOrNull(R.styleable.SpaceNavView_inactive_item_color)
                    ?.let { inactiveSpaceItemColor = it }
                typedArray
                    .getResourceIdOrNull(R.styleable.SpaceNavView_center_button_icon)
                    ?.let { centerButtonIcon = it }
                isCenterPartLinear = typedArray.getBoolean(
                    R.styleable.SpaceNavView_center_part_linear,
                    false
                )
            }
    }

    /** Инициализация, расстановка и кастомизация вьюшек. */
    private fun initAndAddViewsToMainView(contentWidth: Int) {
        removeAllViews()
        bezierView.addView(
            centerButton,
            LayoutParams(centerButtonSize, centerButtonSize).apply {
                addRule(CENTER_IN_PARENT)
            }
        )
        addView(
            leftContent,
            LayoutParams(contentWidth, mainContentHeight).apply {
                addRule(ALIGN_PARENT_LEFT)
                addRule(LinearLayout.HORIZONTAL)
                addRule(ALIGN_PARENT_BOTTOM)
            }
        )
        addView(
            rightContent,
            LayoutParams(contentWidth, mainContentHeight).apply {
                addRule(ALIGN_PARENT_RIGHT)
                addRule(LinearLayout.HORIZONTAL)
                addRule(ALIGN_PARENT_BOTTOM)
            }
        )
        addView(
            shadowView,
            LayoutParams(matchParent, 1f.dpToPx().roundToInt()).apply {
                addRule(ABOVE, leftContent.id)
            }
        )
        addView(
            centerBackgroundView,
            LayoutParams(centerBackgroundWight, mainContentHeight).apply {
                addRule(CENTER_HORIZONTAL)
                addRule(ALIGN_PARENT_BOTTOM)
            }
        )
        addView(
            bezierView,
            LayoutParams(centerContentWight, centerContentHeight).apply {
                addRule(CENTER_HORIZONTAL)
                addRule(ALIGN_PARENT_BOTTOM)
                bottomMargin = centerBottomMargin
            }
        )
        addView(
            mainContent,
            LayoutParams(matchParent, mainContentHeight).apply {
                addRule(ALIGN_PARENT_BOTTOM)
            }
        )
    }

    private fun updateBackground(color: Int) {
        rightContent.setBackgroundColor(color)
        centerBackgroundView.setBackgroundColor(color)
        leftContent.setBackgroundColor(color)
        bezierView.bezierBackgroundColor = color
    }

    override fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)

        if (newWidth != oldWidth) { // иногда они всё же равны
            val contentWidth = (newWidth - centerContentHeight) / 2
            // Проверяем условия
            if (isTextOnlyMode) require(!isIconOnlyMode)
            // Накидываем основные вьюшки
            initAndAddViewsToMainView(contentWidth)
            // Добавление текущих элементов в левый и правый контенты
            addGivenButtons(leftContent, rightContent, contentWidth)
            // Обновляем вьюшку
            // Заметка: если убрать эту строчку, уйти на другой экран и вернуться
            // обратно, то вьюшка не будет доступна.
            post(::requestLayout)
        }
    }

    override fun removeAllViews() {
        super.removeAllViews()
        bezierView.removeAllViews()
        leftContent.removeAllViews()
        rightContent.removeAllViews()
    }

    private fun addGivenButtons(
        leftContent: LinearLayout,
        rightContent: LinearLayout,
        contentWidth: Int
    ) {
        for (i in spaceItemList.indices) {
            val targetWidth: Int = if (spaceItemList.size > MIN_BUTTON_COUNT) {
                contentWidth / 2
            } else {
                contentWidth
            }
            val spaceItem = spaceItemList.getData(i)
            val containerParams = LayoutParams(targetWidth, mainContentHeight)
            val container = inflate<SpaceItemView>(R.layout.space_button_info_view).apply {
                layoutParams = containerParams
                setOnClickListener { updateSpaceItems(i) }
                setOnLongClickListener {
                    spaceOnLongClickListener?.onSpaceItemLongClick(i)
                    true
                }
            }

            val color = if (i == selectedItem) activeSpaceItemColor else inactiveSpaceItemColor
            val contentTextView = container.contentTextView.apply {
                text = spaceItem.name
                setTextColor(color)
                if (isIconOnlyMode) isVisible = false
            }
            val iconImageView = container.contentIconImageView.apply {
                setImageResource(if (i == selectedItem) spaceItem.activeIcon else spaceItem.defaultIcon)
                if (isTextOnlyMode) isVisible = false
            }

            if (!isTextOnlyMode) {
                val iconSize = if (isIconOnlyMode) iconOnlySize else iconSize
                iconImageView.layoutParams = iconImageView.layoutParams.apply {
                    height = iconSize
                    width = iconSize
                }
            }
            if (!isIconOnlyMode) {
                contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, spaceItemTextSize.toFloat())
            }

            spaceItemList.updateViewForData(container, spaceItem)

            if (spaceItemList.size == MIN_BUTTON_COUNT && leftContent.childCount == 1) {
                rightContent.addView(container, containerParams)
            } else if (spaceItemList.size > MIN_BUTTON_COUNT && leftContent.childCount == 2) {
                rightContent.addView(container, containerParams)
            } else {
                leftContent.addView(container, containerParams)
            }
        }
    }

    private val inflater = LayoutInflater.from(context)

    private inline fun <reified V : View> inflate(@LayoutRes resource: Int): V =
        inflater.inflate(resource, this, false) as V

    /** Был выбран новый элемент с индексом [newSelectedItem]. */
    private fun updateSpaceItems(newSelectedItem: Int) {
        if (newSelectedItem != CENTRAL_BUTTON_INDEX) {
            // Если мы выбрали новый элемент
            if (selectedItem != newSelectedItem) {
                // Изменить активные и неактивные значки и цвет текста
                spaceItemList.updateDataWithView(newSelectedItem) { selectedButton, data ->
                    with(selectedButton) {
                        contentIconImageView.setImageResource(data.activeIcon)
                        contentTextView.setTextColor(activeSpaceItemColor)
                    }
                    data
                }
                if (selectedItem != CENTRAL_BUTTON_INDEX) {
                    spaceItemList.updateDataWithView(selectedItem) { currentSelectedButton, data ->
                        with(currentSelectedButton) {
                            contentIconImageView.setImageResource(data.defaultIcon)
                            contentTextView.setTextColor(inactiveSpaceItemColor)
                        }
                        data
                    }
                }
                selectedItem = newSelectedItem

                spaceOnClickListener?.onSpaceItemClick(newSelectedItem)
            } else {
                spaceOnClickListener?.onSpaceItemReselected(newSelectedItem)
            }
        } else {
            spaceOnClickListener?.onCenterButtonClick()
        }
    }

    /** Изменить текущий выбранный элемент на центральную кнопку. */
    fun setCenterButtonSelected() =
        updateSpaceItems(CENTRAL_BUTTON_INDEX)

    /**
     * Изменить текущий выбранный элемент на данный новый с индексом [indexToChange].
     *
     * Чтобы поменять на центральную кнопку: [setCenterButtonSelected] или
     * ```
     * changeCurrentItem(CENTRAL_BUTTON_INDEX)
     * ```
     */
    fun changeCurrentItem(indexToChange: Int) {
        if (spaceItemList.exists(indexToChange)) {
            updateSpaceItems(indexToChange)
        } else {
            throw ArrayIndexOutOfBoundsException(
                "Please be more careful, we do't have such item: $indexToChange"
            )
        }
    }

    /** @see changeCurrentItem */
    fun changeCurrentItem(spaceItem: SpaceItem) =
        changeCurrentItem(spaceItemList.indexOf(spaceItem))

    /**
     * Изменить иконку [SpaceItem].
     * @param itemIndex позиция изменяемого элемента.
     * @param newIcon новая иконка элемента.
     * @throws ArrayIndexOutOfBoundsException если [itemIndex] меньше 0 или
     * такого элемента не существует в списке.
     */
    fun changeItemIconAtPosition(itemIndex: Int, newIcon: Int) {
        if (spaceItemList.exists(itemIndex)) {
            spaceItemList.updateDataWithView(itemIndex) { view, data ->
                view.contentIconImageView.setImageResource(newIcon)
                data.copy(defaultIcon = newIcon)
            }
        } else {
            throw arrayIndexOutOfBoundsException(itemIndex)
        }
    }

    /**
     * Изменить текст [SpaceItem].
     * @param itemIndex позиция изменяемого элемента.
     * @param newText новый текст элемента.
     * @throws ArrayIndexOutOfBoundsException если [itemIndex] меньше 0 или
     * такого элемента не существует в списке.
     */
    fun changeItemTextAtPosition(itemIndex: Int, newText: String) {
        if (spaceItemList.exists(itemIndex)) {
            spaceItemList.updateDataWithView(itemIndex) { view, data ->
                view.contentTextView.text = newText
                data.copy(name = newText)
            }
        } else {
            throw arrayIndexOutOfBoundsException(itemIndex)
        }
    }

    /** [ArrayIndexOutOfBoundsException] с внятным описанием ошибки. */
    private fun arrayIndexOutOfBoundsException(itemIndex: Int) =
        ArrayIndexOutOfBoundsException(
            """
                Your item index can't be 0 or greater than space item size, 
                your items size is ${spaceItemList.size}, your current index is: $itemIndex.
            """.trimIndent()
        )

    private fun update(view: SpaceItemView, data: SpaceItem) {
        view.contentIconImageView.setImageResource(data.defaultIcon)
        view.contentTextView.text = data.name
    }

    override fun onSaveInstanceState(): Parcelable =
        bundleOf(
            STATE to super.onSaveInstanceState(),
            SELECTED_ITEM_KEY to selectedItem,
            CENTER_BUTTON_ICON_KEY to centerButtonIcon,
            CENTER_BUTTON_COLOR_KEY to centerButtonColor,
            SPACE_BACKGROUND_COLOR_KEY to spaceBackgroundColor,
            SPACE_ITEM_TEXT_SIZE to spaceItemTextSize,
            ACTIVE_SPACE_ITEM_COLOR_KEY to activeSpaceItemColor,
            INACTIVE_SPACE_ITEM_COLOR_KEY to inactiveSpaceItemColor,
            TEXT_ONLY_MODE to isTextOnlyMode,
            ICON_ONLY_MODE to isIconOnlyMode,
            ICON_ONLY_SIZE to iconOnlySize,
            ICON_SIZE to iconSize,
            VISIBILITY to translationY,
            CENTER_PART_LINEAR to isCenterPartLinear
        ).also {
            spaceItemList.saveData(it, SPACE_ITEM_LIST_KEY)
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(
            (state as? Bundle)?.run {
                selectedItem = getInt(SELECTED_ITEM_KEY)
                centerButtonIcon = getInt(CENTER_BUTTON_ICON_KEY)
                centerButtonColor = getInt(CENTER_BUTTON_COLOR_KEY)
                spaceBackgroundColor = getInt(SPACE_BACKGROUND_COLOR_KEY)
                spaceItemTextSize = getInt(SPACE_ITEM_TEXT_SIZE)
                activeSpaceItemColor = getInt(ACTIVE_SPACE_ITEM_COLOR_KEY)
                inactiveSpaceItemColor = getInt(INACTIVE_SPACE_ITEM_COLOR_KEY)
                isTextOnlyMode = getBoolean(TEXT_ONLY_MODE)
                isIconOnlyMode = getBoolean(ICON_ONLY_MODE)
                iconOnlySize = getInt(ICON_ONLY_SIZE)
                iconSize = getInt(ICON_SIZE)
                translationY = getFloat(VISIBILITY)
                isCenterPartLinear = getBoolean(CENTER_PART_LINEAR)
                spaceItemList.restoreData(this, SPACE_ITEM_LIST_KEY, ::update)
                getParcelable<Parcelable>(STATE)
            } ?: state
        )
    }

    companion object {
        const val CENTRAL_BUTTON_INDEX = -1
        private const val matchParent = ViewGroup.LayoutParams.MATCH_PARENT

        private const val STATE = "savedStateKey"
        private const val SELECTED_ITEM_KEY = "selectedItemKey"
        private const val SPACE_ITEM_LIST_KEY = "spaceItemListKey"
        private const val SPACE_ITEM_TEXT_SIZE = "spaseItemTextSize"
        private const val CENTER_BUTTON_ICON_KEY = "centerButtonIconKey"
        private const val CENTER_BUTTON_COLOR_KEY = "centerButtonColorKey"
        private const val SPACE_BACKGROUND_COLOR_KEY = "spaceBackgroundColorKey"
        private const val ACTIVE_SPACE_ITEM_COLOR_KEY = "activeSpaceItemColorKey"
        private const val INACTIVE_SPACE_ITEM_COLOR_KEY = "inactiveSpaceItemColorKey"
        private const val CENTER_PART_LINEAR = "centerPartLinear"
        private const val TEXT_ONLY_MODE = "textOnlyMode"
        private const val ICON_ONLY_MODE = "iconOnlyMode"
        private const val ICON_ONLY_SIZE = "iconOnlySize"
        private const val ICON_SIZE = "iconSize"
        private const val VISIBILITY = "visibilty"

        private const val MAX_BUTTON_COUNT = 4
        private const val MIN_BUTTON_COUNT = 2
    }
}

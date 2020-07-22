package com.luseen.spacelib

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Центральная, самая большая кнопка.
 */
class CenterButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr) {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        /*when (ev.action) {
            MotionEvent.ACTION_DOWN -> downValueAnimator.also(::setParams).start()
            MotionEvent.ACTION_UP -> upValueAnimator.also(::setParams).start()
        }*/
        return super.onTouchEvent(ev)
    }

    /** @see AnimationData *//*
    private val animationData: AnimationData by lazy(LazyThreadSafetyMode.NONE) {
        AnimationData(
            params = layoutParams,
            strokeWidth = 14f.dpToPx().roundToInt()
        )
    }*/

    /** Аниматор для отрисовки нажатия кнопки. *//*
    private val downValueAnimator: ValueAnimator
        get() = ValueAnimator.ofInt(animationData.defaultSize, animationData.smallestSize)

    */
    /** Аниматор для отрисовки отпускания кнопки. *//*
    private val upValueAnimator: ValueAnimator
        get() = ValueAnimator.ofInt(animationData.smallestSize, animationData.defaultSize)

    */
    /** Установка основных параметров для обоих аниматоров. *//*
    private fun setParams(valueAnimator: ValueAnimator) {
        valueAnimator.duration = DEFAULT_DURATION
        valueAnimator.addUpdateListener {
            layoutParams = layoutParams.also {
                it.width = (valueAnimator.animatedValue as? Int)?.also(::println)
                    ?: throw TypeCastException("Необходимо использовать Int для задания ширины.")
            }
        }
    }

    */
    /**
     * Данные для анимации.
     * @param params Параметры, которые мы будем переиспользовать
     * @param defaultSize Стандартный размер кнопки.
     * Будет отправной точкой при нажатии, и конечной точкой при отпускании кнопки.
     * @param smallestSize Наименьший размер кнопки.
     * Будет конечной точки при нажатии, и отправной точкой при отпускании кнопки.
     *//*
    private data class AnimationData(
        val params: ViewGroup.LayoutParams,
        val strokeWidth: Int,
        val defaultSize: Int = params.width,
        val smallestSize: Int = defaultSize - strokeWidth
    )

    private companion object {
        private const val DEFAULT_DURATION: Long = 100
    }*/
}

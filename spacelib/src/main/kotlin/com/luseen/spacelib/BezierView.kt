package com.luseen.spacelib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.luseen.spacelib.extensions.getColor
import com.luseen.spacelib.utils.invalidatable

/**
 * Арка над центральной кнопкой.
 */
@SuppressLint("ViewConstructor")
class BezierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    bezierWidth: Float = 0f,
    bezierHeight: Float = 0f
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val paint: Paint = Paint().apply {
        strokeWidth = 0f
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val path: Path = Path()

    /** Цвет бэкграуда вьюхи. */
    var bezierBackgroundColor: Int? by invalidatable(null)

    /** Ширина арки над центральной кнопкой. */
    var bezierWidth: Float by invalidatable(bezierWidth)

    /** Высота арки над центральной кнопкой. */
    var bezierHeight: Float by invalidatable(bezierHeight)

    /** Если `false`, то над центральной кнопкой будет отображаться дуга. */
    var isLinear: Boolean by invalidatable(true)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setBackgroundColor(getColor(android.R.color.transparent))
    }

    override fun onDraw(canvas: Canvas) {
        bezierBackgroundColor?.let(paint::setColor)
        path.reset()
        path.moveTo(0f, bezierHeight)

        if (!isLinear) {
            // Первая половина
            path.cubicTo(
                bezierWidth / X_CONTROL_POINT,
                bezierHeight,
                bezierWidth / X_CONTROL_POINT,
                0f,
                bezierWidth / X_END_POINT,
                0f
            )
            // Вторая половина
            path.cubicTo(
                bezierWidth / X_CONTROL_POINT * X_SECOND_PART,
                0f,
                bezierWidth / X_CONTROL_POINT * X_SECOND_PART,
                bezierHeight,
                bezierWidth,
                bezierHeight
            )
        }
        canvas.drawPath(path, paint)
    }

    private companion object {
        private const val X_CONTROL_POINT = 4f
        private const val X_END_POINT = 2f
        private const val X_SECOND_PART = 3
    }
}

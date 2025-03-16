package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class DetailChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Data(
        val day: Int,
        val sumAmount: Int,
        val category: String
    )

    private var list: List<Data> = listOf()
    private val categoryColors = mutableMapOf<String, Int>()
    private var maxSum = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 10f
    }
    private val path = Path()

    init {
        if (isInEditMode) {
            setData(
                listOf(
                    Data(day = 20, sumAmount = 1580, category = "Продукты"),
                    Data(day = 22, sumAmount = 499, category = "Здоровье"),
                    Data(day = 12, sumAmount = 129, category = "Продукты"),
                    Data(day = 23, sumAmount = 4541, category = "Кафе и рестораны"),
                    Data(day = 20, sumAmount = 1600, category = "Алкоголь"),
                    Data(day = 15, sumAmount = 1841, category = "Доставка еды"),
                    Data(day = 19, sumAmount = 469, category = "Транспорт"),
                    Data(day = 20, sumAmount = 4000, category = "Здоровье"),
                    Data(day = 16, sumAmount = 809, category = "Продукты"),
                    Data(day = 13, sumAmount = 1000, category = "Спорт"),
                    Data(day = 21, sumAmount = 389, category = "Транспорт")
                )
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        var width = 0
        when (wMode) {
            MeasureSpec.EXACTLY -> width = wSize
            MeasureSpec.AT_MOST -> width = min(10f * list.size, wSize.toFloat()).toInt()
            MeasureSpec.UNSPECIFIED -> width = (10f * list.size).toInt()
        }

        var height = 0
        when (hMode) {
            MeasureSpec.EXACTLY -> height = hSize
            MeasureSpec.AT_MOST -> height = min(300, hSize)
            MeasureSpec.UNSPECIFIED -> height = min(300, hSize)

        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAxes(canvas)
        drawChart(canvas)
    }

    fun setData(list: List<Data>) {
        this.list = list
        assignColors()
        maxSum = this.list.maxOfOrNull { it.sumAmount } ?: 1
        invalidate()
    }

    private fun assignColors() {
        var colorIndex = 0
        list.map { it.category }.distinct().forEach {
            categoryColors[it] = colors[colorIndex % colors.size]
            colorIndex++
        }
    }

    private fun drawAxes(canvas: Canvas) {
        val padding = 90f
        val width = width.toFloat()
        val height = height.toFloat()

        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)
        canvas.drawLine(padding, height - padding, padding, padding, axisPaint)

        for (i in 1..30 step 2) {
            val x = padding + (width - 2 * padding) * i / 30
            canvas.drawText(i.toString(), x, height - 40f, textPaint)
        }

        for (i in 0..5) {
            val y = height - padding - (height - 2 * padding) * i / 5
            val sumValue = (maxSum * i / 5).toString()
            canvas.drawText(sumValue, 20f, y, textPaint)
        }
    }

    private fun drawChart(canvas: Canvas) {
        val padding = 80f
        val width = width.toFloat()
        val height = height.toFloat()

        val pointsByCategory = list.groupBy { it.category }

        pointsByCategory.forEach { (category, points) ->
            paint.color = categoryColors[category] ?: Color.BLACK
            pointPaint.color = categoryColors[category] ?: Color.BLACK
            points.sortedBy { it.day }.forEachIndexed { index, data ->
                val x = padding + (width - 2 * padding) * data.day / 30
                val y = height - padding - (height - 2 * padding) * data.sumAmount / maxSum

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                canvas.drawCircle(x, y, 10f, pointPaint)
            }

            canvas.drawPath(path, paint)
        }
    }

    public override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
        }
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? Bundle)?.let {
            super.onRestoreInstanceState(it.getParcelable("superState"))
        }
    }

}
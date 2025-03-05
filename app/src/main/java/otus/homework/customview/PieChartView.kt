package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    private data class PieSlice(val category: String, val startAngle: Float, val sweepAngle: Float)

    private var payloads: List<Payload> = listOf()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 10f
        style = Paint.Style.STROKE
        color = Color.BLACK
    }
    private val rectF = RectF()
    private val categoryColors = mutableMapOf<String, Int>()
    private var totalAmount = 0f
    private var onCategoryClickListener: ((String) -> Unit)? = null
    private var pieSlices = mutableListOf<PieSlice>()
    private val wrapContentWidthPx: Float
    private var centerCircleRadius = 0f
    private var selectedCategory = ""

    init {
        if (isInEditMode) {
            setPayloads(
                listOf(
                    Payload(1, "Азбука Вкуса", 1580, "Продукты", 1623318531),
                    Payload(2, "Ригла", 499, "Здоровье", 1623322251),
                    Payload(3, "Пятерочка", 700, "Алкоголь", 1623322371)
                )
            )
        }
        wrapContentWidthPx = resources.displayMetrics.density * 200
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        centerCircleRadius = wSize * 0.4f
        var width = 0
        when (wMode) {
            MeasureSpec.EXACTLY -> width = wSize
            MeasureSpec.AT_MOST -> width = minOf(wrapContentWidthPx, wSize.toFloat()).toInt()
            MeasureSpec.UNSPECIFIED -> width = wrapContentWidthPx.toInt()
        }

        var height = 0
        when (hMode) {
            MeasureSpec.EXACTLY -> height = hSize
            MeasureSpec.AT_MOST -> height = minOf(wrapContentWidthPx, hSize.toFloat()).toInt()
            MeasureSpec.UNSPECIFIED -> height = wrapContentWidthPx.toInt()
        }

        val minWH = minOf(width, height)
        setMeasuredDimension(minWH, minWH)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())

        pieSlices.forEach { slice ->
            paint.color = categoryColors[slice.category] ?: Color.GRAY
            canvas.drawArc(rectF, slice.startAngle, slice.sweepAngle, true, paint)
            if (slice.category == selectedCategory) {
                canvas.drawArc(rectF, slice.startAngle, slice.sweepAngle, true, paintBorder)
            }
        }
        paint.color = Color.WHITE
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), centerCircleRadius, paint)
    }

    fun setPayloads(payloads: List<Payload>) {
        this.payloads = payloads
        totalAmount = payloads.sumOf { it.amount }.toFloat()
        prepareSlices()
        invalidate()
    }

    private fun prepareSlices() {
        categoryColors.clear()
        pieSlices.clear()
        var startAngle = 0f
        var colorIndex = 0

        val categoryAmounts = payloads.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }

        categoryAmounts.forEach { (category, amount) ->
            if (!categoryColors.containsKey(category)) {
                categoryColors[category] = colors[colorIndex % colors.size]
                colorIndex++
            }
            val sweepAngle = (amount / totalAmount) * 360f
            pieSlices.add(PieSlice(category, startAngle, sweepAngle))
            startAngle += sweepAngle
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val centerX = width / 2f
            val centerY = height / 2f
            val dx = event.x - centerX
            val dy = event.y - centerY
            val touchDistance = sqrt(dx * dx + dy * dy)

            if (touchDistance < centerCircleRadius) return false

            getClickedCategory(event.x, event.y)?.let {
                selectedCategory = it
                invalidate()
                onCategoryClickListener?.invoke(it)
            }
        }
        return super.onTouchEvent(event)
    }


    private fun getClickedCategory(x: Float, y: Float): String? {
        val centerX = width / 2f
        val centerY = height / 2f

        val dx = x - centerX
        val dy = y - centerY
        val angle = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 360) % 360

        return pieSlices.firstOrNull { angle in it.startAngle..(it.startAngle + it.sweepAngle) }?.category
    }

    fun setOnCategoryClickListener(listener: (String) -> Unit) {
        onCategoryClickListener = listener
    }

    public override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putString("selectedCategory", selectedCategory)
        }
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? Bundle)?.let {
            selectedCategory = it.getString("selectedCategory", "")
            super.onRestoreInstanceState(it.getParcelable("superState"))
        }
    }
}

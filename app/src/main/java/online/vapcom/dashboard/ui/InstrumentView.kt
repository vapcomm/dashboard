/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import online.vapcom.dashboard.R
import kotlin.math.cos
import kotlin.math.sin

private const val TAG = "InstrView."

/**
 * Базовый класс аналоговых приборов
 */
abstract class InstrumentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // сохраняем паддинги, чтобы они не пересчитывались при расчётах
    private var padLeft = 0
    private var padRight = 0
    private var padTop = 0
    private var padBottom = 0

    // размеры внутреннего контента со всеми элементами
    private var contentWidth = 0
    protected var contentHeight = 0

    // центр прибора, вокруг которого строятся шкалы и вращается стрелка
    protected var centerX = 0F
    protected var centerY = 0F

    private var rimThickness = 2F   // толщина обода в px    //TODO: передавать через атрибуты
    private val rimOffset = 10      // смещение штрихов относительно обода
    protected var rimRadius = 0F    // радиус обода
    private var rimPaint: Paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = rimThickness
    }

    var rimColor: Int               // цвет обода
        get() = rimPaint.color
        set(value) { rimPaint.color = value }

    protected val scaleAngle = 240    // диапазон всей шкалы в градусах

    protected var bigMarksCount = 0       // количество больших отметок
    protected var bigMarksAngleDelta = 0F // угол в градусах между большими отметками
    protected var bigMarks: FloatArray = floatArrayOf()   // массив четвёрок координат больших отметок для Canvas.drawLines()

    // paint больших засечек
    private val bigMarkPaint: Paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 6F
        strokeCap = Paint.Cap.ROUND
    }

    var bigMarksColor: Int          // цвет больших засечек
        get() = bigMarkPaint.color
        set(value) { bigMarkPaint.color = value }

    var bigMarksWidth: Float        // толщина линии больших засечек
        get() = bigMarkPaint.strokeWidth
        set(value) { bigMarkPaint.strokeWidth = value }

    protected var smallMarksCount = 0     // количество малых отметок
    protected var smallMarks: FloatArray = floatArrayOf()   // массив четвёрок координат малых отметок

    // paint маленьких засечек
    private val smallMarkPaint: Paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        color = Color.LTGRAY
        strokeWidth = 2F
    }

    var smallMarksColor: Int        // цвет маленьких засечек
        get() = smallMarkPaint.color
        set(value) { smallMarkPaint.color = value }

    var smallMarksWidth: Float      // толщина линий маленьких засечек
        get() = smallMarkPaint.strokeWidth
        set(value) { smallMarkPaint.strokeWidth = value }

    protected var marksNumbers: Array<String> = emptyArray()            // значения чисел скоростей
    protected var marksNumbersCoordinates : FloatArray = floatArrayOf() // пары координат чисел скоростей

    // paint отрисовки текста чисел
    private var marksNumbersPaint: TextPaint = TextPaint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        textAlignment
        textSize = 40F
        color = Color.WHITE
    }

    // стрелка, заливка и линия окантовки отдельно
    protected val arrow = Arrow()
    private val arrowFillPaint: Paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
        color = Color.rgb(0xD0, 0, 0)
    }
    private val arrowStrokePaint: Paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 2F
    }

    @SuppressLint("CustomViewStyleable", "PrivateResource")
    protected fun parseCommonAttributes(a: TypedArray) {
        bigMarksColor = a.getColor(R.styleable.InstrumentView_bigMarksColor, Color.WHITE)
        smallMarksColor = a.getColor(R.styleable.InstrumentView_smallMarksColor, Color.LTGRAY)
        rimColor = a.getColor(R.styleable.InstrumentView_rimColor, Color.WHITE)

        bigMarksWidth = a.getDimension(R.styleable.InstrumentView_bigMarksWidth, 6F)
        smallMarksWidth = a.getDimension(R.styleable.InstrumentView_smallMarksWidth, 2F)

        // атрибуты чисел, можно менять цвет и fontFamily
        val textAppearance = a.getResourceId(
            R.styleable.InstrumentView_android_textAppearance,
            R.style.TextAppearance_MaterialComponents_Body1
        )

        val ta = context.obtainStyledAttributes(textAppearance, R.styleable.TextAppearance)
        val font = ta.getString(R.styleable.TextAppearance_fontFamily)
        font?.let {
            marksNumbersPaint.typeface = Typeface.create(font, Typeface.NORMAL)
        }
        marksNumbersPaint.color =
            ta.getColor(R.styleable.TextAppearance_android_textColor, Color.WHITE)

        ta.recycle()
    }



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d(TAG, "##### onSizeChanged: ${w}x$h, old: ${oldw}x$oldh")

        setupScaleAndArrow(w, h)
        invalidate()
    }

    /**
     * Определяет размеры элементов в звисимости от заданных размеров всего виджета
     */
    private fun setupScaleAndArrow(w: Int, h: Int) {
        padLeft = paddingLeft
        padTop = paddingTop
        padRight = paddingRight
        padBottom = paddingBottom

        Log.d(TAG, "##### pL:$padLeft, pT:$padTop, pR:$padRight, pB:$padBottom")

        contentWidth = w - paddingLeft - paddingRight
        contentHeight = h - paddingTop - paddingBottom

        centerX = padLeft + contentWidth/2F
        centerY = padTop + contentHeight/2F

        Log.d(TAG, "##### cW:$contentWidth, cH:$contentHeight, cx:$centerX, cy:$centerY")

        // внешний обод
        rimRadius = (kotlin.math.min(contentWidth, contentHeight) - rimThickness)/2F
        Log.d(TAG, "##### rimR:$rimRadius")

        arrow.length = rimRadius * 0.9F
        arrow.setAngle(startAngle(), centerX, centerY, contentHeight)

        setupMarks()
    }

    /**
     * Вычисляет координаты засечек
     */
    private fun setupMarks() {
        setupBigMarks()
        setupSmallMarks()
        setupMarksNumbers()
    }

    /**
     * Угол нулевой отметки
     * sa - (sa - 180)/2
     */
    protected fun startAngle() = scaleAngle/2.0 + 90.0

    /**
     *  Заполняет массив координат линий больших отметок
     */
    private fun setupBigMarks() {
        val bigMarkLength = rimRadius/8    // длина большого штриха

        var angleDeg = startAngle()
        for(m in 0 until bigMarksCount) {
            setupMark(m, angleDeg, bigMarkLength, bigMarks)
            angleDeg -= bigMarksAngleDelta
        }
    }

    /**
     *  Заполняет массив координат линий малых отметок
     */
    protected open fun setupSmallMarks() {
        val smallMarkLength = rimRadius/12    // длина малого штриха

        var angleDeg = startAngle() - bigMarksAngleDelta/2.0
        for(m in 0 until smallMarksCount) {
            setupMark(m, angleDeg, smallMarkLength, smallMarks)
            angleDeg -= bigMarksAngleDelta
        }
    }

    /**
     * Расчитывает координаты одной отметки
     */
    protected fun setupMark(markNumber: Int, angleDeg: Double, markLength: Float, marks: FloatArray) {
        val i = markNumber * 4
        val angle = Math.toRadians(angleDeg)
        val cosa = cos(angle).toFloat()
        val sina = sin(angle).toFloat()

        marks[i] = centerX + (rimRadius - rimOffset - markLength) * cosa
        marks[i + 1] = contentHeight - (centerY + (rimRadius - rimOffset - markLength) * sina)
        marks[i + 2] = centerX + (rimRadius - rimOffset) * cosa
        marks[i + 3] = contentHeight - (centerY + (rimRadius - rimOffset) * sina)
    }

    /**
     * Заполняет массив координат чисел
     */
    private fun setupMarksNumbers() {
        val numberOffset = rimRadius/3.8F   // смещение центра текста относительно внешнего кольца
        val textSize = rimRadius/8          // размер текста делаем относительным размера виджета

        marksNumbersPaint.textSize = textSize

        // смещение по вертикали на центр текста, см. https://stackoverflow.com/a/36321422/10085047
        val dy = (marksNumbersPaint.descent() + marksNumbersPaint.ascent()) / 2f

        var angleDeg = startAngle()
        for(m in 0 until bigMarksCount) {
            val i = m * 2
            val angle = Math.toRadians(angleDeg)
            val cosa = cos(angle).toFloat()
            val sina = sin(angle).toFloat()

            marksNumbersCoordinates[i] = centerX + (rimRadius - rimOffset - numberOffset) * cosa
            marksNumbersCoordinates[i + 1] = contentHeight - (centerY + (rimRadius - rimOffset - numberOffset) * sina) - dy
            angleDeg -= bigMarksAngleDelta
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // внешний обод
        canvas.drawCircle(centerX, centerY, rimRadius, rimPaint)

        // отметки
        canvas.drawLines(bigMarks, bigMarkPaint)
        canvas.drawLines(smallMarks, smallMarkPaint)

        // стрелку рисуем под числами, чтобы она их не перекрывала и числа всегда легко читались
        //TODO: флаг через атрибуты порядка отрисовки стрелки и чисел (classic, below)
        canvas.drawPath(arrow.path, arrowFillPaint)
        canvas.drawPath(arrow.path, arrowStrokePaint)

        // числа
        for(m in 0 until bigMarksCount) {
            val i = m * 2
            canvas.drawText(marksNumbers[m], marksNumbersCoordinates[i], marksNumbersCoordinates[i + 1], marksNumbersPaint)
        }

    }

    fun moveOnDeltaX(deltaX: Float) {
        x += deltaX
    }

}
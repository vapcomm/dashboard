package online.vapcom.dashboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import online.vapcom.dashboard.R

//private const val TAG = "TachomView"

/**
 * Отображение стрелочного тахометра
 */
class TachometerView : InstrumentView {

    companion object {
        private const val DEFAULT_MAX_RPM = 7000    // максимальные обороты по умолчанию
        private const val RPM_STEP = 1000           // шаг отображения оборотов
    }

    private var maxRPM = DEFAULT_MAX_RPM    // максимальные отображаемые обороты, должны быть кратна RPM_STEP
    var rpm = 0     // текущие обороты
        set(value) {
            arrow.setAngle(rpmAngle(value), centerX, centerY, contentHeight)
            field = value
            invalidate()
        }


    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    @SuppressLint("CustomViewStyleable", "PrivateResource")
    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // атрибуты из XML
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.InstrumentView, defStyle, 0)

            parseCommonAttributes(a)

            maxRPM = a.getInteger(R.styleable.InstrumentView_maxRPM, DEFAULT_MAX_RPM)
            if(maxRPM % RPM_STEP != 0)
                throw IllegalArgumentException("maxRPM should be a multiple of $RPM_STEP, but was: $maxRPM")

            rpm = a.getInteger(R.styleable.InstrumentView_rpm, 0)

            a.recycle()
        }

        initMarksData()
    }

    private fun initMarksData() {
        bigMarksCount = maxRPM/RPM_STEP + 1     // количество больших отметок
        bigMarksAngleDelta = scaleAngle.toFloat() / (bigMarksCount - 1)     // угол в градусах между большими отметками
        bigMarks = FloatArray(bigMarksCount*4)   // массив четвёрок координат больших отметок для Canvas.drawLines()

        smallMarksCount = (bigMarksCount - 1) * 4     // количество малых отметок
        smallMarks = FloatArray(smallMarksCount*4)   // массив четвёрок координат малых отметок

        marksNumbers = Array(bigMarksCount) { it.toString() }   // значения чисел оборотов
        marksNumbersCoordinates = FloatArray(bigMarksCount * 2)     // пары координат чисел скоростей
    }

    /**
     *  Заполняет массив координат линий малых отметок
     */
    override fun setupSmallMarks() {
        val smallMarkLength = rimRadius/12    // длина малого штриха
        val angleDelta = bigMarksAngleDelta/5.0

        var angleDeg = startAngle()
        for(m in 0 until smallMarksCount) {
            angleDeg -= if(m % 4 == 0 && m != 0) angleDelta * 2   // прыгаем мимо больших штрихов
            else angleDelta

            setupMark(m, angleDeg, smallMarkLength, smallMarks)
        }
    }


    /**
     * Возвращает угол стрелки для заданных оборотов
     */
    private fun rpmAngle(rpm: Int): Double {
        val r = if(rpm > maxRPM) maxRPM
                else if(rpm < 0) 0 else rpm
        return  startAngle() - (scaleAngle.toDouble() / maxRPM) * r
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // сразу задвигаем за экран
        x = w.toFloat()
    }

}

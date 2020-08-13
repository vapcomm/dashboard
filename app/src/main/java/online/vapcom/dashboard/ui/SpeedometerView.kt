package online.vapcom.dashboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import online.vapcom.dashboard.R

//private const val TAG = "SpeedoView"

/**
 * Отображение стрелочного спидометра
 */
class SpeedometerView : InstrumentView {

    companion object {
        private const val DEFAULT_MAX_SPEED = 200   // максимальная скорость по умолчанию
        private const val SPEED_STEP = 20           // шаг скорости между большими отметками в км/ч
    }

    private var maxSpeed = DEFAULT_MAX_SPEED    // максимальная отображаемая скорость, должна быть кратна SPEED_STEP

    var speed = 0   // текущая отображаемая стрелкой скорость
        set(value) {
            arrow.setAngle(speedAngle(value), centerX, centerY, contentHeight)
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

            maxSpeed = a.getInteger(R.styleable.InstrumentView_maxSpeed, DEFAULT_MAX_SPEED)
            if(maxSpeed % SPEED_STEP != 0)
                throw IllegalArgumentException("maxSpeed should be a multiple of $SPEED_STEP km/h, but was: $maxSpeed")

            speed = a.getInteger(R.styleable.InstrumentView_speed, 0)

            a.recycle()
        }

        initMarksData()
    }


    private fun initMarksData() {
        bigMarksCount = maxSpeed/SPEED_STEP + 1     // количество больших отметок
        bigMarksAngleDelta = scaleAngle.toFloat() / (bigMarksCount - 1)     // угол в градусах между большими отметками
        bigMarks = FloatArray(bigMarksCount*4)   // массив четвёрок координат больших отметок для Canvas.drawLines()

        smallMarksCount = bigMarksCount - 1     // количество малых отметок
        smallMarks = FloatArray(smallMarksCount*4)   // массив четвёрок координат малых отметок

        marksNumbers = Array(bigMarksCount) { (it * SPEED_STEP).toString() }   // значения чисел скоростей
        marksNumbersCoordinates = FloatArray(bigMarksCount * 2)     // пары координат чисел скоростей
    }

    /**
     * Возвращает угол стрелки для заданной скорости
     */
    private fun speedAngle(speed: Int): Double {
        val spd = if(speed > maxSpeed) maxSpeed
                  else if(speed < 0) 0 else speed
        return startAngle() - (scaleAngle.toDouble() / maxSpeed) * spd
    }

}

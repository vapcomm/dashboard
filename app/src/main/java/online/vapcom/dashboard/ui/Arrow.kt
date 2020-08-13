/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard.ui

import android.graphics.Path
import kotlin.math.cos
import kotlin.math.sin

/**
 * Стрелка прибора
 */
class Arrow {

    var length = 0F     // длина стрелки от центра вращения
    var tailRadius = 5F // радиус хвоста

    val path = Path()   // путь, который рисуется в onDraw


    /**
     * Расчитывает координаты точек стрелки и обновляет path
     */
    fun setAngle(angleDegrees: Double, cx: Float, cy: Float, parentHeight: Int) {

        val tipAngle = Math.toRadians(angleDegrees).toFloat()
        val tipX = cx + length * cos(tipAngle)
        val tipY = parentHeight - (cy + length * sin(tipAngle))

        val rightAngle = Math.toRadians(angleDegrees - 120).toFloat()
        val rightX = cx + tailRadius * cos(rightAngle)
        val rightY = parentHeight - (cy + tailRadius * sin(rightAngle))

        val leftAngle = Math.toRadians(angleDegrees + 120).toFloat()
        val leftX = cx + tailRadius * cos(leftAngle)
        val leftY = parentHeight - (cy + tailRadius * sin(leftAngle))

        path.rewind()
        path.moveTo(cx, cy)
        path.lineTo(leftX, leftY)
        path.lineTo(tipX, tipY)
        path.lineTo(rightX, rightY)
        path.lineTo(cx, cy)
        path.close()
    }

}

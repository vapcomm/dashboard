/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard.ui

import androidx.databinding.BindingAdapter

/**
 * Адаптер скорости
 */
@BindingAdapter("speed")
fun setSpeed(speedometer: SpeedometerView, speed: Int) {
    speedometer.speed = speed
}

/**
 * Адаптер оборотов
 */
@BindingAdapter("rpm")
fun setRPM(tachometer: TachometerView, rpm: Int) {
    tachometer.rpm = rpm
}

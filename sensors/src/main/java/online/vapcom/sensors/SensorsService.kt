/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.sensors

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Сервис эмуляции получения показаний с датчиков скорости и оборотов двигателя.
 * Приложение забирает из него данные через AIDL-интерфейс
 */
class SensorsService : Service() {

    companion object {
        const val TAG = "SensorsSrv"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "~~ onCreate:")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "~~ onDestroy:")
    }

    private val binder = object : SensorsAidlInterface.Stub() {
        override fun getSpeed(): Int {
            val rpm = calculateRPM()
            return 260 * rpm / 7000 // привязываем скорость к оборотам, и считаем что у нас нет коробки передач
        }

        override fun getRPM(): Int {
            return calculateRPM()
        }

    }

    private val upToRPM = 3000

    /**
     * Эмулирует плавный разгон и плавное торможение за 6 секундный период
     */
    fun calculateRPM() : Int {
        val time = (System.currentTimeMillis() % 6000).toInt()
        val rpm = if(time < 4000) // первые 4 секунды разгон
                    time / 4000f * upToRPM
                  else (6000 - time) / 2000f * upToRPM   // за 2 секунды торможение до 0

        return rpm.toInt()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "~~ onBind: intent: $intent")

        return binder
    }


}

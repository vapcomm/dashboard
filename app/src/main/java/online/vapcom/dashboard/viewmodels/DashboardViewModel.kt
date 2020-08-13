/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.launch
import online.vapcom.dashboard.data.RepoReply
import online.vapcom.dashboard.data.SensorsRepository

private const val TAG = "DashbrdVM."

/**
 * Модель показаний спидометра и тахометра
 */
class DashboardViewModel(private val sensorsRepository: SensorsRepository) : BaseViewModel() {

    companion object {
        val FACTORY = singleArgViewModelFactory(::DashboardViewModel)
    }

    val speed: LiveData<Int> = sensorsRepository.getSpeed()
    val rpm: LiveData<Int> = sensorsRepository.getRPM()

    //TODO: isSpeedValid, isRPMValid, чтобы визуально отображать проблемы с получением данных,
    //      например, подсвечивать красным обод прибора через байндинг

    fun startSensorsDisplay() {
        Log.i(TAG, "++++ startSensorsDisplay:")

        uiScope.launch {
            val reply = sensorsRepository.start()
            when(reply) {
                is RepoReply.Success -> {}
                is RepoReply.Error -> {
                    Log.e(TAG, "Error: ${reply.error}")
                    //TODO: в зависимости от кода ошибки здесь делается повтор start(),
                    //      или какие-либо действия по отображению ошибки в UI с её описанием
                }
            }
        }
    }

}

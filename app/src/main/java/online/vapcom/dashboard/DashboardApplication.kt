/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard

import android.app.Application
import online.vapcom.dashboard.data.SensorsRepository

//private const val TAG = "DashbrdApp"

/**
 * Главный класс приложения, нужен для создания ServiceLocator
 */
class DashboardApplication: Application() {

    val sensorsRepository: SensorsRepository
        get() = ServiceLocator.provideSensorsRepository(applicationContext)

}

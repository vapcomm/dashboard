/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard

import android.content.Context
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.runBlocking
import online.vapcom.dashboard.data.SensorsRepository
import online.vapcom.dashboard.data.SensorsRepositoryImpl

/**
 * Синглетон для хранения сервисов в приложении.
 *
 * NOTE: это самый простой и эффективный способ DI без использования сторонних библиотек.
 *       В данном приложении нет модульных тестов UI, поэтому можно целиком обойтись и без DI, и она оставлена только для примера.
 */
object ServiceLocator {

    @Volatile
    var sensorsRepository: SensorsRepository? = null
        @VisibleForTesting set

    /**
     * Возвращает репозиторий словаря
     */
    fun provideSensorsRepository(context: Context): SensorsRepository {
        synchronized(this) {
            return sensorsRepository ?:
                SensorsRepositoryImpl(context).also { sensorsRepository = it }
        }
    }

    private val lock = Any()    // для блокировки сброса

    /**
     * Сброс всех ресурсов, только для тестов
     */
    @VisibleForTesting
    fun reset() {
        synchronized(lock) {
            runBlocking {
                // здесь чистить DAO и кэши
            }

            sensorsRepository = null
        }
    }
}
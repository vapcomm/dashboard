/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import online.vapcom.sensors.SensorsAidlInterface

private const val TAG = "SensRepo.."
private const val ERROR_MODULE_NUM = 10     // номер модуля в кодах ошибок, см. ErrorDescription
// последний использованный код ошибки: 13

/**
 * Рабочая реализация репозитория датчиков
 */
class SensorsRepositoryImpl(private val context: Context): SensorsRepository {

    private var sensorsService: SensorsAidlInterface? = null

    // соедиение с AIDL-сервисом датчиков
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.w(TAG, ">>> onServiceConnected: name: $name")
            sensorsService = SensorsAidlInterface.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, ">>> onServiceDisconnected: name: $name")
            sensorsService = null
        }

    }

    private val speed: MutableLiveData<Int> = MutableLiveData(0)
    override fun getSpeed(): LiveData<Int> = speed

    private val rpm: MutableLiveData<Int> = MutableLiveData(0)
    override fun getRPM(): LiveData<Int> = rpm

    override suspend fun start() : RepoReply = withContext(Dispatchers.IO) {
        Log.w(TAG, ">>> start:")

        try {
            val intent = Intent()
            intent.setClassName("online.vapcom.sensors", "online.vapcom.sensors.SensorsService")
            if(!context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) {
                RepoReply.Error(ErrorDescription(true, errCode(ERROR_MODULE_NUM, 13),
                    "Unable to bind to Sensors service"))
            }
            Log.w(TAG, ">>> start: bindService OK")
        } catch (ex: Exception) {
            return@withContext RepoReply.Error(ErrorDescription(true, errCode(ERROR_MODULE_NUM, 10),
                "Unable to connect to Sensors service", ex.message ?: "", stackTraceToString(ex)))
        }

        //NOTE: это простой вариант цикла получения данных. Более правильно с архитектурной точки зрения передавать наверх
        //      индикацию из onServiceConnected(), о том, что подключились к сервису датчиков и можно забирать из него данные.
        //      Этот цикл тогда будет в другой suspend-функции типа loadData().
        while (true) {
            try {
                //TODO: делать запросы скорости и оборотов в разных корутинах/нитях
                speed.postValue(sensorsService?.speed ?: -1)    // -1 - общая ошибка получения данных
                rpm.postValue(sensorsService?.rpm ?: -1)

                delay(50)
            } catch (ex: Exception) {
                return@withContext RepoReply.Error(ErrorDescription(false, errCode(ERROR_MODULE_NUM, 11),
                    "Error: unable to get sensors data", ex.message ?: "", stackTraceToString(ex)))
            }
        }

        @Suppress("UNREACHABLE_CODE")
        RepoReply.Error(ErrorDescription(false, errCode(ERROR_MODULE_NUM, 12),
            "Error: connection with Sensors service was lost"))
    }

}

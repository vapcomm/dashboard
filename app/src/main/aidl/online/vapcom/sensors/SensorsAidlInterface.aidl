// SensorsAidlInterface.aidl
package online.vapcom.sensors;

// AIDL-интерфейс получения данных с сервиса датчиков
interface SensorsAidlInterface {
    int getSpeed() = 1;
    int getRPM() = 2;
}

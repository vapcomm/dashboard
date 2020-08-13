// SensorsAidlInterface.aidl
package online.vapcom.sensors;

// AIDL-интерфейс получения данных с сервиса датчиков
interface SensorsAidlInterface {
    int getSpeed() = 1; //NOTE: числа - это номера методов для возможности версонифицирования интерфейса и замены их в будущем на другие реализации
    int getRPM() = 2;
}

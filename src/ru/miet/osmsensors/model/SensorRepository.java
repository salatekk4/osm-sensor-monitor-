package ru.miet.osmsensors.model;

/**
 * Интерфейс репозитория датчиков.
 * Определяет контракт для любого хранилища объектов Sensor.
 */
public interface SensorRepository {

    /**
     * Добавить датчик в хранилище.
     * @return true — добавлен, false — s == null
     * @throws DuplicateSensorException если ID уже занят
     */
    boolean add(Sensor s);

    /**
     * Найти датчик по идентификатору.
     * @throws SensorNotFoundException если датчика нет
     */
    Sensor findById(int id);

    /**
     * Удалить датчик по идентификатору.
     * @return удалённый датчик
     * @throws SensorNotFoundException если датчика нет
     */
    Sensor removeById(int id);

    /**
     * Получить все датчики указанного типа ("T", "CO2", "N2").
     * @return новый массив; пустой, если совпадений нет
     */
    Sensor[] findByType(String type);

    /**
     * Получить все добавленные датчики.
     */
    Sensor[] getAll();

    /**
     * Текущее количество датчиков в хранилище.
     */
    int getCount();

    /**
     * Лимита больше нет.
     * @deprecated оставлен для совместимости, вернёт Integer.MAX_VALUE
     */
    @Deprecated
    int getCapacity();

    /**
     * Получить все датчики, у которых isAlarm() == true.
     * @return новый массив; пустой, если аварийных нет
     */
    Sensor[] findAlarmSensors();
}

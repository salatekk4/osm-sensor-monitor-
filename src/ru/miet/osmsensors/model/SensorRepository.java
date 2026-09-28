package ru.miet.osmsensors.model;

/**
 * Интерфейс репозитория датчиков.
 * Определяет контракт для любого хранилища объектов Sensor.
 */
public interface SensorRepository {

    /**
     * Добавить датчик в хранилище.
     * @return true — добавлен успешно, false — хранилище заполнено или s == null
     */
    boolean add(Sensor s);

    /**
     * Найти датчик по идентификатору.
     * @return найденный Sensor или null
     */
    Sensor findById(int id);

    /**
     * Получить все датчики указанного типа ("T", "CO2", "N2").
     * @return новый массив точной длины; пустой массив, если совпадений нет
     */
    Sensor[] findByType(String type);

    /**
     * Получить все добавленные датчики без пустых ячеек.
     */
    Sensor[] getAll();

    /**
     * Текущее количество датчиков в хранилище.
     */
    int getCount();

    /**
     * Максимальная ёмкость хранилища.
     */
    int getCapacity();

    /**
     * Получить все датчики, у которых isAlarm() == true.
     * @return новый массив точной длины; пустой массив, если аварийных нет
     */
    Sensor[] findAlarmSensors();
}

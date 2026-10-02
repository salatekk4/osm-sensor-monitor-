package ru.miet.osmsensors.model;

/**
 * Интерфейс репозитория датчиков.
 * Старые методы (add, findById) не бросают исключений и сохраняют прежнее поведение.
 * Новые методы addUnique и getById сообщают об ошибках через исключения.
 */
public interface SensorRepository {

    /** @return true - добавлен; false - хранилище заполнено или s == null */
    boolean add(Sensor s);

    /** @return найденный Sensor или null */
    Sensor findById(int id);

    /** @return новый массив точной длины; пустой, если совпадений нет */
    Sensor[] findByType(String type);

    Sensor[] getAll();

    int getCount();

    int getCapacity();

    /** @return новый массив точной длины с датчиками, у которых isAlarm() == true */
    Sensor[] findAlarmSensors();

    /**
     * Добавляет датчик, если id свободен и есть место.
     * @throws IllegalArgumentException если s == null
     * @throws DuplicateSensorException если датчик с таким id уже есть
     * @throws StorageFullException     если хранилище заполнено
     */
    void addUnique(Sensor s) throws DuplicateSensorException, StorageFullException;

    /**
     * @throws SensorNotFoundException если датчика с таким id нет
     */
    Sensor getById(int id) throws SensorNotFoundException;
}

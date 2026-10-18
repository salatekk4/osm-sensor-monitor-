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

    /**
     * Заменяет датчик с указанным id новым объектом.
     * @throws IllegalArgumentException если updatedSensor == null
     * @throws SensorNotFoundException  если датчика с таким id нет
     * @throws DuplicateSensorException если новый id уже занят другим датчиком
     */
    void updateById(int id, Sensor updatedSensor)
            throws SensorNotFoundException, DuplicateSensorException;

    /**
     * Удаляет датчик по id.
     * @throws SensorNotFoundException если датчика с таким id нет
     */
    void deleteById(int id) throws SensorNotFoundException;

    /**
     * Редактирует датчик по id (id сохраняется). Тип может меняться,
     * поэтому создаётся новый объект нужного класса.
     * @throws SensorNotFoundException если датчика с таким id нет
     */
    default void updateSensor(int id, String type, double x, double y,
                              double radius, double value, String status)
            throws SensorNotFoundException, DuplicateSensorException {
        getById(id);
        Sensor updated;
        switch (type.toUpperCase()) {
            case "CO2": updated = new Co2Sensor(id, x, y, radius, value, status); break;
            case "N2":  updated = new GasSensor(id, x, y, radius, value, status); break;
            default:    updated = new TemperatureSensor(id, x, y, radius, value, status);
        }
        updateById(id, updated);
    }
}

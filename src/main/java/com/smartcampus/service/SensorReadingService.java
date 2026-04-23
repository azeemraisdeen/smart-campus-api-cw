package com.smartcampus.service;

import com.smartcampus.exception.RoomNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.storage.DataStore;

import java.util.List;
import java.util.UUID;

public class SensorReadingService {

    private final DataStore store = DataStore.getInstance();

    public List<SensorReading> getReadings(String sensorId) {
        if (store.getSensor(sensorId) == null) {
            throw new RoomNotFoundException(sensorId);
        }
        return store.getReadingsForSensor(sensorId);
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            throw new RoomNotFoundException(sensorId);
        }

        // Only ACTIVE sensors can accept readings
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId("RD-" + UUID.randomUUID());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.addReading(sensorId, reading);

        // Update sensor's current value to match the latest reading
        sensor.setCurrentValue(reading.getValue());
        store.putSensor(sensorId, sensor);

        return reading;
    }
}

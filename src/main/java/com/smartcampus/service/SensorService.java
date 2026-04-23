package com.smartcampus.service;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.RoomNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.storage.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SensorService {

    private final DataStore store = DataStore.getInstance();

    public List<Sensor> getAllSensors(String type) {
        List<Sensor> all = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.trim().isEmpty()) {
            return all.stream()
                .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type.trim()))
                .collect(Collectors.toList());
        }
        return all;
    }

    public Sensor getSensorById(String id) {
        Sensor sensor = store.getSensor(id);
        if (sensor == null) {
            throw new RoomNotFoundException(id);
        }
        return sensor;
    }

    public Sensor createSensor(Sensor sensor) {
        // Validate the referenced room actually exists
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            throw new LinkedResourceNotFoundException("Room", "null");
        }
        Room room = store.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            sensor.setId("SENSOR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.putSensor(sensor.getId(), sensor);
        room.addSensorId(sensor.getId());

        return sensor;
    }
}

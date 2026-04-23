package com.smartcampus.storage;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Static singleton so all resource instances share the same data maps.
// Using ConcurrentHashMap to handle concurrent requests safely without explicit locking.
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        loadSampleData();
    }

    private void loadSampleData() {
        Room lib301 = new Room("LIB-301", "Library Quiet Study", 40);
        Room cs101  = new Room("CS-101",  "Computer Science Lab", 30);
        Room hall1  = new Room("HALL-1",  "Main Hall", 200);
        rooms.put(lib301.getId(), lib301);
        rooms.put(cs101.getId(),  cs101);
        rooms.put(hall1.getId(),  hall1);

        Sensor temp001 = new Sensor("TEMP-001", "Temperature", "ACTIVE",      21.5, "LIB-301");
        Sensor co2001  = new Sensor("CO2-001",  "CO2",         "ACTIVE",      412.0, "LIB-301");
        Sensor occ001  = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0,  "CS-101");
        Sensor temp002 = new Sensor("TEMP-002", "Temperature", "ACTIVE",      19.0, "CS-101");
        sensors.put(temp001.getId(), temp001);
        sensors.put(co2001.getId(),  co2001);
        sensors.put(occ001.getId(),  occ001);
        sensors.put(temp002.getId(), temp002);

        lib301.addSensorId("TEMP-001");
        lib301.addSensorId("CO2-001");
        cs101.addSensorId("OCC-001");
        cs101.addSensorId("TEMP-002");

        List<SensorReading> t001 = new ArrayList<>();
        t001.add(new SensorReading("RD-" + UUID.randomUUID(), System.currentTimeMillis() - 60000, 21.0));
        t001.add(new SensorReading("RD-" + UUID.randomUUID(), System.currentTimeMillis() - 30000, 21.3));
        t001.add(new SensorReading("RD-" + UUID.randomUUID(), System.currentTimeMillis(),         21.5));
        sensorReadings.put("TEMP-001", t001);

        List<SensorReading> c001 = new ArrayList<>();
        c001.add(new SensorReading("RD-" + UUID.randomUUID(), System.currentTimeMillis() - 45000, 405.0));
        c001.add(new SensorReading("RD-" + UUID.randomUUID(), System.currentTimeMillis(),          412.0));
        sensorReadings.put("CO2-001", c001);
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Room getRoom(String id) { return rooms.get(id); }
    public void putRoom(String id, Room room) { rooms.put(id, room); }
    public void deleteRoom(String id) { rooms.remove(id); }

    public Map<String, Sensor> getSensors() { return sensors; }
    public Sensor getSensor(String id) { return sensors.get(id); }
    public void putSensor(String id, Sensor sensor) { sensors.put(id, sensor); }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}

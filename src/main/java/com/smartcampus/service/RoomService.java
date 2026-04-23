package com.smartcampus.service;

import com.smartcampus.exception.RoomNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.storage.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomService {

    private final DataStore store = DataStore.getInstance();

    public List<Room> getAllRooms() {
        return new ArrayList<>(store.getRooms().values());
    }

    public Room getRoomById(String id) {
        Room room = store.getRoom(id);
        if (room == null) {
            throw new RoomNotFoundException(id);
        }
        return room;
    }

    public Room createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            room.setId("ROOM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        store.putRoom(room.getId(), room);
        return room;
    }

    public void deleteRoom(String id) {
        Room room = getRoomById(id);
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(id);
        }
        store.deleteRoom(id);
    }
}

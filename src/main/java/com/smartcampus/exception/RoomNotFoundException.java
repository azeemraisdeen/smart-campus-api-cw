package com.smartcampus.exception;

public class RoomNotFoundException extends RuntimeException {
    private final String roomId;

    public RoomNotFoundException(String roomId) {
        super("No room found with ID: " + roomId);
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}

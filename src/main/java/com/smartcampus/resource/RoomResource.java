package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.service.RoomService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final RoomService roomService = new RoomService();

    @GET
    public Response getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return Response.ok(rooms).build();
    }

    @POST
    public Response createRoom(Room room) {
        Room created = roomService.createRoom(room);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        return Response.ok(room).build();
    }

    /**
     * DELETE a room. Returns 409 Conflict if sensors are still assigned to it.
     * A second DELETE on the same ID just returns 404 since the room is already gone.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        roomService.deleteRoom(roomId);
        return Response.noContent().build();
    }
}

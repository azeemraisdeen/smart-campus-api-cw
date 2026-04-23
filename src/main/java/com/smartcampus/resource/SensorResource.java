package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.service.SensorService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorService sensorService = new SensorService();

    /**
     * GET all sensors, with optional ?type= filter.
     * e.g. /api/v1/sensors?type=CO2
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = sensorService.getAllSensors(type);
        return Response.ok(sensors).build();
    }

    /**
     * POST a new sensor. Validates that the roomId exists (returns 422 if not).
     */
    @POST
    public Response createSensor(Sensor sensor) {
        Sensor created = sensorService.createSensor(sensor);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Sub-resource locator for /sensors/{sensorId}/readings.
     * Returns a SensorReadingResource instance for the given sensorId.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}

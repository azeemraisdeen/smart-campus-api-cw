package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root discovery endpoint - returns API metadata and available endpoints.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0.0");
        response.put("status", "RUNNING");
        response.put("module", "5COSC022W - Client-Server Architectures");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("rooms",    "/api/v1/rooms");
        endpoints.put("sensors",  "/api/v1/sensors");
        endpoints.put("readings", "/api/v1/sensors/{sensorId}/readings");
        response.put("endpoints", endpoints);

        response.put("timestamp", System.currentTimeMillis());

        return Response.ok(response).build();
    }
}

package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

// Catches anything not handled by the other mappers.
// Logs the real error server-side but returns a generic message to the client
// so no stack traces or internal details are ever exposed.
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        LOGGER.log(Level.SEVERE, "Caught unhandled exception: " + ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(500, "Internal Server Error",
                "Something went wrong on the server. Please try again later.");
        return Response.status(500).entity(error).type(MediaType.APPLICATION_JSON).build();
    }
}

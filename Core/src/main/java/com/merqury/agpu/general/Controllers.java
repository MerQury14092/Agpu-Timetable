package com.merqury.agpu.general;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class Controllers {
    public static void sendError(int status, String message, HttpServletResponse response) throws IOException {
        log.error("Error response with {} code. Message: {}", status, message);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.setHeader("Content-Type", "application/json");
        response.getOutputStream().print("{\"status\":%s,\"error\":\"%s\"}".formatted(status, message));
        response.getOutputStream().flush();
    }
}

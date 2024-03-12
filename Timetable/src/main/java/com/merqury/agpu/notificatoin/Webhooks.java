package com.merqury.agpu.notificatoin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.merqury.agpu.DTO.TimetableDay;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Log4j2
public class Webhooks {
    private static final ObjectMapper jsonConverter;
    static  {
        jsonConverter = new ObjectMapper();
    }
    public static boolean sendData(String host, TimetableDay timetableDay){
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL((host.contains("http://")||host.contains("https://"))?host:"http://"+host).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(jsonConverter.writeValueAsBytes(timetableDay.deleteHolidays()));
            connection.getOutputStream().flush();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            return connection.getResponseCode() < 300;
        } catch (IOException e){
            log.warn("Webhook is not available on: {}", host);
            return false;
        }
    }

    public static boolean ping(String host){
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL("http://"+host).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write("ping".getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            return connection.getResponseCode() == 200;
        } catch (IOException e){
            return false;
        }
    }
}

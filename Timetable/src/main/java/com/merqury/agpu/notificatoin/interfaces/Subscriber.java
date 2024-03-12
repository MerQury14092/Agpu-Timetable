package com.merqury.agpu.notificatoin.interfaces;

import com.merqury.agpu.DTO.TimetableDay;

public interface Subscriber {
    void handleNotification(String id, TimetableDay chagedTimetableDay);
}

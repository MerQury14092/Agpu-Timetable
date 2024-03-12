package com.merqury.agpu.notificatoin.DTO;

import com.merqury.agpu.DTO.TimetableDay;
import com.merqury.agpu.notificatoin.Webhooks;
import com.merqury.agpu.notificatoin.interfaces.Subscriber;
import com.merqury.agpu.notificatoin.service.TimetableChangesPublisher;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Webhook implements Subscriber {
    private String url, group;

    @Override
    public void handleNotification(String id, TimetableDay chagedTimetableDay) {
        if(id.equals(group))
            publishNotification(chagedTimetableDay);
    }

    private void publishNotification(TimetableDay chagedTimetableDay){
        if(!Webhooks.sendData(url, chagedTimetableDay))
            TimetableChangesPublisher.singleton().removeSubscriber(this);
    }
}

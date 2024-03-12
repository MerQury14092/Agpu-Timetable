package com.merqury.agpu.memory;

import com.merqury.agpu.DTO.Week;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeekMemory {
    private final List<Week> memory;

    public WeekMemory(){
        memory = new ArrayList<>();
    }

    public List<Week> getEverything(){
        return memory;
    }

    public void addAll(List<Week> list){
        memory.addAll(list);
    }

    public void initWeeks(){
        memory.forEach(Week::initArray);
    }
}

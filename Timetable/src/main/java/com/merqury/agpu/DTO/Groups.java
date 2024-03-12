package com.merqury.agpu.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Groups {
    private String facultyName;
    private List<String> groups;
    public Groups(){
        groups = new ArrayList<>();
    }
}

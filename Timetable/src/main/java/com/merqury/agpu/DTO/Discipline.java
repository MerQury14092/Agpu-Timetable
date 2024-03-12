package com.merqury.agpu.DTO;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.merqury.agpu.enums.DisciplineType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class Discipline {
    @JsonIgnore
    private String date;
    private String time;
    private String name;
    private String teacherName;
    private String audienceId;
    private int subgroup;
    private DisciplineType type;
    private String groupName;
    private boolean isDistant;
    @JsonIgnore
    private int colspan;

    public Discipline() {
    }

    public static Discipline holiday(){
        Discipline res = new Discipline();
        res.setName("HOLIDAY");
        return res;
    }

    public Discipline proxy() throws CloneNotSupportedException {
        return new Discipline(
                date,
                time,
                name,
                teacherName,
                audienceId,
                subgroup,
                type,
                groupName,
                isDistant,
                colspan
        );
    }
}

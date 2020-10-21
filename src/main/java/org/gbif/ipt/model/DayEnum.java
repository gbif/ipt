package org.gbif.ipt.model;

import javax.annotation.Nullable;

public enum DayEnum {
    MONDAY("monday", 2),
    TUESDAY("tuesday", 3),
    WEDNESDAY("wednesday", 4),
    THURSDAY("thursday", 5),
    FRIDAY("friday", 6),
    SATURDAY("saturday", 7),
    SUNDAY("sunday", 1);

    private final String identifier;
    private final int dayId;

    DayEnum(String identifier, int dayId) {
        this.identifier = identifier;
        this.dayId = dayId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getDayId() {
        return dayId;
    }

    public static DayEnum findByIdentifier(@Nullable String id) {
        if (id != null) {
            DayEnum[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                DayEnum entry = var1[var3];
                if (entry.getIdentifier().toLowerCase().equals(id.trim().toLowerCase())) {
                    return entry;
                }
            }
        }

        return null;
    }
}

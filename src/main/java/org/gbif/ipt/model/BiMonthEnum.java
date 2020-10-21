package org.gbif.ipt.model;

import javax.annotation.Nullable;

public enum BiMonthEnum {
    JANUARY_JULY("january_july", 0),
    FEBRUARY_AUGUST("february_august", 1),
    MARCH_SEPTEMBER("march_september", 2),
    APRIL_OCTOBER("april_october", 3),
    MAY_NOVEMBER("may_november", 4),
    JUNE_DECEMBER("june_december", 5);

    private final String identifier;
    private final int biMonthId;

    BiMonthEnum(String identifier, int biMonthId) {
        this.identifier = identifier;
        this.biMonthId = biMonthId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getBiMonthId() {
        return biMonthId;
    }

    public static BiMonthEnum findByIdentifier(@Nullable String id) {
        if (id != null) {
            BiMonthEnum[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                BiMonthEnum entry = var1[var3];
                if (entry.getIdentifier().toLowerCase().equals(id.trim().toLowerCase())) {
                    return entry;
                }
            }
        }

        return null;
    }
}

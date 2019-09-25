package it.univaq.estations.model;

import java.util.HashMap;
import java.util.Map;

public enum CheckInStatusTypes {

    CHARGED_SUCCESSFULLY (10),

    CHARGED_SUCCESSFULLY_AUTOMATED_CHECKIN (15),

    CHARGING_SPOT_IN_USE_NON_EV_PARKED (110),

    CHARGING_SPOT_IN_USE_OTHER_EV_PARKED (100),

    CHARGING_SPOT_NOT_ACCESSIBLE_ACCESS_LOCKED_OR_SITE_CLOSED (120),

    CHARGING_SPOT_NOT_FOUND_INADEQUATE_OR_INCORRECT_DETAILS (130),

    DID_NOT_VISIT_LOCATION (0),

    EQUIPMENT_AND_LOCATION_CONFIRMED_CORRECT (140),

    EQUIPMENT_LOCATION_HAS_BEEN_DECOMMISSIONED (160),

    FAILED_TO_CHARGE_EQUIPMENT_NOT_COMPATIBLE (30),

    FAILED_TO_CHARGE_EQUIPMENT_NOT_FULLY_INSTALLED (22),

    FAILED_TO_CHARGE_EQUIPMENT_NOT_OPERATIONAL (20),

    FAILED_TO_CHARGE_EQUIPMENT_PROBLEM (25),

    FAILED_TO_CHARGE_NO_CHARGING_EQUIPMENT_PRESENT (50),

    FAILED_TO_CHARGE_REQUIRED_OTHER_ACCESS_CARD_OR_FOB_OR_ETC (40),

    LOCATION_IS_A_DUPLICATE (150),

    OTHER_NEGATIVE_OR_BAD (200),

    OTHER_POSITIVE_OR_GOOD (210);

    private final int statusCode;
    private static Map<Integer, CheckInStatusTypes> map = new HashMap<>();


    CheckInStatusTypes(int statusCode) {
        this.statusCode = statusCode;
    }

    // static initialization blocks get run when the class gets loaded
    static {
        for (CheckInStatusTypes statusType : CheckInStatusTypes.values()) {
            map.put(statusType.statusCode, statusType);
        }
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public static CheckInStatusTypes valueOf(int statusCode) {
        return map.get(statusCode);
    }


}

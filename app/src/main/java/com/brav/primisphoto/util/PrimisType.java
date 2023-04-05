package com.brav.primisphoto.util;

/**
 * Created by ambra on 30/10/2017.
 */

public enum PrimisType {
    Red(0),
    Green(1);

    private int intValue;
    private PrimisType(int value) {
        intValue = value;
    }

    @Override
    public String toString() {
        String stringValue;
        switch (intValue) {
            case 0:
                stringValue = "Red";
                break;
            case 1:
                stringValue = "Green";
                break;
            default:
                stringValue = "";
        }

        return stringValue;
    }
}

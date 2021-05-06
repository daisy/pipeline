package org.daisy.dotify.text.impl;

import java.text.MessageFormat;

class NoInt2TextLocalization extends BasicInteger2Text {

    @Override
    public String getDefinedValue(int value) throws UndefinedNumberException {
        switch (value) {
            case 0:
                return "null";
            case 1:
                return "ett";
            case 2:
                return "to";
            case 3:
                return "tre";
            case 4:
                return "fire";
            case 5:
                return "fem";
            case 6:
                return "seks";
            case 7:
                return "sju";
            case 8:
                return "åtte";
            case 9:
                return "ni";
            case 10:
                return "ti";
            case 11:
                return "elleve";
            case 12:
                return "tolv";
            case 13:
                return "tretten";
            case 14:
                return "fjorten";
            case 15:
                return "femten";
            case 16:
                return "seksten";
            case 17:
                return "sytten";
            case 18:
                return "atten";
            case 19:
                return "nitten";
            case 20:
                return "tjue";
            case 30:
                return "tretti";
            case 40:
                return "førti";
            case 50:
                return "femti";
            case 60:
                return "seksti";
            case 70:
                return "sytti";
            case 80:
                return "åtti";
            case 90:
                return "nitti";
            default:
                throw new UndefinedNumberException();
        }
    }

    @Override
    public String formatNegative(String value) {
        return MessageFormat.format("minus {0}", value);
    }

    @Override
    public String formatThousands(String th, String rem) {
        if ("".equals(rem)) {
            return MessageFormat.format("{0} tusen", th);
        } else {
            return MessageFormat.format("{0} tusen {1}", th, rem);
        }
    }

    @Override
    public String formatHundreds(String hu, String rem) {
        if ("".equals(hu) && "".equals(rem)) {
            return "hundre";
        } else if ("".equals(hu)) {
            return MessageFormat.format("hundre og {0}", rem);
        } else if ("".equals(rem)) {
            return MessageFormat.format("{0} hundre", hu);
        } else {
            return MessageFormat.format("{0} hundre og {1}", hu, rem);
        }
    }

    @Override
    public String postProcess(String value) {
        return value;
    }

    @Override
    public String formatTens(String tens, String rem) {
        return MessageFormat.format("{0}{1}", tens, (rem.equals("ett") ? "en" : rem));
    }

}

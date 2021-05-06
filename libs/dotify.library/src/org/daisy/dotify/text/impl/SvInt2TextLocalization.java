package org.daisy.dotify.text.impl;

import java.text.MessageFormat;

class SvInt2TextLocalization extends BasicInteger2Text {

    @Override
    public String getDefinedValue(int value) throws UndefinedNumberException {
        switch (value) {
            case 0:
                return "noll";
            case 1:
                return "ett";
            case 2:
                return "två";
            case 3:
                return "tre";
            case 4:
                return "fyra";
            case 5:
                return "fem";
            case 6:
                return "sex";
            case 7:
                return "sju";
            case 8:
                return "åtta";
            case 9:
                return "nio";
            case 10:
                return "tio";
            case 11:
                return "elva";
            case 12:
                return "tolv";
            case 13:
                return "tretton";
            case 14:
                return "fjorton";
            case 15:
                return "femton";
            case 16:
                return "sexton";
            case 17:
                return "sjutton";
            case 18:
                return "arton";
            case 19:
                return "nitton";
            case 20:
                return "tjugo";
            case 30:
                return "trettio";
            case 40:
                return "fyrtio";
            case 50:
                return "femtio";
            case 60:
                return "sextio";
            case 70:
                return "sjuttio";
            case 80:
                return "åttio";
            case 90:
                return "nittio";
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
            return MessageFormat.format("{0}tusen", th);
        } else {
            return MessageFormat.format("{0}tusen{1}", th, rem);
        }
    }

    @Override
    public String formatHundreds(String hu, String rem) {
        if ("".equals(hu) && "".equals(rem)) {
            return "hundra";
        } else if ("".equals(hu)) {
            return MessageFormat.format("hundra{0}", rem);
        } else if ("".equals(rem)) {
            return MessageFormat.format("{0}hundra", hu);
        } else {
            return MessageFormat.format("{0}hundra{1}", hu, rem);
        }
    }

    @Override
    public String postProcess(String value) {
        // replace three occurrences of the same character by two
        return value.replaceAll("(\\w)(\\1{2})", "$2");
    }

    @Override
    public String formatTens(String tens, String rem) {
        return MessageFormat.format("{0}{1}", tens, rem);
    }

}

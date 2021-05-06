package org.daisy.dotify.text.impl;

import java.text.MessageFormat;

class FiInt2TextLocalization extends BasicInteger2Text {
    private static final String YKSI = "yksi";

    @Override
    public String getDefinedValue(int value) throws UndefinedNumberException {
        switch (value) {
            case 0:
                return "nolla";
            case 1:
                return YKSI;
            case 2:
                return "kaksi";
            case 3:
                return "kolme";
            case 4:
                return "neljä";
            case 5:
                return "viisi";
            case 6:
                return "kuusi";
            case 7:
                return "seitsemän";
            case 8:
                return "kahdeksan";
            case 9:
                return "yhdeksän";
            case 10:
                return "kymmenen";
            case 11:
                return "yksitoista";
            case 12:
                return "kaksitoista";
            case 13:
                return "kolmetoista";
            case 14:
                return "neljätoista";
            case 15:
                return "viisitoista";
            case 16:
                return "kuusitoista";
            case 17:
                return "seitsemäntoista";
            case 18:
                return "kahdeksantoista";
            case 19:
                return "yhdeksäntoista";
            case 20:
                return "kaksikymmentä";
            case 30:
                return "kolmekymmentä";
            case 40:
                return "neljäkymmentä";
            case 50:
                return "viisikymmentä";
            case 60:
                return "kuusikymmentä";
            case 70:
                return "seitsemänkymmentä";
            case 80:
                return "kahdeksankymmentä";
            case 90:
                return "yhdeksänkymmentä";
            default:
                throw new UndefinedNumberException();
        }
    }

    @Override
    public String formatNegative(String value) {
        return MessageFormat.format("miinus {0}", value);
    }

    @Override
    public String formatThousands(String th, String rem) {
        if (YKSI.equals(th) && "".equals(rem)) {
            return "tuhat";
        } else if (YKSI.equals(th)) {
            return MessageFormat.format("tuhat{0}", rem);
        } else if ("".equals(rem)) {
            return MessageFormat.format("{0}tuhatta", th);
        } else {
            return MessageFormat.format("{0}tuhatta{1}", th, rem);
        }
    }

    @Override
    public String formatHundreds(String hu, String rem) {
        if (YKSI.equals(hu) && "".equals(rem)) {
            return "sata";
        } else if (YKSI.equals(hu)) {
            return MessageFormat.format("sata ja {0}", rem);
        } else if ("".equals(rem)) {
            return MessageFormat.format("{0}sataa", hu);
        } else {
            return MessageFormat.format("{0}sataa{1}", hu, rem);
        }
    }

    @Override
    public String postProcess(String value) {
        return value;
    }

    @Override
    public String formatTens(String tens, String rem) {
        return MessageFormat.format("{0}{1}", tens, rem);
    }

}

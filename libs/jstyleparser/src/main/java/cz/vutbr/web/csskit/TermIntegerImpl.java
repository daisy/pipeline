package cz.vutbr.web.csskit;

import cz.vutbr.web.css.TermInteger;

public class TermIntegerImpl extends TermLengthImpl implements TermInteger {

	protected TermIntegerImpl() {
	    setUnit(Unit.none);
	}

    public int getIntValue() {
        return getValue().intValue();
    }

    public TermInteger setValue(int value) {
        setValue(Float.valueOf(value));
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if(operator!=null) sb.append(operator.value());
        sb.append(String.valueOf(getIntValue()));
        return sb.toString();
    }

}

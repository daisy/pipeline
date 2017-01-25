package cz.vutbr.web.csskit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.StyleSheet;

public class AbstractRuleBlock<T> extends AbstractRule<T> implements RuleBlock<T> {
	
	protected StyleSheet stylesheet;
	
	public StyleSheet getStyleSheet()
	{
		return stylesheet;
	}

	public void setStyleSheet(StyleSheet stylesheet)
	{
		this.stylesheet = stylesheet;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AbstractRuleBlock<?>))
			return false;
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		AbstractRuleBlock<T> clone; {
			try {
				clone = (AbstractRuleBlock<T>)super.clone();
			} catch (CloneNotSupportedException e) {
				throw new InternalError("coding error");
			}
		}
		clone.list = cloneList(list);
		return clone;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> List<T> cloneList(List<T> list) {
		List<T> clone = new ArrayList<T>();
		for (T t : list) {
			if (t instanceof Cloneable) {
				try {
					clone.add((T)t.getClass().getMethod("clone").invoke(t));
				} catch (IllegalAccessException e) {
					throw new InternalError("coding error");
				} catch (IllegalArgumentException e) {
					throw new InternalError("coding error");
				} catch (InvocationTargetException e) {
					throw new InternalError("coding error");
				} catch (NoSuchMethodException e) {
					throw new InternalError("coding error");
				} catch (SecurityException e) {
					throw new InternalError("coding error");
				}
			} else
				clone.add(t);
		}
		return clone;
	}
}

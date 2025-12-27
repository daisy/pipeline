package cz.vutbr.web.csskit;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cz.vutbr.web.css.MediaQueryList;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.StyleSheet.Origin;

public class AbstractRule<T> extends AbstractList<T> implements Rule<T> {
	
	protected List<T> list = Collections.emptyList();
	protected int hash = 0;
	protected Origin origin = null;
	protected MediaQueryList media = MediaQueryListImpl.EMPTY;
	
	@Override
	public List<T> asList() {
		return this.list;
	}
	
	@Override
	public Rule<T> replaceAll(List<T> replacement) {
        hash = 0;
		this.list = replacement;
		return this;
	}
	
	@Override
	public Rule<T> unlock() {
        hash = 0;
		this.list = new ArrayList<T>();
		return this;
	}
	
	@Override
	public Origin getOrigin() {
		return origin;
	}
	
	@Override
	public void setOrigin(Origin origin) {
		this.origin = origin;
		// set origin recursively on contained rules
		for (T t : list)
			if (t instanceof Rule)
				((Rule<?>)t).setOrigin(origin);
	}
	
	@Override
	public MediaQueryList getMediaQueries() {
		return media;
	}

	@Override
	public void setMediaQueries(MediaQueryList media) {
		if (media != null) {
			if (this.media == null)
				this.media = media;
			else if (this.media != media)
				this.media = this.media.and(media);
		}
		// set media recursively on contained rules
		for (T t : list)
			if (t instanceof Rule)
				((Rule<?>)t).setMediaQueries(media);
	}

	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public T get(int index) {
		return list.get(index);
	}	
	
	@Override
	public T set(int index, T element) {
        hash = 0;
		if (element instanceof Rule) {
			((Rule<?>)element).setOrigin(origin);
			((Rule<?>)element).setMediaQueries(media);
		}
		return list.set(index, element);
	}
	
	@Override
	public void add(int index, T element) {
        hash = 0;
		if (element instanceof Rule) {
			((Rule<?>)element).setOrigin(origin);
			((Rule<?>)element).setMediaQueries(media);
		}
		list.add(index, element);
	}
	
	@Override
	public T remove(int index) {
        hash = 0;
		return list.remove(index);
	}
	
	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public boolean add(T o) {
	    hash = 0;
		if (o instanceof Rule) {
			((Rule<?>)o).setOrigin(origin);
			((Rule<?>)o).setMediaQueries(media);
		}
		return list.add(o);
	};
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
	    if (hash == 0)
	    {
    		final int prime = 31;
    		int result = super.hashCode();
    		result = prime * result + ((list == null) ? 0 : list.hashCode());
    		hash = result;
	    }
	    return hash;
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
		if (!(obj instanceof AbstractRule<?>))
			return false;
		AbstractRule<?> other = (AbstractRule<?>) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		return true;
	}

	
	
}

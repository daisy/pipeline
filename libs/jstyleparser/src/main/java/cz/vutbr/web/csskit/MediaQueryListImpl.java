package cz.vutbr.web.csskit;

import java.util.Collections;
import java.util.List;

import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.MediaQueryList;
import cz.vutbr.web.css.Rule;

public class MediaQueryListImpl extends AbstractRuleBlock<MediaQuery> implements MediaQueryList {

	/**
	 * Immutable empty media query list.
	 */
	public static final MediaQueryList EMPTY = new MediaQueryListImpl() {
			{
				this.list = Collections.emptyList();
			}
			@Override
			public Rule<MediaQuery> replaceAll(List<MediaQuery> replacement) {
				throw new UnsupportedOperationException("Object is immutable");
			}
			@Override
			public Rule<MediaQuery> unlock() {
				throw new UnsupportedOperationException("Object is immutable");
			}
		};

	/**
	 * Immutable media query list that never matches.
	 */
	public static final MediaQueryList NOT_ALL = new MediaQueryListImpl() {
			{
				this.list = Collections.singletonList(MediaQueryImpl.NOT_ALL);
			}
			@Override
			public Rule<MediaQuery> replaceAll(List<MediaQuery> replacement) {
				throw new UnsupportedOperationException("Object is immutable");
			}
			@Override
			public Rule<MediaQuery> unlock() {
				throw new UnsupportedOperationException("Object is immutable");
			}
		};

	public MediaQueryListImpl() {
	}

	public MediaQueryList and(MediaQuery query) {
		if (query == null)
			return this;
		else
			return and(Collections.singletonList(query));
	}

	public MediaQueryList and(List<MediaQuery> queries) {
		if (queries == null || queries.isEmpty())
			return this;
		else if (isEmpty()) {
			if (queries instanceof MediaQueryList)
				return (MediaQueryList)queries;
			else {
				MediaQueryList list = new MediaQueryListImpl();
				list.unlock();
				for (MediaQuery q : queries)
					list.add(q);
				return list;
			}
		} else {
			MediaQueryList combined = this;
			MediaQueryList newList = new MediaQueryListImpl();
			newList.unlock();
			for (MediaQuery q : this) {
				MediaQuery c = null;
				for (MediaQuery qq : queries) {
					MediaQuery cc = q.and(qq);
					if (cc != MediaQueryImpl.NOT_ALL) {
						newList.add(cc);
						if (c == null)
							c = cc;
						else if (c != cc)
							combined = newList;
					}
				}
				if (c == null)
					combined = newList;
			}
			if (combined == newList && newList.isEmpty())
				return NOT_ALL;
			return combined;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb = OutputUtil.appendList(sb, this, OutputUtil.MEDIA_DELIM);
		return sb.toString();
	}
}

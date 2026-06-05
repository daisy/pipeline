package org.daisy.pipeline.tts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoundRobinLoadBalancer implements LoadBalancer {
	private int mIndex = 0;
	private Object mSyncPoint;

	private List<Host> mHosts;

	/**
	 * @param hostlist is the list of hosts. The first host will be considered
	 *            as the master host.
	 * @param syncPoint is used for locking when several threads attempt to call
	 *            selectHost() with the same syncPoint.
	 */
	public RoundRobinLoadBalancer(String hostlist, Object syncPoint) {
		mSyncPoint = syncPoint;
		String[] parts = hostlist.split("[ ,;\t\n]+");
		mHosts = new ArrayList<Host>();

		for (int i = 0; i < parts.length; ++i) {
			try {
				Host h = new Host();
				String[] pair = parts[i].split(":");
				h.address = pair[0];
				h.port = Integer.valueOf(pair[1]);
				mHosts.add(h);
			} catch (Exception e) {
				throw new IllegalArgumentException("bad format for: '" + parts[i] + "'");
			}
		}

	}

	@Override
	public Host selectHost() {
		int index;
		if (mSyncPoint != null) {
			synchronized (mSyncPoint) {
				mIndex = (mIndex + 1) % mHosts.size();
				index = mIndex;
			}
		} else {
			mIndex = (mIndex + 1) % mHosts.size();
			index = mIndex;
		}

		return mHosts.get(index);
	}

	@Override
	public Collection<Host> getAllHosts() {
		return mHosts;
	}

	@Override
	public Host getMaster() {
		return mHosts.get(0);
	}

	@Override
	public void discard(Host h) {
		mHosts.remove(h);
	}

	public void discardAll(Collection<Host> hosts) {
		mHosts.removeAll(hosts);
	}

}

package org.daisy.pipeline.nlp.breakdetect.calabash.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

/**
 * This class keeps track of the duplicated nodes. Rather than maintaining a set
 * of all the nodes created, it keeps under watch only those that might not have
 * been closed yet in the original document. The nodes' depth levels are used
 * for detecting when they are closed.
 */
public class DuplicationManager {

	private List<NodeInfo> mDuplicatedNodes;
	private ArrayList<Set<NodeInfo>> mUnderWatch;

	private boolean mForbidAnyDup;
	private final static QName IDattr = new QName("id");

	public DuplicationManager(boolean forbidAnyDup) {
		mForbidAnyDup = forbidAnyDup;
	}

	public void onNewDocument() {
		mDuplicatedNodes = new ArrayList<NodeInfo>();
		mUnderWatch = new ArrayList<Set<NodeInfo>>();
	}

	public void onNewSection() {
	}

	public void onNewNode(XdmNode node, int level) {
		if (!mForbidAnyDup && node.getAttributeValue(IDattr) == null)
			return;

		NodeInfo info = node.getUnderlyingNode();
		for (Set<NodeInfo> watched : mUnderWatch)
			if (watched.contains(info)) {
				mDuplicatedNodes.add(info);
				break;
			}

		if (mUnderWatch.size() > level)
			mUnderWatch.subList(level + 1, mUnderWatch.size()).clear();
		else
			while (mUnderWatch.size() <= level)
				mUnderWatch.add(new HashSet<NodeInfo>());
		mUnderWatch.get(level).add(info);
	}

	//the result may contain multiple occurrences of the same node
	public List<NodeInfo> getDuplicatedNodes() {
		return mDuplicatedNodes;
	}
}

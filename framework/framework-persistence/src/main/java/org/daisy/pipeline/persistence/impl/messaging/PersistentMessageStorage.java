package org.daisy.pipeline.persistence.impl.messaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.event.ProgressMessage;
import org.daisy.pipeline.persistence.impl.Database;

public class PersistentMessageStorage implements MessageStorage {

	private EntityManagerFactory emf;
	private Database database;
	private Map<String,Iterable<Message>> allMessages = new HashMap<>();

	public synchronized void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
		this.database = new Database(emf);
	}

	@Override
	public synchronized boolean add(Message msg) {
		if (database != null) {
			boolean added = false;
			// text-less messages are filtered out from ProgressMessage.asMessageFilter() but
			// because the results of selecting from this database are not ProgressMessage instances
			// we have to filter out the empty messages here
			if (!(msg instanceof ProgressMessage && msg.getText() == null)) {
				database.addObject(new PersistentMessage(msg));
				added = true;
			}
			if (msg instanceof ProgressMessage) {
				// Just flatten the messages for now because it is too complicated to store the tree.
				// This assumes that the message is only added when it will no longer change.
				for (ProgressMessage child : (ProgressMessage)msg)
					added = add(child) || added;
			}
			return added;
		}
		return false;
	}

	@Override
	public boolean remove(String jobId) {
		EntityManager em = emf.createEntityManager();
		StringBuilder sqlBuilder = new StringBuilder("delete from PersistentMessage where jobId='%s'");
		String sql = String.format(sqlBuilder.toString(), jobId.toString());
		Query q = em.createQuery(sql);
		em.getTransaction().begin();
		int res = q.executeUpdate();
		em.getTransaction().commit();
		em.close();
		return res > 0;
	}

	@Override
	public Iterable<Message> get(String jobId) {
		if (!allMessages.containsKey(jobId))
			allMessages.put(jobId, () -> {
				EntityManager em = emf.createEntityManager();
				StringBuilder sqlBuilder = new StringBuilder("select m from PersistentMessage m where m.jobId='%s'");
				String sql = String.format(sqlBuilder.toString(), jobId.toString());
				Query q = em.createQuery(sql);
				@SuppressWarnings("unchecked") // just how persistence works
				List<Message> result = q.getResultList();
				em.close();
				return result.iterator();
			});
		return allMessages.get(jobId);
	}
}

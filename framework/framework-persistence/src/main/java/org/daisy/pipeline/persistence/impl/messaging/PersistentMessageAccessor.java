package org.daisy.pipeline.persistence.impl.messaging;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;


import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.pipeline.job.JobId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentMessageAccessor extends MessageAccessor {
	private static final Logger logger =
		LoggerFactory.getLogger(PersistentMessageAccessor.class);


	JobId jobId;
	private EntityManagerFactory emf;

	public PersistentMessageAccessor(JobId jobId,EntityManagerFactory emf) {
		super();
		this.jobId = jobId;
		this.emf = emf;
	}

	@Override
	public List<Message> getAll() {
		return getMessages(jobId, 0,
				Arrays.asList(Level.values()));
	}

	@Override
	protected List<Message> getMessagesFrom(Level level) {
		List<Level> levels = new LinkedList<Level>();

		for (Level iter : Level.values()) {
			if (iter.compareTo(level) == 0) {
				levels.add(iter);
			} else {
				break;
			}
		}

		return getMessages(jobId, 0,
				levels);
	}

	@Override
	public MessageFilter createFilter() {
		return new PersistentMessageAccessor.MessageFilter();
	}
	@Override
	public boolean delete(){
		EntityManager em = emf.createEntityManager();

		StringBuilder sqlBuilder=new StringBuilder("delete from PersistentMessage where jobId='%s' ");
		String sql=String.format(sqlBuilder.toString(), jobId.toString());
		Query q=em.createQuery(sql);
		em.getTransaction().begin();
		int res=q.executeUpdate();
		em.getTransaction().commit();
		em.close();
		return res>0;
	}

	protected class MessageFilter implements MessageAccessor.MessageFilter {
		List<Level> mLevels = Arrays.asList(Level.values());
		int mSeq = -1;
		int mStart;
		int mEnd;
		boolean getRange = false;
		
		@Override
		public MessageFilter filterLevels(final Set<Level> levels) {
			mLevels = new LinkedList<Level>(levels);
			return this;
		}

		@Override
		public MessageFilter greaterThan(int idx) {
			mSeq = idx;
			return this;
		}
		@Override
		public MessageFilter inRange(int start, int end) {
			mStart = start;
			mEnd = end;
			getRange = true;
			return this;
		}
			
		@Override
		public List<Message> getMessages() {
			if (getRange) {
				return PersistentMessageAccessor.this
						.getMessagesInRange(jobId, mStart, mEnd, mLevels);
			}
			else {
				return PersistentMessageAccessor.this
						.getMessages(jobId, mSeq, mLevels);
			}	
		}
		
		
	}
	
	private List<Message> getMessages(JobId id,int from, List<Level> levels){
		EntityManager em = emf.createEntityManager();
		StringBuilder sqlBuilder=new StringBuilder("select m from PersistentMessage m where m.jobId='%s' and  m.sequence > %s");
		String sql=String.format(sqlBuilder.toString(), id.toString(),from);
		Query q=em.createQuery(sql);

		@SuppressWarnings("unchecked") //just how persistence works
		List<Message> result = q.getResultList();
		em.close();
		return result;
	}
	
	private List<Message> getMessagesInRange(JobId id,int start, int end, List<Level> levels){
		EntityManager em = emf.createEntityManager();
		StringBuilder sqlBuilder=new StringBuilder("select m from PersistentMessage m where m.jobId='%s' and  m.sequence >= %s and m.sequence <= %s");
		String sql=String.format(sqlBuilder.toString(), id.toString(),start,end);
		Query q=em.createQuery(sql);
		@SuppressWarnings("unchecked") //just how persistence works
		List<Message> result = q.getResultList();
		em.close();
		return result;
	}
}

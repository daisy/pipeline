package org.daisy.pipeline.nonpersistent.impl.messaging;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.pipeline.job.JobId;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class VolatileMessageAccessor extends MessageAccessor{

	private VolatileMessageStorage storage=VolatileMessageStorage.getInstance();
	private String id; 

	/**
	 * @param id
	 */
	public VolatileMessageAccessor(JobId id) {
		this.id = id.toString();
	}

	@Override
	public List<Message> getAll() {
		return this.getMessagesFrom(Level.TRACE);
	}
	

	@Override
	protected List<Message> getMessagesFrom(final Level level) {
		Collection<Message> messages = Collections2.filter(storage.get(this.id),Filters.getLevelFilter(level));
		return Lists.newLinkedList(messages);
	}

	@Override
	public boolean delete() {
		storage.remove(id);
		return true;
	}

	@Override
	public MessageFilter createFilter() {
		return new VolatileMessgeFilter();
	}


	/**
	 *
	 *
	 * @param start inclusive
	 * @param end inclusive
	 * @param levels
	 * @return
	 */
	private List<Message> getMessagesInRange(int start,int end,Set<Level> levels){
		if(this.storage.get(this.id).size()==0){
			return Collections.emptyList();
		}

		List<Message> indexed= this.storage.get(this.id).subList(start,end);
		indexed=new LinkedList<Message>(Collections2.filter(indexed,Filters.getLevelSetFilter(levels)));

		return indexed;
	}

	private List<Message> getMessagesFromIdx(int start,Set<Level> levels){
		return getMessagesInRange(start,this.storage.get(this.id).size(),levels);
	}

	private class VolatileMessgeFilter implements
			MessageAccessor.MessageFilter {

		private Set<Level> mLevels = new HashSet(Arrays.asList(Level.values()));
		private int mSeq = -1;
		private int mStart;
		private int mEnd;
		private boolean getRange = false;
		
		@Override
		public MessageFilter filterLevels(final Set<Level> levels) {
			mLevels = levels;
			return this;
		}

		@Override
		public MessageFilter greaterThan(int idx) {

			mSeq = idx;
			return this;
		}

		/**
		 * @param start inclusive
		 * @param end inclusive
		 * @return
		 */
		@Override
		public MessageFilter inRange(int start, int end) {
			if(start<0){
				throw new IndexOutOfBoundsException("range start has to be 0 or greater");
			}else if(end>VolatileMessageAccessor.this.getAll().size()){
				throw new IndexOutOfBoundsException("range end has to be less than the number of messages");
			}else if(start>end){
				throw new IllegalArgumentException("range start is greater than end");
			}

			mStart = start;
			mEnd = end;
			getRange = true;
			return this;
		}

		@Override
		public List<Message> getMessages() {
			if (getRange) {
				return VolatileMessageAccessor.this.getMessagesInRange( mStart,mEnd, mLevels);
			}else{
				return VolatileMessageAccessor.this.getMessagesFromIdx( mSeq+1, mLevels);
			}	
			//return null;
		}
	}

	private static class Filters{
		public static Predicate<Message> getLevelFilter(final Level level){
			return  new Predicate<Message>(){
				public boolean apply(Message msg){
					return msg.getLevel().compareTo(level)<=0;
				}
			};
		}
		public static Predicate<Message> getLevelSetFilter(final Set<Level> levels){
			return  new Predicate<Message>(){
				public boolean apply(Message msg){
					return levels.contains(msg.getLevel());
				}
			};
		}
	}
	
}

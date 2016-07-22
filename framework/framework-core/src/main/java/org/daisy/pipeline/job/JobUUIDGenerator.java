package org.daisy.pipeline.job;

import java.util.UUID;


// TODO: Auto-generated Javadoc
/**
 * The Class JobUUIDGenerator relies on {@link UUID} objects to generate job ids
 */
public class JobUUIDGenerator implements JobIdGenerator {

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.JobIdGenerator#generateId()
	 */
	@Override
	public JobId generateId() {
		return new JobUUID();
	}
	public JobBatchId generateBatchId() {
		return new JobBatchId(new JobUUID().toString());
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.JobIdGenerator#generateIdFromString(java.lang.String)
	 */
	@Override
	public JobId generateIdFromString(String base) {
		return new JobUUID(base);
	}

	/**
	 * The Class JobUUID.
	 */
	private static class JobUUID implements JobId{

		/**
		 *
		 */
		private static final long serialVersionUID = -379462775714828942L;
		/** The m id. */
		private final UUID mId;

		/**
		 * Instantiates a new job uuid.
		 */
		private JobUUID() {
			mId = UUID.randomUUID();
		}

		/**
		 * Instantiates a new job uuid.
		 *
		 * @param base the base
		 */
		private JobUUID(String base) {
			mId = UUID.fromString(base);
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return mId.hashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			JobUUID other = (JobUUID) obj;
			if (mId == null) {
				if (other.mId != null) {
					return false;
				}
			} else if (!mId.equals(other.mId)) {
				return false;
			}
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return mId.toString();
		}

	}



}

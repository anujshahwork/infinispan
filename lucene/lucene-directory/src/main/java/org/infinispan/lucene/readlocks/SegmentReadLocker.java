package org.infinispan.lucene.readlocks;



/**
 * <p>SegmentReadLocker implementations have to make sure that segments are not deleted while they are
 * being used by an IndexReader.</p>
 * <p>When an {@link org.infinispan.lucene.InfinispanIndexInput} is opened on a file which is split in smaller chunks,
 * {@link #acquireReadLock(String)} is invoked; then the {@link #releaseReadLock(String)} is
 * invoked when the stream is closed.</p>
 * <p>{@link #markForDeletion(String)} is invoked when a file is deleted, so {@link #releaseReadLock(String)}
 * is invoked for the last time this implementation must delete all segment chunks and the associated metadata.</p>
 * <p>Note that if you can use and tune the {@link org.apache.lucene.index.LogByteSizeMergePolicy} you could avoid the need
 * for readlocks by setting a maximum segment size to equal the chunk size used by the InfinispanDirectory; readlocks
 * will be skipped automatically when not needed, so it's advisable to still configure an appropriate SegmentReadLocker
 * for the cases you might want to tune the chunk size.</p>
 *
 * @author Sanne Grinovero
 * @since 4.1
 */
public interface SegmentReadLocker {

  /**
   * Set file to be deleted when all acquired read locks have been released. The file will be deleted immediately
   * if the file is not locked.
   * @param fileName of the file to mark for deletion
   * @see InfinispanDirectory#deleteFile(String)
   */
   void markForDeletion(String fileName);

   /**
    * It will release a previously acquired readLock.
    * If it's invoked on a file without pending locks and the file is marked for deletion it will be deleted.
    *
    * @param fileName of the file to release or delete
    * @see markForDeletion()
    */
   void releaseReadLock(String fileName);

   /**
    * Acquires a readlock, in order to prevent other invocations to {@link #releaseReadLock(String)}
    * from deleting the file.
    *
    * @param filename
    * @return true if the lock was acquired, false if the implementation
    * detects the file does not exist, or that it's being deleted by some other thread.
    * @see InfinispanDirectory#openInput(String)
    */
   boolean acquireReadLock(String filename);

}

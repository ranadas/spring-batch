package org.springframework.batch.core.step.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulation of a list of items to be processed and possibly a list of
 * failed items to be skipped. To mark an item as skipped clients should iterate
 * over the chunk using the {@link #iterator()} method, and if there is a
 * failure call {@link ChunkIterator#remove(Exception)} on the iterator. The
 * skipped items are then available through the chunk.
 * 
 * @author Dave Syer
 * 
 */
class Chunk<W> implements Iterable<W> {

	private List<W> items = new ArrayList<W>();

	private List<SkippedItem<W>> skips = new ArrayList<SkippedItem<W>>();

	/**
	 * Add the item to the chunk.
	 * @param item
	 */
	public void add(W item) {
		items.add(item);
	}

	/**
	 * Clear the items down to signal that we are done.
	 */
	public void clear() {
		items.clear();
	}

	/**
	 * @return a copy of the items to be processed as an unmodifiable list
	 */
	public List<W> getItems() {
		return Collections.unmodifiableList(new ArrayList<W>(items));
	}

	/**
	 * @return a copy of the skips as an unmodifiable list
	 */
	public List<SkippedItem<W>> getSkips() {
		return Collections.unmodifiableList(new ArrayList<SkippedItem<W>>(skips));
	}

	/**
	 * @return true if there are no items in the chunk
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * Get an unmodifiable iterator for the underlying items.
	 * @see java.lang.Iterable#iterator()
	 */
	public ChunkIterator iterator() {
		return new ChunkIterator(items);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("[items=%s, skips=%s]", items, skips);
	}

	/**
	 * Special iterator for a chunk providing the {@link #remove(Exception)}
	 * method for dynamically removing an item abd adding it to the skips.
	 * 
	 * @author Dave Syer
	 * 
	 */
	public class ChunkIterator implements Iterator<W> {

		final private Iterator<W> iterator;

		private W next;

		public ChunkIterator(List<W> items) {
			iterator = items.iterator();
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public W next() {
			next = iterator.next();
			return next;
		}

		public void remove(Exception e) {
			if (next != null) {
				skips.add(new SkippedItem<W>(next, e));
			}
			iterator.remove();
		}

		public void remove() {
			throw new UnsupportedOperationException("To remove an item you must provide an exception.");
		}

	}

	/**
	 * Wrapper for a skipped item and its exception.
	 * 
	 * @author Dave Syer
	 * 
	 */
	public static class SkippedItem<T> {

		final private Exception exception;

		final private T item;

		public SkippedItem(T item, Exception e) {
			this.item = item;
			this.exception = e;
		}

		/**
		 * Public getter for the exception.
		 * @return the exception
		 */
		public Exception getException() {
			return exception;
		}

		/**
		 * Public getter for the item.
		 * @return the item
		 */
		public T getItem() {
			return item;
		}

		@Override
		public String toString() {
			return String.format("[exception=%s, item=%s]", exception, item);
		}

	}

}
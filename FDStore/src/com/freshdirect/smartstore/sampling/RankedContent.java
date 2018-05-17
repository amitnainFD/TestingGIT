package com.freshdirect.smartstore.sampling;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.filter.ContentFilter;

/**
 * Wrapper class for ranked content.
 * 
 * This class represents either one particular content key ({@link Single}) or
 * a group of content keys ({@link Aggregate}) that need to be counted with a
 * common rank (Category Aggregation).
 * 
 * @author istvan
 * 
 */
public abstract class RankedContent implements Comparable<RankedContent> {
	/**
	 * One particular content key.
	 * 
	 * @author istvan
	 * 
	 */
	public static class Single extends RankedContent {
		private ContentKey id;

		private double score;

		private ContentNodeModel model;

		public Single(ContentKey id, double score) {
			this.id = id;
			this.score = score;
		}

		public Single(double score, ContentNodeModel model) {
			this.id = model.getContentKey();
			this.score = score;
			this.model = model;
		}
		

		void setModel(ContentNodeModel m) {
                    this.id = m.getContentKey();
                    this.model = m;
                }
		
		/**
		 * Filter the internal product node with the given filter.
		 * @param filter
		 * @return
		 */
		public boolean filter(ContentFilter filter) {
		    ContentNodeModel newModel = filter.filter(getModel());
		    if (newModel != null) {
		        setModel(newModel);
		        return true;
		    } else {
		        return false;
		    }
		}

		public double getScore() {
			return score;
		}

		public int getCount() {
			return 1;
		}

		public String getId() {
			return id.getId();
		}
		
		@Override
		public String getName() {
			getModel();
			return model != null ? model.getFullName() : id.getId();
		}

		public ContentKey getContentKey() {
			return id;
		}

		public String toString() {
			return id.toString() + ' ' + score;
		}

                public ContentNodeModel getModel() {
                    if (model == null) {
                        model = ContentFactory.getInstance().getContentNodeByKey(id);
                    }
                    return model;
                }

	};

	/**
	 * Content key aggregation.
	 * 
	 * This items are aggregated at the category level, and thus are treated the
	 * same from the perspective of sampling.
	 * 
	 * @author istvan
	 * 
	 */
	public static class Aggregate extends RankedContent {

		private static Random R = new Random();

		protected double totalScore = 0;

		// List<Single>
		protected LinkedList<Single> items = new LinkedList<Single>();
		protected String id;

		public Aggregate(String id) {
			this.id = id;
		}

		public void add(Single o) {
			items.add(o);
			totalScore += o.getScore();
		}

		/**
		 * Remove one content key and adjust group score.
		 * 
		 * @return content removed
		 */
		public Single take() {
			Single r = items.remove(R.nextInt(items.size()));
			totalScore -= r.getScore();
			return r;
		}

		public Single takeFirst() {
			Single r = items.removeFirst();
			totalScore -= r.getScore();
			return r;
		}

		public void filterWith(ContentFilter filter) {
			for (Iterator<Single> i = items.iterator(); i.hasNext();) {
				Single stored = i.next();
                                if (!stored.filter(filter)) {
                                    totalScore -= stored.getScore();
                                    i.remove();
                                }
//                                if (filter.filter(stored.getModel()) == null) {
//                                    totalScore -= stored.getScore();
//                                    i.remove();
//                            }
			}
		}

		/**
		 * Get the id of the group.
		 */
		public String getId() {
			return id;
		}
		
		@Override
		public String getName() {
			return id;
		}

		/**
		 * 
		 * @return List<Single>
		 */
		public List<Single> getItems() {
			return items;
		}

		public double getScore() {
			return totalScore;
		}

		public int getCount() {
			return items.size();
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			for (Iterator<Single> i = items.iterator(); i.hasNext();) {
				RankedContent.Single item = (RankedContent.Single) i.next();
				buffer.append(buffer.length() > 0 ? ',' : '[');
				buffer.append(item);
			}
			return buffer.append("] ").append(getScore()).toString();
		}
	}
	
	public static class DeterministicAggregate extends Aggregate {
		public DeterministicAggregate(String id) {
			super(id);
		}
		
		@Override
		public void add(Single o) {
			int pos;
			for (pos = 0; pos < items.size(); pos++) {
				if (o.compareTo(items.get(pos)) < 0)
					break;
			}
			items.add(pos, o);
			totalScore += o.getScore();
		}

		@Override
		public Single take() {
			return takeFirst();
		}
	}

	/**
	 * @return score associated with content(s)
	 */
	public abstract double getScore();

	/**
	 * @return number of content keys associated with this instance
	 */
	public abstract int getCount();

	/**
	 * @return id used in referring to this instance
	 */
	public abstract String getId();
	
	/**
	 * @return a readable name of the associated content (e.g. full name)
	 */
	public abstract String getName();
	
	@Override
	public int compareTo(RankedContent o) {
		int result = -Double.compare(getScore(), o.getScore());
		if (result == 0) {
			return getName().compareTo(o.getName());
		}
		return result;
	}
}
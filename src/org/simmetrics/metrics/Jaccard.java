/*
 * #%L
 * Simmetrics Core
 * %%
 * Copyright (C) 2014 - 2016 Simmetrics Authors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * #L%
 */

package org.simmetrics.metrics;

import static com.google.common.collect.Sets.intersection;

import java.util.Set;

import org.simmetrics.SetDistance;
import org.simmetrics.SetMetric;

/**
 * Calculates the Jaccard distance and similarity coefficient over two
 * multisets. The similarity is defined as the size of the intersection divided
 * by the size of the union of the sample sets. The distance is obtained by
 * subtracting the Jaccard coefficient from 1.
 * <p>
 * <code>
 * similarity(a,b) = ∣a ∩ b∣ / ∣a ∪ b∣
 * <br>
 * distance(a,b) = 1 - similarity(a,b)
 * </code>
 * <p>
 * When <code>∣a ∪ b∣</code> is empty the multisets have no elements in common.
 * In this case the similarity is 0 by definition.
 * <p>
 * Unlike the generalized Jaccard index the occurrence (cardinality) of an entry
 * is not taken into account. E.g. {@code [hello, world]} and
 * {@code [hello, world, hello, world]} would be identical when compared with
 * the Jaccard index but are dissimilar when the generalized version is used.
 * <p>
 * Similar to the overlap coefficient which divides the intersection by the size
 * of the smaller of the two sets.
 * <p>
 * Similar to the dice coefficient which divides the shared information
 * (intersection) by sum of cardinalities.
 * <p>
 * This class is immutable and thread-safe.
 * 
 * @see GeneralizedJaccard
 * @see OverlapCoefficient
 * @see Dice
 * @see <a href="http://en.wikipedia.org/wiki/Jaccard_index">Wikipedia - Jaccard
 *      index</a>
 *
 * @param <T>
 *            type of the token
 * 
 */
public final class Jaccard<T> implements SetMetric<T>, SetDistance<T> {

	@Override
	public float compare(Set<T> a, Set<T> b) {

		if (a.isEmpty() && b.isEmpty()) {
			return 1.0f;
		}

		if (a.isEmpty() || b.isEmpty()) {
			return 0.0f;
		}
		
		// Smaller set first for performance improvement. 
		// See: note at Sets.intersection
		if(a.size() > b.size()){
			final Set<T> swap = a; a = b; b = swap;
		}
		
		final int intersection = intersection(a, b).size();

		// ∣a ∩ b∣ / ∣a ∪ b∣
		// Implementation note: The size of the union of two sets is equal to
		// the size of both sets minus the duplicate elements.
		return intersection / (float) (a.size() + b.size() - intersection);
	}

	@Override
	public float distance(Set<T> a, Set<T> b) {
		return 1.0f - compare(a, b);
	}

	@Override
	public String toString() {
		return "Jaccard";
	}

}

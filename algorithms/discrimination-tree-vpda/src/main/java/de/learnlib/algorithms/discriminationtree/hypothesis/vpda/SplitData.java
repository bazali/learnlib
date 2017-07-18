/* Copyright (C) 2017 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.discriminationtree.hypothesis.vpda;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Data associated with a {@link DTNode} while an enclosing subtree is being split.
 *
 * @param <I> input symbol type
 * @author Malte Isberner
 */
public class SplitData<I> {

	// TODO: HashSets/Maps are quite an overkill for booleans
	private final Set<Boolean> marks = new HashSet<>();

	private final Map<Boolean, TransList<I>> incomingTransitions = new HashMap<>();

	private Boolean stateLabel;

	/**
	 * Mark this node with the given label. The result indicates whether
	 * it has been newly marked.
	 *
	 * @param label the label to mark this node with
	 * @return {@code true} if the node was previously unmarked (wrt. to the given label),
	 * {@code false} otherwise
	 */
	public boolean mark(boolean label) {
		return marks.add(label);
	}

	public Set<Boolean> getLabels() {
		return marks;
	}

	/**
	 * Checks whether there is a state label associated with this node,
	 * regardless of its value.
	 *
	 * @return {@code true} if there is a state label ({@code true} or {@code false})
	 * associated with this node, {@code false} otherwise
	 */
	public boolean hasStateLabel() {
		return (stateLabel != null);
	}

	/**
	 * Sets the state label associated with this split data.
	 * <b>Note:</b> invoking this operation is illegal if a state label has already
	 * been set.
	 *
	 * @param label the state label
	 */
	public void setStateLabel(Boolean label) {
		assert !hasStateLabel();

		this.stateLabel = label;
	}

	/**
	 * Retrieves the state label associated with this split data.
	 * <b>Note:</b> invoking this operation is illegal if no state label
	 * has previously been set.
	 *
	 * @return the state label
	 */
	public Boolean getStateLabel() {
		assert hasStateLabel();

		return stateLabel;
	}

	/**
	 * Retrieves the list of incoming transitions for the respective label.
	 * This method will always return a non-{@code null} value.
	 *
	 * @param label the label
	 * @return the (possibly empty) list associated with the given state label
	 */
	public TransList<I> getIncoming(Boolean label) {
		TransList<I> list = incomingTransitions.get(label);
		if (list == null) {
			list = new TransList<>();
			incomingTransitions.put(label, list);
		}

		return list;
	}

	/**
	 * Checks whether the corresponding node is marked with the given label.
	 *
	 * @param label the label
	 * @return {@code true} if the corresponding node is marked with the given
	 * label, {@code false} otherwise
	 */
	public boolean isMarked(Boolean label) {
		return marks.contains(label);
	}

}
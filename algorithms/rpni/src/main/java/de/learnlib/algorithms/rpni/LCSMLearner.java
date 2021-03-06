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
package de.learnlib.algorithms.rpni;

import de.learnlib.algorithms.rpni.automata.CPTA;

import java.util.Comparator;

/**
 * "Low coverage state merging" heuristic of the RPNI algorithm. Implementation based on the book
 * "Grammatical Inference" by de la Higuera.
 *
 * @param <I> input alphabet type
 *
 * @author frohme
 */
public class LCSMLearner<I> extends AbstractCSMLearner<I> {

	@Override
	protected Comparator<Integer> getRedStateComparator(final CPTA<I> model) {
		// Comparing the regular way, gives us the node with the lowest coverage first
		return Comparator.comparingInt(model::getCoverage);
	}
}

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
package de.learnlib.algorithms.lstargeneric;

import de.learnlib.algorithms.rivestschapire.RivestSchapireMealy;
import de.learnlib.api.MembershipOracle;
import de.learnlib.testsupport.AbstractGrowingAlphabetMealyTest;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * @author frohme
 */
public class RivestSchapireMealyGrowingAlphabetTest
		extends AbstractGrowingAlphabetMealyTest<RivestSchapireMealy<Integer, Character>> {

	@Override
	protected RivestSchapireMealy<Integer, Character> getLearner(MembershipOracle<Integer, Word<Character>> oracle,
																 Alphabet<Integer> alphabet) {
		return new RivestSchapireMealy<>(alphabet, oracle);
	}
}

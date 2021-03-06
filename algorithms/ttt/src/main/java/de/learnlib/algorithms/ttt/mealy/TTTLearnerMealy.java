/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.algorithms.ttt.mealy;

import java.util.Map;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.ttt.base.BaseTTTLearner;
import de.learnlib.algorithms.ttt.base.DTNode;
import de.learnlib.algorithms.ttt.base.OutputInconsistency;
import de.learnlib.algorithms.ttt.base.TTTHypothesis.TTTEdge;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.acex.MealyOutInconsPrefixTransformAcex;
import de.learnlib.counterexamples.acex.OutInconsPrefixTransformAcex;
import de.learnlib.mealy.MealyUtil;
import de.learnlib.oracles.DefaultQuery;

public class TTTLearnerMealy<I, O> extends
		BaseTTTLearner<MealyMachine<?, I, ?, O>, I, Word<O>> implements LearningAlgorithm.MealyLearner<I, O> {

	@GenerateBuilder(defaults = BaseTTTLearner.BuilderDefaults.class)
	public TTTLearnerMealy(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle,
			AcexAnalyzer analyzer) {
		super(alphabet, oracle, new TTTHypothesisMealy<>(alphabet), analyzer);
	}

	@Override
	@SuppressWarnings("unchecked")
	public MealyMachine<?, I, ?, O> getHypothesisModel() {
		return (TTTHypothesisMealy<I, O>) hypothesis;
	}
	
	@Override
	protected TTTTransition<I,Word<O>> createTransition(TTTState<I,Word<O>> state, I sym) {
		TTTTransitionMealy<I,O> trans = new TTTTransitionMealy<>(state, sym);
		trans.output = query(state, Word.fromLetter(sym)).firstSymbol();
		return trans;
	}

	@Override
	protected Word<O> predictSuccOutcome(TTTTransition<I, Word<O>> trans,
			DTNode<I, Word<O>> succSeparator) {
		TTTTransitionMealy<I, O> mtrans = (TTTTransitionMealy<I, O>) trans;
		if (succSeparator == null) {
			return Word.fromLetter(mtrans.output);
		}
		return succSeparator.subtreeLabel(trans.getDTTarget()).prepend(mtrans.output);
	}

	@Override
	protected Word<O> computeHypothesisOutput(TTTState<I, Word<O>> state,
			Word<I> suffix) {
		TTTState<I,Word<O>> curr = state;
		
		WordBuilder<O> wb = new WordBuilder<>(suffix.length());
		
		for (I sym : suffix) {
			TTTTransitionMealy<I, O> trans = (TTTTransitionMealy<I,O>) hypothesis.getInternalTransition(curr, sym);
			wb.append(trans.output);
			curr = getAnyTarget(trans);
		}
		
		return wb.toWord();
	}

	@Override
	public GraphDOTHelper<TTTState<I,Word<O>>, TTTEdge<I, Word<O>>> getHypothesisDOTHelper() {
		return new EmptyDOTHelper<TTTState<I,Word<O>>,TTTEdge<I,Word<O>>>() {
			@Override
			public boolean getEdgeProperties(TTTState<I, Word<O>> src,
					TTTEdge<I, Word<O>> edge, TTTState<I, Word<O>> tgt,
					Map<String, String> properties) {
				if (!super.getEdgeProperties(src, edge, tgt, properties)) {
					return false;
				}
				String label = String.valueOf(edge.transition.getInput());
				label += " / ";
				TTTTransitionMealy<I, O> trans = (TTTTransitionMealy<I,O>) edge.transition;
				if (trans.output != null) {
					label += trans.output;
				}
				properties.put(EdgeAttrs.LABEL, label);
				return true;
			}
		};
	}

	@Override
	protected Word<O> succEffect(Word<O> effect) {
		return effect.subWord(1);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean refineHypothesisSingle(DefaultQuery<I,Word<O>> ceQuery) {
		DefaultQuery<I,Word<O>> shortenedCeQuery = MealyUtil.shortenCounterExample((TTTHypothesisMealy<I, O>) hypothesis, ceQuery);
		if (shortenedCeQuery != null) {
			return super.refineHypothesisSingle(shortenedCeQuery);
		}
		return false;
	}
	
	@Override
	protected OutInconsPrefixTransformAcex<I, Word<O>> deriveAcex(OutputInconsistency<I, Word<O>> outIncons) {
		TTTState<I, Word<O>> source = outIncons.srcState;
		Word<I> suffix = outIncons.suffix;
		
		OutInconsPrefixTransformAcex<I,Word<O>> acex = new MealyOutInconsPrefixTransformAcex<>(suffix, oracle,
				w -> getDeterministicState(source, w).getAccessSequence());
		
		acex.setEffect(0, outIncons.targetOut);
		Word<O> lastHypOut = computeHypothesisOutput(getAnySuccessor(source, suffix.prefix(-1)), suffix.suffix(1));
		acex.setEffect(suffix.length() - 1, lastHypOut);
		return acex;
	}
	
	@Override
	protected OutputInconsistency<I, Word<O>> findOutputInconsistency() {
		OutputInconsistency<I, Word<O>> best = null;
		
		for (TTTState<I, Word<O>> state : hypothesis.getStates()) {
			DTNode<I, Word<O>> node = state.getDTLeaf();
			while (!node.isRoot()) {
				Word<O> expectedOut = node.getParentEdgeLabel();
				node = node.getParent();
				Word<I> suffix = node.getDiscriminator();
				Word<O> hypOut = computeHypothesisOutput(state, suffix);
				int mismatchIdx = MealyUtil.findMismatch(expectedOut, hypOut);
				if (mismatchIdx != MealyUtil.NO_MISMATCH
						&& (best == null || mismatchIdx <= best.suffix.length())) {
					best = new OutputInconsistency<>(state, suffix.prefix(mismatchIdx + 1), expectedOut.prefix(mismatchIdx + 1));
				}
			}
		}
		return best;
	}
}

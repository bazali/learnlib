package de.learnlib.passive.rpni;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

class RedBlueMerge<SP,TP,S extends AbstractBlueFringePTAState<SP, TP, S>> {
	
	static final class FoldRecord<S extends AbstractBlueFringePTAState<?,?,S>> {
		public S q;
		public final S r;
		public int i = -1;
		
		public FoldRecord(S q, S r) {
			this.q = q;
			this.r = r;
		}
	}
	
	private final RichArray<S>[] succMod;
	private final RichArray<TP>[] transPropMod;
	private final RichArray<SP> propMod;
	private final int alphabetSize;
	
	private final S qr;
	private final S qb;
	
	@SuppressWarnings("unchecked")
	public RedBlueMerge(AbstractBlueFringePTA<SP,TP,S> pta, S qr, S qb) {
		if (!qr.isRed()) {
			throw new IllegalArgumentException("Merge target must be a red state");
		}
		if (!qb.isBlue()) {
			throw new IllegalArgumentException("Merge source must be a blue state");
		}
		int numRedStates = pta.getNumRedStates();
		this.succMod = new RichArray[numRedStates];
		this.transPropMod = new RichArray[numRedStates];
		this.propMod = new RichArray<>(numRedStates);
		this.alphabetSize = pta.alphabetSize;
		
		this.qr = qr;
		this.qb = qb;
	}
	
	public S getRedState() {
		return qr;
	}
	
	public S getBlueState() {
		return qb;
	}
	
	
	public boolean merge() {
		if (!mergeRedProperties(qr, qb)) {
			return false;
		}
		updateRedTransition(qb.parent, qb.parentInput, qr);
		
		Deque<FoldRecord<S>> stack = new ArrayDeque<>();
		stack.push(new FoldRecord<>(qr, qb));
		
		FoldRecord<S> curr;
		while ((curr = stack.peek()) != null) {
			int i = ++curr.i;
			
			
			if (i == alphabetSize) {
				stack.pop();
				continue;
			}
			
			S q = curr.q;
			S r = curr.r;

			S rSucc = r.getSuccessor(i);
			if (rSucc != null) {
				S qSucc = getSucc(q, i);
				if (qSucc != null) {
					if (qSucc.isRed()) {
						if (!mergeRedProperties(qSucc, rSucc)) {
							return false;
						}
					}
					else {
						SP rSuccSP = rSucc.property, qSuccSP = qSucc.property;
						RichArray<TP> rSuccTPs = rSucc.transProperties;
						RichArray<TP> qSuccTPs = qSucc.transProperties;
						
						SP newSP = null;
						RichArray<TP> newTPs = null;
						if (qSuccSP == null && rSuccSP != null) {
							newSP = rSuccSP;
						}
						else if (rSuccSP != null) { // && qSucc.property != null
							if (!Objects.equals(qSuccSP, rSuccSP)) {
								return false;
							}
						}
						
						if (rSuccTPs != null) {
							if (qSuccTPs != null) {
								RichArray<TP> mergedTPs
									= mergeTransProperties(qSuccTPs, rSuccTPs);
								if (mergedTPs == null) {
									return false;
								}
								else if (mergedTPs != qSuccTPs) {
									newTPs = mergedTPs;
								}
							}
							else {
								newTPs = rSuccTPs.clone();
							}
						}
						
						if (newSP != null || newTPs != null) {
							qSucc = cloneTopSucc(qSucc, i, stack, newTPs);
							if (newSP != null) {
								qSucc.property = newSP;
							}
						}
					}
					
					stack.push(new FoldRecord<>(qSucc, rSucc));
				}
				else {
					if (q.isRed()) {
						updateRedTransition(q, i, rSucc, r.getTransProperty(i));
					}
					else {
						q = cloneTop(q, stack);
						assert q.isCopy;
						q.setForeignSuccessor(i, rSucc, alphabetSize);
					}
				}
			}
		}
		
		return true;
	}
	
	private S cloneTopSucc(S succ, int i, Deque<FoldRecord<S>> stack, RichArray<TP> newTPs) {
		S succClone = (newTPs != null) ? succ.copy(newTPs) : succ.copy();
		if (succClone == succ) {
			return succ;
		}
		S top = stack.peek().q;
		if (top.isRed()) {
			updateRedTransition(top, i, succClone);
		}
		else {
			S topClone = cloneTop(top, stack);
			topClone.setForeignSuccessor(i, succClone, alphabetSize);
		}
		return succClone;
	}
	
	private S cloneTop(S topState, Deque<FoldRecord<S>> stack) {
		assert !topState.isRed();
		
		S topClone = topState.copy();
		if (topClone == topState) {
			return topState;
		}
		S currTgt = topClone;
		
		Iterator<FoldRecord<S>> it = stack.iterator();
		FoldRecord<S> currRec = it.next();
		assert currRec.q == topState;
		currRec.q = topClone;
		
		assert it.hasNext();
		currRec = it.next();
		S currSrc = currRec.q;
		
		while (!currSrc.isRed()) {
			S currSrcClone = currSrc.copy();
			currSrcClone.successors.update(currRec.i, currTgt);
			if (currSrcClone == currSrc) {
				return topClone; // we're done
			}
			currRec.q = currSrcClone;
			currTgt = currSrcClone;
			
			assert it.hasNext();
			currRec = it.next();
			currSrc = currRec.q;
		}
		
		assert currSrc.isRed();
		updateRedTransition(currSrc, currRec.i, currTgt);
		
		return topClone;
	}
	
	private RichArray<TP> getTransProperties(S q) {
		if (q.isRed()) {
			int qId = q.id;
			RichArray<TP> props = transPropMod[qId];
			if (props != null) {
				return props;
			}
		}
		return q.transProperties;
	}
	
	private SP getStateProperty(S q) {
		if (q.isRed()) {
			int qId = q.id;
			SP prop = propMod.get(qId);
			if (prop != null) {
				return prop;
			}
		}
		return q.property;
	}
	private S getSucc(S q, int i) {
		if (q.isRed()) {
			int qId = q.id;
			RichArray<S> modSuccs = succMod[qId];
			if (modSuccs != null) {
				return modSuccs.get(i);
			}
		}
		return q.getSuccessor(i);
	}
	
	private void updateRedTransition(S redSrc, int input, S tgt) {
		updateRedTransition(redSrc, input, tgt, null);
	}
	
	private void updateRedTransition(S redSrc, int input, S tgt, TP transProp) {
		assert redSrc.isRed();
		
		int id = redSrc.id;
		RichArray<S> newSuccs = succMod[id];
		if (newSuccs == null) {
			if (redSrc.successors == null) {
				newSuccs = new RichArray<>(alphabetSize);
			}
			else {
				newSuccs = redSrc.successors.clone();
			}
			succMod[id] = newSuccs;
		}
		newSuccs.update(input, tgt);
		if (transProp != null) {
			RichArray<TP> newTransProps = transPropMod[id];
			if (newTransProps == null) {
				if (redSrc.transProperties == null) {
					newTransProps = new RichArray<>(alphabetSize);
				}
				else {
					newTransProps = redSrc.transProperties.clone();
				}
				transPropMod[id] = newTransProps;
			}
			newTransProps.update(input, transProp);
		}
	}
	
	
	private boolean mergeRedProperties(S qr, S qb) {
		if (!mergeRedStateProperty(qr, qb)) {
			return false;
		}
		return mergeRedTransProperties(qr, qb);
	}
	
	private boolean mergeRedTransProperties(S qr, S qb) {
		assert qr.isRed();
		
		RichArray<TP> qbProps = qb.transProperties;
		if (qbProps == null) {
			return true;
		}
		RichArray<TP> qrProps = getTransProperties(qr);
		RichArray<TP> mergedProps = qbProps;
		if (qrProps != null) {
			mergedProps = mergeTransProperties(qrProps, qbProps);
			if (mergedProps == null) {
				return false;
			}
		}
		if (mergedProps != qrProps) {
			transPropMod[qr.id] = mergedProps;
		}
		return true;
	}
	
	private boolean mergeRedStateProperty(S qr, S qb) {
		assert qr.isRed();
		
		SP qbProp = qb.property;
		if (qbProp == null) {
			return true;
		}
		SP qrProp = getStateProperty(qr);
		if (qrProp != null) {
			return Objects.equals(qbProp, qrProp);
		}
		propMod.update(qr.id, qbProp);
		return true;
	}
	
	/**
	 * Merges two non-null transition property arrays. The behavior of this method is as follows:
	 * <ul>
	 * <li>if {@code tps1} subsumes {@code tps2}, then {@code tps1} is returned.</li>
	 * <li>otherwise, if {@code tps1} and {@code tps2} can be merged, a new {@link RichArray}
	 * containing the result of the merge is returned.
	 * <li>otherwise (i.e., if no merge is possible), {@code null} is returned.
	 * </ul>
	 * 
	 * @param tps1
	 * @param tps2
	 * @return
	 */
	private RichArray<TP> mergeTransProperties(RichArray<TP> tps1, RichArray<TP> tps2) {
		int len = tps1.length;
		int i;
		for (i = 0; i < len; i++) {
			TP tp1 = tps1.get(i);
			TP tp2 = tps2.get(i);
			if (tp2 != null) {
				if (tp1 != null) {
					if (!Objects.equals(tp1, tp2)) {
						return null;
					}
				}
				else {
					tps1 = tps1.clone();
					tps1.update(i++, tp2);
					break;
				}
			}
		}
		
		for (; i < len; i++) {
			TP tp1 = tps1.get(i);
			TP tp2 = tps2.get(i);
			if (tp2 != null) {
				if (tp1 != null) {
					if (!Objects.equals(tp1, tp2)) {
						return null;
					}
				}
				else {
					tps1.update(i, tp2);
				}
			}
		}
		
		return tps1;
	}
	
	public void apply(AbstractBlueFringePTA<SP,TP,S> pta, Consumer<? super BlueStateRef<S>> newFrontierConsumer) {
		int alphabetSize = pta.alphabetSize;
		
		for (int i = 0; i < succMod.length; i++) {
			S redState = pta.redStates.get(i);
			assert redState.isRed();
			RichArray<S> newSuccs = succMod[i];
			if (newSuccs != null) {
				int len = newSuccs.length;
				for (int j = 0; j < len; j++) {
					S newSucc = newSuccs.get(j);
					if (newSucc != null) {
						redState.setForeignSuccessor(j, newSucc, alphabetSize);
						Color c = newSucc.getColor();
						if (c != Color.RED) {
							newSucc.parent = redState;
							newSucc.parentInput = j;
							incorporate(newSucc);
							if (c != Color.BLUE) {
								newFrontierConsumer.accept(newSucc.makeBlue());
							}
						}
					}
				}
			}
			
			SP newProp = propMod.get(i);
			if (newProp != null) {
				redState.property = newProp;
			}
		}
	}
	
	private void incorporate(S state) {
		if (!state.isCopy) {
			return;
		}
		state.isCopy = false;
		Deque<S> queue = new ArrayDeque<>();
		queue.offer(state);
		
		S curr;
		
		while ((curr = queue.poll()) != null) {
			RichArray<S> succs = curr.successors;
			if (succs == null) {
				continue;
			}
			for (int i = 0; i < alphabetSize; i++) {
				S succ = succs.get(i);
				if (succ != null) {
					succ.parent = curr;
					succ.parentInput = i;
					if (succ.isCopy) {
						succ.isCopy = false;
						queue.offer(succ);
					}
				}
			}
		}
	}
}
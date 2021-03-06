/* Copyright (C) 2013-2015 TU Dortmund
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
package de.learnlib.examples.mealy;

import static de.learnlib.examples.mealy.ExampleCoffeeMachine.Input.BUTTON;
import static de.learnlib.examples.mealy.ExampleCoffeeMachine.Input.CLEAN;
import static de.learnlib.examples.mealy.ExampleCoffeeMachine.Input.POD;
import static de.learnlib.examples.mealy.ExampleCoffeeMachine.Input.WATER;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.DefaultLearningExample.DefaultMealyLearningExample;
import de.learnlib.examples.mealy.ExampleCoffeeMachine.Input;

/**
 * This example represents the Coffee Machine example from
 * Steffen et al. "Introduction to Active Automata Learning from
 * a Practical Perspective" (Figure 3)
 * 
 * @author Maik Merten 
 */
public class ExampleCoffeeMachine extends DefaultMealyLearningExample<Input,String> {


	public static enum Input {
		WATER,
		POD,
		BUTTON,
		CLEAN
	}
	
	public final static String out_ok = "ok";
	public final static String out_error = "error";
	public final static String out_coffee = "coffee!";
	
	
	public static Alphabet<Input> createInputAlphabet() {
		return Alphabets.fromEnum(Input.class);
	}
	
	
	
    /**
     * Construct and return a machine representation of this example
     * 
     * @return a Mealy machine representing the coffee machine example
     */   
    public static <S,T,A extends MutableMealyMachine<S,? super Input,T,? super String>>
    A constructMachine(A machine) {
//		S a = machine.addInitialState(), b = machine.addState(), c = machine
//				.addState(), d = machine.addState(), e = machine.addState(), f = machine
//				.addState();
//
//		machine.addTransition(a, WATER, c, out_ok);
//		machine.addTransition(a, POD, b, out_ok);
//		machine.addTransition(a, BUTTON, f, out_error);
//		machine.addTransition(a, CLEAN, a, out_ok);
//
//		machine.addTransition(b, WATER, d, out_ok);
//		machine.addTransition(b, POD, b, out_ok);
//		machine.addTransition(b, BUTTON, f, out_error);
//		machine.addTransition(b, CLEAN, a, out_ok);
//
//		machine.addTransition(c, WATER, c, out_ok);
//		machine.addTransition(c, POD, d, out_ok);
//		machine.addTransition(c, BUTTON, f, out_error);
//		machine.addTransition(c, CLEAN, a, out_ok);
//
//		machine.addTransition(d, WATER, d, out_ok);
//		machine.addTransition(d, POD, d, out_ok);
//		machine.addTransition(d, BUTTON, e, out_coffee);
//		machine.addTransition(d, CLEAN, a, out_ok);
//
//		machine.addTransition(e, WATER, f, out_error);
//		machine.addTransition(e, POD, f, out_error);
//		machine.addTransition(e, BUTTON, f, out_error);
//		machine.addTransition(e, CLEAN, a, out_ok);
//
//		machine.addTransition(f, WATER, f, out_error);
//		machine.addTransition(f, POD, f, out_error);
//		machine.addTransition(f, BUTTON, f, out_error);
//		machine.addTransition(f, CLEAN, f, out_error);

    	machine = AutomatonBuilders.forMealy(machine)
    			.withInitial("a")
    			.from("a")
    				.on(WATER).withOutput(out_ok).to("c")
    				.on(POD).withOutput(out_ok).to("b")
    				.on(BUTTON).withOutput(out_error).to("f")
    				.on(CLEAN).withOutput(out_ok).loop()
    			.from("b")
    				.on(WATER).withOutput(out_ok).to("d")
    				.on(POD).withOutput(out_ok).loop()
    				.on(BUTTON).withOutput(out_error).to("f")
    				.on(CLEAN).withOutput(out_ok).to("a")
    			.from("c")
    				.on(WATER).withOutput(out_ok).loop()
    				.on(POD).withOutput(out_ok).to("d")
    				.on(BUTTON).withOutput(out_error).to("f")
    				.on(CLEAN).withOutput(out_ok).to("a")
    			.from("d")
    				.on(WATER, POD).withOutput(out_ok).loop()
    				.on(BUTTON).withOutput(out_coffee).to("e")
    				.on(CLEAN).withOutput(out_ok).to("a")
    			.from("e")
    				.on(WATER, POD, BUTTON).withOutput(out_error).to("f")
    				.on(CLEAN).withOutput(out_ok).to("a")
    			.from("f")
    				.on(WATER, POD, BUTTON, CLEAN).withOutput(out_error).loop()
    		.create();
    	
    	
    	return machine;
    }
    
    public static CompactMealy<Input,String> constructMachine() {
    	return constructMachine(new CompactMealy<>(createInputAlphabet()));
    }

    
    public static ExampleCoffeeMachine createExample() {
    	return new ExampleCoffeeMachine();
    }
	
	public ExampleCoffeeMachine() {
		super(constructMachine());
	}
	
}

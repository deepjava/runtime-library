/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.deepjava.runtime.mpc555.demo;

import java.io.PrintStream;

import org.deepjava.runtime.mpc555.driver.SCI;
import org.deepjava.runtime.ppc32.Decrementer;

/* changes:
 * 22.02.11 NTB/Martin Z�ger	OutT replaced by System.out
 * 11.11.10	NTB/Urs Graf		creation
 */

/**
 * Simple demo application how to use the <code>Decrementer</code>.
 * This application simply outputs the character 'x' once
 * per second over the SCI2.
 */
public class DecrementerDemo extends Decrementer {
	static DecrementerDemo decTest; 
	
	/**
	 * Outputs 'x' once a second.
	 */
	public void action () {
		System.out.print('x');
	}
	
	static {
		// Initialize the SCI2 (9600 8N1) and use it for System.out
		SCI sci = SCI.getInstance(SCI.pSCI2);
		sci.start(9600, SCI.NO_PARITY, (short)8);
		System.out = new PrintStream(sci.out);
		System.err = System.out;
		
		// Create and install the Decrementer demo
		decTest = new DecrementerDemo(); 
		decTest.decPeriodUs = 1000000;
		Decrementer.install(decTest);
	}
}
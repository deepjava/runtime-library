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

package org.deepjava.runtime.mpc5200.demo;

import java.io.PrintStream;

import org.deepjava.runtime.mpc5200.driver.UART6;
import org.deepjava.runtime.ppc32.Task;

/**
 * Demo for System.out using UART on PSC6.
 * 
 * @author Urs Graf
 */
public class UART6Demo extends Task {
	
	public void action() {
		// Write a single character to the stdout
		System.out.print('.');
	}

	static {
		// Initialize UART (9600 8N1)
		UART6.start(9600, UART6.NO_PARITY, (short)8);
		
		// Use the UART3 for stdout and stderr
		System.out = new PrintStream(UART6.out);
		System.err = System.out;
		
		// Print a string to the stdout
		System.out.print("System.out demo (UART6)");
		
		// Create and install the demo task
		Task t = new UART6Demo();
		t.period = 500;
		Task.install(t);
	}
}

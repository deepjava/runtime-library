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

import org.deepjava.runtime.mpc5200.driver.DAC_MAX5500;
import org.deepjava.runtime.mpc5200.driver.SPI_FQD;
import org.deepjava.runtime.mpc5200.driver.UART3;
import org.deepjava.runtime.ppc32.Task;

/**
 * Test class demonstrating the use of a <code>MAX5500</code> DAC connected to a PSC on the mpc5200.<br>
 * The PSC runs in SPI mode. All four channels are driven with a ramp.  
 * 
 * @author Urs Graf
 */
public class SPIDemo extends Task {
	static short i;
	
	public void action() {
//		System.out.print('.');
		DAC_MAX5500.send(0, i);
		DAC_MAX5500.send(1, i);
		DAC_MAX5500.send(2, i);
		DAC_MAX5500.send(3, i);
		i += 0x40;
		if (i > 0xfff) i = 0;
		SPI_FQD.receive();
		System.out.print(SPI_FQD.getEncoder0());
		System.out.print('\t');
		System.out.println(SPI_FQD.getEncoder1());
	}

	static {
		// Use the UART3 for stdout and stderr
		UART3.start(9600, UART3.NO_PARITY, (short)8);
		System.out = new PrintStream(UART3.out);
		System.err = System.out;

		System.out.print("started");
		DAC_MAX5500.init();
		SPI_FQD.init();
		
		// Create and install the demo task
		Task t = new SPIDemo();
		t.period = 100;
		Task.install(t);
	}
}

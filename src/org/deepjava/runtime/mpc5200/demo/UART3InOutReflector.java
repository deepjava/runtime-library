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

import org.deepjava.runtime.mpc5200.driver.*;
import org.deepjava.runtime.ppc32.Task;

/**
 * Demo for InputStream and OutputStream using UART3.<br>
 * Received characters will be sent back immediately.
 * 
 * @author Urs Graf
 */
public class UART3InOutReflector extends Task {
	static UARTOutputStream out;
	static UARTInputStream in;
	
	public void action() {
		// reflect input on stdin to stdout
		if (in.available() > 0)	out.write(in.read());
	}

	static {
		// Initialize SCI2 (9600 8N1)
		UART3.start(9600, UART3.NO_PARITY, (short)8);
		out = new UARTOutputStream(UARTOutputStream.pPSC3);
		in = new UARTInputStream(UARTInputStream.pPSC3);
		out.write((byte)'x');
		
		Task t = new UART3InOutReflector();
		t.period = 0;
		Task.install(t);
	}
}

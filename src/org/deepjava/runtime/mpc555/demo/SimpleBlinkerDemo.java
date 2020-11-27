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

import org.deepjava.runtime.mpc555.driver.MPIOSM_DIO;
import org.deepjava.runtime.ppc32.Task;

/* CHANGES:
 * 24.02.2011	NTB/Zueger	creation
 */

/**
 * Simple blinker application demo.
 * Connect an LED to pin MPIOSM12. The LED will be toggled every half second.
 */
public class SimpleBlinkerDemo extends Task {
	
	static MPIOSM_DIO out;
	
	/**
	 * Toggles the LED.
	 */
	public void action(){
		out.set(!out.get());
	}
	
	static {
		out = new MPIOSM_DIO(12, true); // Initialize MPIOSM12 as output
		out.set(false); // Set MPIOSM12 to low
		
		// Create and install the task
		SimpleBlinkerDemo t = new SimpleBlinkerDemo();
		t.period = 500;
		Task.install(t);
	}

}

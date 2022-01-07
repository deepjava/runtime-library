/*
 * Copyright 2011 - 2022 NTB University of Applied Sciences in Technology
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

package org.deepjava.runtime.zynq7000.microzed.driver;

import org.deepjava.runtime.zynq7000.Izynq7000;
import org.deepjava.unsafe.arm.US;

/**
 * Driver to use the MIO pins on the MicroZed board as digital input or outputs.<br>
 * 
 * The driver supports digital functions on MIO0, MIO9 .. MIO15.<br>
 * Please make sure, that no other driver uses the same pins for alternative functions.
 */
public class MIO_DIO implements Izynq7000 {
	
	/**
	 * Initialize a MIO pin as digital I/O.
	 * 
	 * @param channel Select pin 0, 9 .. 15
	 * @param out Set I/O direction,<code>true</code> =&gt;output, <code>false</code> =&gt; input
	 */
	public static void init(int channel, boolean out) {
		int reg = US.GET4(GPIO_DIR0);
		if (out) reg |= (1 << channel); else reg &= ~(1 << channel); 
		US.PUT4(GPIO_DIR0, reg);
		if (out) {
			reg = US.GET4(GPIO_OUT_EN0);
			reg |= (1 << channel);
			US.PUT4(GPIO_OUT_EN0, reg);
		}
	}

	/**
	 * Read the digital value of the corresponding <code>channel</code>
	 * 
	 * @param channel Select pin 0, 9 .. 15
	 * @return Logical level of <code>channel</code>
	 */
	public static boolean in(int channel) {
		int reg = US.GET4(GPIO_IN0);
		return (reg & (1 << channel)) == (1 << channel);
	}

	/**
	 * Set the logical level <code>val</code> of the corresponding <code>channel</code>
	 *  
	 * @param channel Select pin 0, 9 .. 15
	 * @param val Logical level
	 */
	public static void out(int channel, boolean val) {
		int reg = ~(1 << (16 + channel));
		if (!val) reg &= 0xffff0000;
		US.PUT4(GPIO_MASK_LSW0, reg);
	}

}

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

import org.deepjava.runtime.mpc5200.driver.UART3;
import org.deepjava.runtime.mpc5200.driver.can.EPOS;
import org.deepjava.runtime.ppc32.Task;

/** 
 * Test class for an EPOS module (motor controller) connected to the CAN1 bus on the mpc5200.<br>
 * A motor position is moved forward and backward.
 * 
 * @author Urs Graf
 *
 */
public class EPOStest1 extends Task {
	static EPOS drive1;
	static int pos = 0;
	static boolean toggle = true;

	public void action() {
		if (toggle) pos += 10; else pos -=10;
		drive1.setPosition(pos);
		if (this.nofActivations % 100 == 0) {
//			CANopen.dispMsgBuf2();
//			drive1.sendSync(); 
			System.out.print('.');
		}
		if (this.nofActivations % 500 == 0) {
			if (toggle) drive1.setOutAC(); else drive1.setOutBD();
			toggle = !toggle;
		}
	}
	

	static {	
		UART3.start(9600, UART3.NO_PARITY, (short)8);
		System.out = new PrintStream(UART3.out);
		System.out.println("start");
		drive1 = new EPOS((byte)1);
		drive1.start();
		drive1.initOutABCD(); 
		drive1.setOutBD();
		drive1.setParams();
		drive1.setPDOtransmission();
		drive1.startNode();
		drive1.enablePower();
		Task t = new EPOStest1();	
		t.period = 10;
		Task.install(t);
	}
}

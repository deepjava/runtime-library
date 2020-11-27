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

import org.deepjava.runtime.mpc555.driver.BlueRSCmdInt;
import org.deepjava.runtime.mpc555.driver.MPIOSM_DIO;
import org.deepjava.runtime.mpc555.driver.SCI;
import org.deepjava.runtime.ppc32.Task;

/* CHANGES:
 * 09.03.11 NTB/Roger Millischer	adapted to the new deep environment
 */

/**
 * Simple application for demonstrating the usage of the
 * BlueRSCmdInt driver with a Stollmann BlueRS+I module.
 */
public class BlueRSCmdIntDemo extends Task {
	private final static String partner = "008025003E46";
	private static final int resetPin = 11;
	private static int cmd = 1;

	public void action() { // Print status changes and received commands
		int status = BlueRSCmdInt.getStatus();
		printStatus(status);
		if (status == BlueRSCmdInt.getStatus()) {
			int rxCmd = BlueRSCmdInt.getReceivedCmd();
			if (rxCmd > 0) {
				System.out.print("Cmd received -> ");
				System.out.println(rxCmd);
			}
		}
	}

	public static void connect() { // Connect to the partner module
		if (BlueRSCmdInt.getStatus() == BlueRSCmdInt.disconnected)
			BlueRSCmdInt.connect(partner);
		else 
			System.out.println("Wrong mode");
	}

	public static void disconnect() {// Disconnect from the partner module
		if (BlueRSCmdInt.getStatus() == BlueRSCmdInt.connected)
			BlueRSCmdInt.disconnect();
		else
			System.out.println("Wrong mode");
	}

	public static void sendCmd() {// Send a command
		if (BlueRSCmdInt.getStatus() == BlueRSCmdInt.connected)
			BlueRSCmdInt.sendCommand(cmd++);
		else
			System.out.println("Wrong mode");
	}

	private static int lastStatus = -1;

	private void printStatus(int status) {
		if (status != lastStatus) {
			lastStatus = status;
			switch (status) {
			case BlueRSCmdInt.disconnected:
				System.out.println("BlueRS -> Disconnected");
				break;
			case BlueRSCmdInt.connecting:
				System.out.println("BlueRS -> Connecting");
				break;
			case BlueRSCmdInt.connected:
				System.out.println("BlueRS -> Connected");
				break;
			case BlueRSCmdInt.disconnecting:
				System.out.println("BlueRS -> Disconnecting");
				break;
			}
		}
	}

	static {
		//initialize SCI1
		SCI sci = SCI.getInstance(SCI.pSCI1);
		sci.start(9600, SCI.NO_PARITY, (short) 8);
		//hook SCI1 to System.out
		System.out = new PrintStream(sci.out);

		MPIOSM_DIO out = new MPIOSM_DIO(resetPin, true); // Init Mpiosm
		out.set(false); // Reset BlueRS
		Task t = new BlueRSCmdIntDemo();
		t.period = 100;
		Task.install(t);
		out.set(true);
	}
}
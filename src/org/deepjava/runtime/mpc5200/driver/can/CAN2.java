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

package org.deepjava.runtime.mpc5200.driver.can;

import org.deepjava.runtime.mpc5200.Impc5200;
import org.deepjava.unsafe.US;

public class CAN2 implements Impc5200 {
	// supports only standard frame format (11Bit itentifier)
	// doesn't use interrupts
	
	static final int rx1BufNo = 1;
	static final int rx2BufNo = 2;
	
	private static byte[] data = new byte[14];

	public static void setTxBuf(int len, int id, boolean rtr, byte data[]) {
		US.PUT1(MSCAN2Base + CANTBSEL, 7);	// select buffer 0
		int idReg = id << 5;
		if (rtr) idReg |= 0x10;	// remote frame
		US.PUT2(MSCAN2Base + CANTXIR0, idReg);	// load id
		for (int i = 0; i < len; i += 2) {	// fill data
			US.PUT2(MSCAN2Base + CANTXDSR0 + i*2, (data[i]<<8) | data[i+1]);
		}
		US.PUT1(MSCAN2Base + CANTXDLR, len);	// set length
		US.PUT1(MSCAN2Base + CANTXTBPR, 0);	// set priority to 0
		US.PUT1(MSCAN2Base + CANTFLG, 1);	// start transmission for buffer 0
	}
	
	public static void setMsgBufRx(int len, int id, boolean rtr, byte data[]) {
		US.PUT1(MSCAN2Base + CANCTL0, 0x01);	// enter initialization mode
		while ((US.GET1(MSCAN2Base + CANCTL1) & 1) == 0);	// wait for acknowledge bit
		
		int idReg = id << 5;
		US.PUT2(MSCAN2Base + CANIDAR0, idReg);	// filter 0: id
		US.PUT2(MSCAN2Base + CANIDMR0, 0x000f);	// filter 0: mask, all bits must match
		
		US.PUT1(MSCAN2Base + CANCTL0, 0);	// exit initialization mode
		while ((US.GET1(MSCAN2Base + CANCTL0) & 1) == 1);	// wait for normal mode
		while ((US.GET1(MSCAN2Base + CANCTL0) & 0x10) == 0);	// wait for synchronization
		US.PUT1(MSCAN2Base + CANRFLG, 0x01);	// clear receive flag
	}
	
	public static void waitForTxComplete(int bufNr) {
		while ((US.GET1(MSCAN2Base + CANTFLG) & (1 << bufNr)) != 1 << bufNr);	// wait for end of transfer
	}
	
	public static void waitForRxComplete() {
		while ((US.GET1(MSCAN2Base + CANRFLG) & 1) != 1);	// wait for end of transfer
	}

	public static byte[] getMsgBuf(int bufNr) {
		for (int i = 0; i < 6; i++) {
			data[i*2] = US.GET1(MSCAN2Base + CANRXFG + i*4);
			data[i*2 + 1] = US.GET1(MSCAN2Base + CANRXFG + i*4 + 1);
		}
		data[12] = US.GET1(MSCAN2Base + CANRXFG + 0x18);
		return data;
	}

	public static void init() {
		US.PUT4(GPSPCR, US.GET4(GPSPCR) | 0x10);	// use pins on PCS2 for CAN
		US.PUT1(MSCAN2Base + CANCTL0, 0x01);	// enter initialization mode
		while ((US.GET1(MSCAN2Base + CANCTL1) & 1) == 0);	// wait for acknowledge bit
		
		US.PUT1(MSCAN2Base + CANCTL1, 0x80);	// enable, use IP clock of 66MHz
		// S-clock = 22MHz -> time quantum = 45.45ns
		US.PUT1(MSCAN2Base + CANBTR0, 0xc2);	// SJW = 4 time quanta, prescaler = 3
		US.PUT1(MSCAN2Base + CANBTR1, 0x6d);	// 1 sample, time segment 2 = 7, time segment 1 = 14
		US.PUT1(MSCAN2Base + CANIDAC, 0x10);	// 4 16-bit filters
		US.PUT1(MSCAN2Base + CANRFLG, 0x01);	// clear receive flag
		
		US.PUT1(MSCAN2Base + CANCTL0, 0);	// exit initialization mode
		while ((US.GET1(MSCAN2Base + CANCTL0) & 1) == 1);	// wait for normal mode
		while ((US.GET1(MSCAN2Base + CANCTL0) & 0x10) == 0);	// wait for synchronization
	}
}

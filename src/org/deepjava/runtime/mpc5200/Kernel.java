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

package org.deepjava.runtime.mpc5200;
import org.deepjava.runtime.IdeepCompilerConstants;
import org.deepjava.runtime.ppc32.Heap;
import org.deepjava.runtime.ppc32.Ippc32;
import org.deepjava.unsafe.ppc.US;

/* changes:
 * 21.6.12	NTB/Urs Graf		creation
 */

/**
 *  This is the kernel class. It provides basic functionalities and does the booting-up. 
 */
public class Kernel implements Ippc32, Impc5200, IdeepCompilerConstants {
	final static int stackEndPattern = 0xee22dd33;
	static int loopAddr;
	static int cmdAddr;
	
	@SuppressWarnings("unused")
	private static void loop() {	// endless loop
		while (true) {
			try {
				if (cmdAddr != -1) {
					US.PUTSPR(LR, cmdAddr);	
					US.ASM("bclrl always, 0");
					cmdAddr = -1;
				}
			} catch (Exception e) {
				cmdAddr = -1;	// stop trying to run the same method
				e.printStackTrace();
				Kernel.blink(2);
			}
		}
	}
	
	/** 
	 * Reads the system time.
	 * 
	 * @return System time in \u00b5s
	 */
	public static long timeUs() {
		int high1, high2, low;
		do {
			high1 = US.GETSPR(TBUread); 
			low = US.GETSPR(TBLread);
			high2 = US.GETSPR(TBUread); 
		} while (high1 != high2);
		long time = ((long)high1 << 32) | ((long)low & 0xffffffffL);
		return time / 33;
	}
	
	/** 
	 * Reads the system time.
	 * 
	 * @return System time in ns
	 */
	public static long timeNs() {
		int high1, high2, low;
		do {
			high1 = US.GETSPR(TBUread); 
			low = US.GETSPR(TBLread);
			high2 = US.GETSPR(TBUread); 
		} while (high1 != high2);
		long time = ((long)high1 << 32) | ((long)low & 0xffffffffL);
		return time * 30;
	}
	
	/** 
	 * Blinks LED on GPIO_WKUP_7, nTimes with approx. 100\u00b5s high time and 100\u00b5s low time, blocks for 1s
	 * 
	 * @param nTimes Number of times the led blinks.
	 */
	public static void blink(int nTimes) { 
		US.PUT4(GPWER, US.GET4(GPWER) | 0x80000000);	// enable GPIO use
		US.PUT4(GPWDDR, US.GET4(GPWDDR) | 0x80000000);	// make output
		int delay = 300000;
		for (int i = 0; i < nTimes; i++) {
			US.PUT4(GPWOUT, US.GET4(GPWOUT) & ~0x80000000);
			for (int k = 0; k < delay; k++);
			US.PUT4(GPWOUT, US.GET4(GPWOUT) | 0x80000000);
			for (int k = 0; k < delay; k++);
		}
		for (int k = 0; k < (10 * delay + nTimes * 2 * delay); k++);
	}

	/**
	 * Enables interrupts globally. 
	 * Individual interrupts for peripheral components must be enabled locally.
	 */
	public static void enableInterrupts() {
		US.ASM("mfmsr r0");	// enable interrupts
		US.PUTGPR(0, US.GETGPR(0) | (1 << 15));
		US.ASM("mtmsr r0");	
	}

	/** 
	 * Blinks LED on GPIO_WKUP_7 if stack end was overwritten
	 */
	public static void checkStack() { 
		int stackOffset = US.GET4(sysTabBaseAddr + stStackOffset);
		int stackBase = US.GET4(sysTabBaseAddr + stackOffset + 4);
		if (US.GET4(stackBase) != stackEndPattern) while (true) blink(3);
	}

	private static int FCS(int begin, int end) {
		int crc  = 0xffffffff;  // initial content
		final int poly = 0xedb88320;  // reverse polynomial 0x04c11db7
		int addr = begin;
		while (addr < end) {
			byte b = US.GET1(addr);
			int temp = (crc ^ b) & 0xff;
			for (int i = 0; i < 8; i++) { // read 8 bits one at a time
				if ((temp & 1) == 1) temp = (temp >>> 1) ^ poly;
				else temp = (temp >>> 1);
			}
			crc = (crc >>> 8) ^ temp;
			addr++;
		}
		return crc;
	}
	
	private static void boot() {
		US.PUT4(XLBACR, 0x00002006);	// time base enable, data and address timeout enable
		
		// mark stack end with specific pattern
		int stackOffset = US.GET4(sysTabBaseAddr + stStackOffset);
		int stackBase = US.GET4(sysTabBaseAddr + stackOffset + 4);
		US.PUT4(stackBase, stackEndPattern);

		int classConstOffset = US.GET4(sysTabBaseAddr);
//		int state = 0;
		int kernelClinitAddr = US.GET4(sysTabBaseAddr + stKernelClinitAddr); 
		while (true) {
			// get addresses of classes from system table
			int constBlkBase = US.GET4(sysTabBaseAddr + classConstOffset);
			if (constBlkBase == 0) break;

			// check integrity of constant block for each class
			int constBlkSize = US.GET4(constBlkBase);
			if (FCS(constBlkBase, constBlkBase + constBlkSize) != 0) while(true) blink(1);

			// initialize class variables
			int varBase = US.GET4(constBlkBase + cblkVarBaseOffset);
			int varSize = US.GET4(constBlkBase + cblkVarSizeOffset);
			int begin = varBase;
			int end = varBase + varSize;
			while (begin < end) {US.PUT4(begin, 0); begin += 4;}
			
//			state++; 
			classConstOffset += 4;
		}
		classConstOffset = US.GET4(sysTabBaseAddr);
		Heap.sysTabBaseAddr = sysTabBaseAddr;
		while (true) {
			// get addresses of classes from system table
			int constBlkBase = US.GET4(sysTabBaseAddr + classConstOffset);
			if (constBlkBase == 0) break;

			// initialize classes
			int clinitAddr = US.GET4(constBlkBase + cblkClinitAddrOffset);
			if (clinitAddr != -1) {	
				if (clinitAddr != kernelClinitAddr) {	// skip kernel 
					US.PUTSPR(LR, clinitAddr);
					US.ASM("bclrl always, 0");
				} else {	// kernel
					loopAddr = US.ADR_OF_METHOD("org/deepjava/runtime/mpc5200/Kernel/loop");
				}
			}
			// the direct call to clinitAddr destroys volatile registers, hence make sure
			// the variable classConstOffset is forced into nonvolatile register
			// this is done by call to empty()
			empty();
			classConstOffset += 4;
		}
	}
	
	private static void empty() {
	}

	static {
		try {
			boot();
			cmdAddr = -1;	// must be after class variables are zeroed by boot
//			US.ASM("mfmsr r0");	// enable interrupts
//			US.PUTGPR(0, US.GETGPR(0) | (1 << 15));
//			US.ASM("mtmsr r0");	
			US.PUTSPR(HID0, 0x8000);		// switch on instruction cache
			US.PUTSPR(LR, loopAddr);
			US.ASM("bclrl always, 0");
		} catch (Exception e) {
			e.printStackTrace();
			while (true) Kernel.blink(5);
		}
	}

}
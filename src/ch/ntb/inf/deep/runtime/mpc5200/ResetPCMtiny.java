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

package ch.ntb.inf.deep.runtime.mpc5200;

import ch.ntb.inf.deep.runtime.IdeepCompilerConstants;
import ch.ntb.inf.deep.runtime.ppc32.Ippc32;
import ch.ntb.inf.deep.runtime.ppc32.PPCException;
import ch.ntb.inf.deep.unsafe.ppc.US;

/* changes:
 * 21.6.12	NTB/GRAU	creation
 */
/**
 * The class for the PPC reset exception.<br>
 * The stack pointer will be initialized and the program counter will be
 * set to the beginning of the class initializer of the kernel.
 * If the code runs out of the flash, it will copy itself from there to the SDRAM.
 * 
 * @author Urs Graf
 */
class ResetPCMtiny extends PPCException implements Ippc32, IphyCoreMpc5200tiny, IdeepCompilerConstants {
	
	static void reset() {
		int sprg0 = US.GETSPR(272);
		int baseAddr;
		if (sprg0 == 0x55aa) {	// boot from ram
			baseAddr = extRamBase + sysTabBaseAddr;
		} else { // boot from flash
			baseAddr = extFlashBase + sysTabBaseAddr;
			US.PUT4(0x80000000, MemBaseAddr >> 16);	// switch memory base address
			
			US.PUTGPR(1, SRAM_BaseAddr + 0x100);	// set temporary stack pointer
			int addr = US.ADR_OF_METHOD("ch/ntb/inf/deep/runtime/mpc5200/ResetPCMtiny/copyCode");
			US.PUTSPR(LR, addr + 0xfff00000);
			US.ASM("bclrl always, 0");
		}

		// use led for debugging of reset
//			US.PUT4(GPWER, US.GET4(GPWER) | 0x80000000);	// enable GPIO use
//			US.PUT4(GPWDDR, US.GET4(GPWDDR) | 0x80000000);	// make output
//			US.PUT4(GPWOUT, 0x00000000);	// switch on led
//			US.PUT4(GPWOUT, 0x80000000);	// switch off led 1

		if (sprg0 == 0x55aa) {	// boot from ram
			baseAddr = extRamBase + sysTabBaseAddr;	// value of baseAddr is lost by calling copyCode!!!
		} else { // boot from flash
			baseAddr = extFlashBase + sysTabBaseAddr;
		}
		int stackOffset = US.GET4(baseAddr + stStackOffset);
		int stackBase = US.GET4(baseAddr + stackOffset + 4);
		int stackSize = US.GET4(baseAddr + stackOffset + 8);
		US.PUTGPR(1, stackBase + stackSize - 4);	// set stack pointer
		
		// at this stage, the code has to be found in the SDRAM
		int kernelClinitAddr = US.GET4(baseAddr + stKernelClinitAddr);
		US.PUTSPR(SRR0, kernelClinitAddr);
		US.PUTSPR(SRR1, SRR1init);
		US.ASM("rfi");
	}
	
	@SuppressWarnings("unused")
	private static void copyCode() {
		// configure CS0 for boot flash 
		US.PUT4(CS0START, 0x0000ff00);	// start address = 0xff000000
		US.PUT4(CS0STOP, 0x0000ffff); 	// stop address = 0xffffffff, size = 16MB
		US.PUT4(CS0CR, 0x0008fd00);	// 8 wait states, multiplexed, ack, enabled, 25 addr. lines, 16 bit data, rw
		US.PUT4(CSCR, 0x01000000);	// CS master enable
		US.PUT4(IPBICR, 0x00010001);	// enable CS0, disable CSboot, enable wait states

		// configure CS for SDRAM 
		US.PUT4(SDRAMCS0, 0x0000001a);	// 128MB, start at 0

		// configure SDRAM controller for DDR 133MHz 
		US.PUT4(SDRAMCR1, 0x73722930);	// config 1	
		US.PUT4(SDRAMCR2, 0x47770000);	// config 2
		US.PUT4(SDRAMCR, 0xe15f0f00);	
		US.PUT4(SDRAMCR, 0xe15f0f02);	
		US.PUT4(SDRAMMR, 0x40090000);	
		US.PUT4(SDRAMMR, 0x058d0000);	
		US.PUT4(SDRAMCR, 0xe15f0f02);	
		US.PUT4(SDRAMCR, 0xe15f0f04);	
		US.PUT4(SDRAMCR, 0xe15f0f04);	
		US.PUT4(SDRAMMR, 0x018d0000);				
		US.PUT4(SDRAMCR, 0x715f0f00);	

		// copy code and const from flash to SDRAM
		int baseAddr = extFlashBase + sysTabBaseAddr;;
		int srcAddr = extFlashBase + US.GET4(baseAddr + stResetOffset);
		int dstAddr = extRamBase + US.GET4(baseAddr + stResetOffset);
		int size = US.GET4(baseAddr + stResetOffset + 4) / 4;
		for (int i = 0; i < size; i++) {
			US.PUT4(dstAddr, US.GET4(srcAddr));
			dstAddr += 4;
			srcAddr += 4;
		}
	}
}

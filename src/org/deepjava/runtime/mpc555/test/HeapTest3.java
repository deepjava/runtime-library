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

package org.deepjava.runtime.mpc555.test;
import java.io.PrintStream;

import org.deepjava.runtime.mpc555.driver.QADC_DIO;
import org.deepjava.runtime.mpc555.driver.SCI;
import org.deepjava.runtime.ppc32.Heap;
import org.deepjava.runtime.ppc32.Task;

/*changes:
 * 10.04.12	NTB/GRAU	creation
 */

public class HeapTest3 extends Task {
	HeapTest2 next;
	static HeapTest2 head;
	static Task t;
	
	public void action() {
		@SuppressWarnings("unused")
		boolean[] taster = taster();
		if (this.nofActivations % 1000 == 0) {
			System.out.print("freeHeap = ");
			System.out.println(Heap.getFreeHeap());
		}
	}
	
	public static boolean[] taster(){
        boolean[] tasten = new boolean[4];
        tasten[0]=QADC_DIO.get(false, 1);
        tasten[1]=QADC_DIO.get(false, 2);
        tasten[2]=QADC_DIO.get(false, 3);
        tasten[3]=QADC_DIO.get(false, 4);
        return tasten;  
  }

	
	static {
		SCI sci = SCI.getInstance(SCI.pSCI2);
		sci.start(9600, SCI.NO_PARITY, (short)8);
		System.out = new PrintStream(sci.out);
		System.err = new PrintStream(sci.out);
		System.out.println("HeapTest3 started");
		Task t = new HeapTest3();
		t.period = 1;
		Task.install(t);
	}
}

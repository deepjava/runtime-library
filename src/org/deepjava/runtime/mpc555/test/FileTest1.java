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

import org.deepjava.runtime.mpc555.IntbMpc555HB;
import org.deepjava.runtime.mpc555.driver.SCI;
import org.deepjava.runtime.mpc555.driver.ffs.FFS;
import org.deepjava.runtime.mpc555.driver.ffs.File;
import org.deepjava.runtime.mpc555.driver.ffs.Rider;

/*changes:
 * 3.5.11	NTB/GRAU	creation
 */

public class FileTest1 implements IntbMpc555HB {
	
	static void createFile1() {
		File f1 = new File("test1.txt");
		Rider r1 = new Rider();
		r1.set(f1, 0);
		r1.writeInt(0x11223344);
		f1.register();
		System.out.println("test1.txt created");
	}
	
	static void createFile2() {
		File f1 = new File("test2.txt");
		Rider r1 = new Rider();
		r1.set(f1, 0);
		r1.writeInt(0x11223344 * 2);
		f1.register();
		System.out.println("test2.txt created");
	}
	
	static void outDir() {
		FFS.outDir();
	}
	
	static {
		SCI sci = SCI.getInstance(SCI.pSCI2);
		sci.start(9600, SCI.NO_PARITY, (short)8);
		System.out = new PrintStream(sci.out);
		System.out.println("file test");
		FFS.init();
	}

}

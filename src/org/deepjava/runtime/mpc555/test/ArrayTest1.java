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

import org.deepjava.runtime.mpc555.driver.SCI;

/*changes:
 * 22.02.11 NTB/Z�ger	OutT replaced by System.out
 * 11.11.10	NTB/GRAU	creation
 */

public class ArrayTest1 {
	static int[] a4 = {1,2,3,4};
	static int[][] a23 = {{1,2,3},{4,5,6}};
	static int[][][] a223 = {{{1,2,3},{4,5,6}},{{11,12,13},{14,15,16}}};
	static int[][] i1 = new int[2][3];
	static short[][] s1 = new short[2][3];
	
	static {
		SCI sci = SCI.getInstance(SCI.pSCI2);
		sci.start(9600, SCI.NO_PARITY, (short)8);
		System.out = new PrintStream(sci.out);
		System.err = new PrintStream(sci.out);
		
		System.out.println("array test");
		System.out.println(a4[1]);
		System.out.println(a23[1][2]);
		System.out.println(a223[1][0][2]);
		i1[1][1] = 100;
		System.out.println(i1[1][1]);
		short a = s1[0][0];
		s1[0][0] = 0x11;
		s1[0][1] = 0x12;
		s1[1][2] = 0x23;
		System.out.println(s1[0][1]);
		System.out.println(s1[1][2]);
		System.out.println(a);
	}
}

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

public class CANopen {
	private static final byte SDOrObject1Byte = 0x40;
//	private static final byte SDOrObject2Byte = 0x40;
//	private static final byte SDOrObject4Byte = 0x40;
	private static final byte SDOwObject1Byte = 0x2f;
	private static final byte SDOwObject2Byte = 0x2b;
	private static final byte SDOwObject4Byte = 0x23;
	
	private static final short cs_SDO_COB = 0x600;	// COB-ID for client server SDO
	private static final short sc_SDO_COB = 0x580;	// COB-ID for server client SDO
//	private static final short PDO1_COB = 0x180;	// COB-ID for TxPDO 1
	private static final short bootUpMsg = 0x700;	// COB-ID for boot up message
//	private static final short sync_COB = 0x080;	// COB-ID for synch signal
	private static final int txBufNo = 0;
	
	private static byte[] data = new byte[8];

	private static void setSDO(byte cmdSpez, short index, byte subIndex, int objVal, int len) {
		data[0] = (byte)cmdSpez;
		data[1] = (byte)index;	// index low byte
		data[2] = (byte)(index >> 8);	// index high byte
		data[3] = (byte)subIndex;	// subindex
		for (int i = 4; i < len; i++) {
			data[i] = (byte)(objVal >> ((i - 4) * 8));	
		}
	}
			
	public static void sendSDO(byte id, short index, byte subIndex, int val, int len) {
		CAN2.setMsgBufRx(0, sc_SDO_COB | id, false, data);	// set receive msg buffer to search for SDO answer
		switch (len) {
		case 4:
			setSDO(SDOrObject1Byte, index, subIndex, val, 4);
			break;
		case 5:
			setSDO(SDOwObject1Byte, index, subIndex, val, 5);
			break;
		case 6:
			setSDO(SDOwObject2Byte, index, subIndex, val, 6);
			break;
		case 8:
			setSDO(SDOwObject4Byte, index, subIndex, val, 8);
			break;
		default:
			break;
		}
		CAN2.setTxBuf(len, cs_SDO_COB | id, false, data);	
		CAN2.waitForRxComplete();	// wait for answer
	}
	
	// network management: enter pre-operational protocol
	// id: 1..127, 0->all nodes
	public static void sendMsg0NMTenterPreOp(int id) {
		data[0] = (byte)0x80;
		data[1] = (byte)id;
		CAN2.setTxBuf(2, 0, false, data);	
		CAN2.waitForTxComplete(txBufNo);	// wait for end of transfer
	}
			
	// network management: reset communication protocol
	// id: 1..127, 0->all nodes
	public static void sendMsg0NMTresetComm(int id) {
		data[0] = (byte)0x82;
		data[1] = (byte)id;
		CAN2.setTxBuf(2, 0, false, data);	
		CAN2.waitForTxComplete(txBufNo);	// wait for end of transfer
	}
			
	// network management: reset node protocol
	// id: 1..127, 0->all nodes
	public static void sendMsg0NMTresetNode(int id) {
		data[0] = (byte)0x81;
		data[1] = (byte)id;
		CAN2.setTxBuf(2, 0, false, data);	
		CAN2.waitForTxComplete(txBufNo);	// wait for end of transfer
	}
		
	// network management: start remote node protocol
	// id: 1..127, 0->all nodes
	public static void sendMsg0NMTstartRemoteNode(int id) {
		data[0] = (byte)0x01;
		data[1] = (byte)id;
		CAN2.setTxBuf(2, 0, false, data);	
		CAN2.waitForTxComplete(txBufNo);	// wait for end of transfer
	}

	// network management: stop remote node protocol
	// id: 1..127, 0->all nodes
	public static void sendMsg0NMTstopRemoteNode(int id) {
		data[0] = (byte)0x02;
		data[1] = (byte)id;
		CAN2.setTxBuf(2, 0, false, data);	
		CAN2.waitForTxComplete(txBufNo);	// wait for end of transfer
	}

	public static void start(int id) {	
		CAN2.setMsgBufRx(0, bootUpMsg | id, false, data);	// set receive msg buffer to listen for bootup msg
		sendMsg0NMTresetNode(id);	// reset all nodes
		CAN2.waitForRxComplete();	// wait for end of bootup msg
	}
	
	// to be fixed for 5200 
	public static void dispMsgBuf1() {
		System.out.print("Message Buffer 1");
		byte data[] = CAN2.getMsgBuf(1);
		int len = data[12] &  0xf;
		System.out.print("\tlength: "); System.out.print(len);
		for (int i = 0; i < len; i++) {
			System.out.printHex(data[i+4]); System.out.print("\t");
		}
		System.out.println();
	}
						
	// to be fixed for 5200 
	public static void dispMsgBuf2() {
		System.out.print("Message Buffer 2");
		byte data[] = CAN2.getMsgBuf(2);
		int len = data[12] &  0xf;
		System.out.println("\tlength: "); System.out.print(len);
		for (int i = 0; i < len; i++) {
			System.out.printHex(data[i+4]); System.out.print("\t");
		}
		System.out.println();
	}
						
	// to be fixed for 5200 
	public static void printSDOAnswer() {
		data = CAN2.getMsgBuf(CAN2.rx1BufNo);
		System.out.print("index: "); System.out.printHex(data[8]*0x100 + data[7]); System.out.print("\t");
		System.out.print("subindex: "); System.out.printHex(data[9]); System.out.print("\t");
		int val = 0xff & data[10]; 
		val += 0xffff & (data[11]*0x100); 
		val += 0xffffff & (data[12]*0x10000); 
		val += data[13]*0x1000000; 
		System.out.print("val: "); System.out.println(val); 
	}

	public static void sendSync() {
		CAN2.setTxBuf(0, 0x80, false, data);
	}

	// to be fixed for 5200 
	public static void setMsgBufRxPDO(int id) {
//		CAN2.setMsgBufRx(0, PDO1_COB + id, false, data);
	}
			

}

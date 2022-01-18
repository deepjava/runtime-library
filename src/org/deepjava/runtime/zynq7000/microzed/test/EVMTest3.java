package org.deepjava.runtime.zynq7000.microzed.test;

import java.io.PrintStream;
import org.deepjava.flink.core.FlinkDevice;
import org.deepjava.flink.subdevices.FlinkGPIO;
import org.deepjava.runtime.arm32.Task;
import org.deepjava.runtime.zynq7000.driver.UART;
import org.deepjava.runtime.zynq7000.microzed.driver.MIO_DIO;

/**
 * Test program for MicroZed EVM and delta robot.
 * Delta robot must be loaded with https://github.com/eeduro/delta.git
 * Change to branch 'microzed'
 * Target tool point will be above U18/U15 on broad side and above G18/F20 on small side.
 * 
 * @author urs.graf
 */
public class EVMTest3 extends Task {
	static FlinkGPIO gpio;
	TestState state = TestState.STARTBROAD;
	PinState pinState;
	int pin, mio;
	boolean mioTest = true, broadSide = true;
	int error = 0;
	
	private void mioTest() {
		switch (pinState) {
		case PUTHIGH:
			MIO_DIO.init(mio, false);	
			MIO_DIO.init(mio+1, true);
			MIO_DIO.out(mio+1, true);
			if (MIO_DIO.in(9)) { // bbb moved to test location
				pinState = PinState.CHECKHIGH;
			}
			break;
		case CHECKHIGH:
			System.out.print(mio+1);
			System.out.print(" driven to 1 -> ");
			System.out.print(mio);
			if (MIO_DIO.in(mio)) {
				System.out.print(" is 1, ");
			} else {
				System.out.print(" is 0, ");
				error++;
			}
			pinState = PinState.PUTLOW;
			break;
		case PUTLOW:
			MIO_DIO.out(mio+1, false);
			pinState = PinState.CHECKLOW;
			break;
		case CHECKLOW:
			System.out.print(mio+1);
			System.out.print(" driven to 0 -> ");
			System.out.print(mio);
			if (!MIO_DIO.in(mio)) {
				System.out.print(" is 0, ");
			} else {
				System.out.print(" is 1, ");
				error++;
			}
			pinState = PinState.REVPUTHIGH;
			break;
		case REVPUTHIGH:
			MIO_DIO.init(mio, true);	
			MIO_DIO.out(mio, true);
			MIO_DIO.init(mio+1, false);
			pinState = PinState.REVCHECKHIGH;
			break;
		case REVCHECKHIGH:
			System.out.print(mio);
			System.out.print(" driven to 1 -> ");
			System.out.print(mio+1);
			if (MIO_DIO.in(mio+1)) {
				System.out.print(" is 1, ");
			} else {
				System.out.print(" is 0, ");
				error++;
			}
			pinState = PinState.REVPUTLOW;
			break;
		case REVPUTLOW:
			MIO_DIO.out(mio, false);
			pinState = PinState.REVCHECKLOW;
			break;
		case REVCHECKLOW:
			System.out.print(mio);
			System.out.print(" driven to 0 -> ");
			System.out.print(mio+1);
			if (!gpio.getValue(mio+1)) {
				System.out.println(" is 0");
			} else {
				System.out.println(" is 1");
				error++;
			}
			pinState = PinState.WAITING;
			break;
		case WAITING:
			if (!MIO_DIO.in(9)) { // bbb is ready to move on
				MIO_DIO.init(mio, false);
				MIO_DIO.init(mio+1, false);
				pinState = PinState.PUTHIGH;
				state = TestState.PINSELECT;
				mio++;
			}
			break;
		}
	}

	private void pinTest() {
		switch (pinState) {
		case PUTHIGH:
			gpio.setDir(pin, false);	
			gpio.setDir(pin+1, true);
			gpio.setValue(pin+1, true);
			if (MIO_DIO.in(9)) { // bbb moved to test location
				pinState = PinState.CHECKHIGH;
			}
			break;
		case CHECKHIGH:
			System.out.print(pin+1);
			System.out.print(" driven to 1 -> ");
			System.out.print(pin);
			if (gpio.getValue(pin)) {
				System.out.print(" is 1, ");
			} else {
				System.out.print(" is 0, ");
				error++;
			}
			pinState = PinState.PUTLOW;
			break;
		case PUTLOW:
			gpio.setValue(pin+1, false);
			pinState = PinState.CHECKLOW;
			break;
		case CHECKLOW:
			System.out.print(pin+1);
			System.out.print(" driven to 0 -> ");
			System.out.print(pin);
			if (!gpio.getValue(pin)) {
				System.out.print(" is 0, ");
			} else {
				System.out.print(" is 1, ");
				error++;
			}
			pinState = PinState.REVPUTHIGH;
			break;
		case REVPUTHIGH:
			gpio.setDir(pin, true);	
			gpio.setValue(pin, true);
			gpio.setDir(pin+1, false);
			pinState = PinState.REVCHECKHIGH;
			break;
		case REVCHECKHIGH:
			System.out.print(pin);
			System.out.print(" driven to 1 -> ");
			System.out.print(pin+1);
			if (gpio.getValue(pin+1)) {
				System.out.print(" is 1, ");
			} else {
				System.out.print(" is 0, ");
				error++;
			}
			pinState = PinState.REVPUTLOW;
			break;
		case REVPUTLOW:
			gpio.setValue(pin, false);
			pinState = PinState.REVCHECKLOW;
			break;
		case REVCHECKLOW:
			System.out.print(pin);
			System.out.print(" driven to 0 -> ");
			System.out.print(pin+1);
			if (!gpio.getValue(pin+1)) {
				System.out.println(" is 0");
			} else {
				System.out.println(" is 1");
				error++;
			}
			pinState = PinState.WAITING;
			break;
		case WAITING:
			if (!MIO_DIO.in(9)) { // bbb is ready to move on
				gpio.setDir(pin, false);
				gpio.setDir(pin+1, false);
				pinState = PinState.PUTHIGH;
				state = TestState.PINSELECT;
				pin++;
			}
			break;
		}
	}
	
	public void action() {
		switch (state) {
		case STARTBROAD:
			MIO_DIO.init(0, false);
			MIO_DIO.init(9, false);
			if (MIO_DIO.in(0)) {
				state = TestState.PINSELECT;
				pinState = PinState.PUTHIGH;
				mio = 10;	// start with MIO10
				pin = 81;	// start with T10
			}
			break;
		case PINSELECT:
			if (broadSide) {
				if (mioTest) {
					if (mio == 15) {
						mioTest = false;	// continue with gpio
					} else {
						System.out.print("test mio");
						System.out.print(mio);
						System.out.print("/");
						System.out.print(mio+1);
						System.out.print(": ");
						state = TestState.MIOTEST;
						pinState = PinState.PUTHIGH;				
					}
				} else {
					if (pin == 88) {
						pin = 73;	// continue with T15
					} else if (pin == 80) {
						pin = 65;	// continue with U17
					} else if (pin == 72) {
						pin = 57;	// continue with P19
					} else if (pin == 64) {
						pin = 49;	// continue with Y19
					} else if (pin == 56) {
						System.out.print("broad side test ended: nof error = ");
						System.out.println(error);
						System.out.println();
						System.out.println("put evm with small side under target tool point");
						System.out.println("target tool point is 1cm above G18/F20");
						System.out.println("press blue button on delta to start testing small side");
						broadSide = false;
						state = TestState.ENDBROAD;
					} else {
						System.out.print("test gpio");
						System.out.print(pin);
						System.out.print("/");
						System.out.print(pin+1);
						System.out.print(": ");
						state = TestState.PINTEST;
						pinState = PinState.PUTHIGH;
					}
				}
			} else {
				if (pin == 5) {
					pin = 6;	// continue with E19
				} else if (pin == 11) {
					pin = 12;	// continue with M17
				} else if (pin == 17) {
					pin = 18;	// continue with L17
				} else if (pin == 23) {
					pin = 24;	// continue with G17
				} else if (pin == 29) {
					pin = 30;	// continue with G20
				} else if (pin == 35) {
					pin = 36;	// continue with N15
				} else if (pin == 41) {
					pin = 42;	// continue with M15
				} else if (pin == 46) {
					System.out.print("small side test ended: nof error = ");
					System.out.println(error);
					System.out.println();
					state = TestState.ENDSMALL;
				} else {
					System.out.print("test gpio");
					System.out.print(pin);
					System.out.print("/");
					System.out.print(pin+1);
					System.out.print(": ");
					state = TestState.PINTEST;
					pinState = PinState.PUTHIGH;
				}
			}
			break;
		case MIOTEST:
			mioTest();
			break;
		case PINTEST:
			pinTest();
			break;
		case ENDBROAD:
			if (!MIO_DIO.in(0)) {
				state = TestState.STARTSMALL;
			}
			break;
		case STARTSMALL:
			if (MIO_DIO.in(0)) {
				state = TestState.PINSELECT;
				pinState = PinState.PUTHIGH;
				pin = 0;	// start with B19
				error = 0;
			}
			break;
		case ENDSMALL:
			Task.remove(this);
			break;
		}
	}
	
	static {
		UART uart1 = UART.getInstance(UART.pUART1);
		uart1.start(115200, (short)0, (short)8);
		System.out = new PrintStream(uart1.out);
		System.err = System.out;
		System.out.println("EVM Test");
		System.out.println("delta robot must be setup and homed, blue and green led must be on");
		System.out.println("put evm with broad side under target tool point");
		System.out.println("target tool point is 1cm above U18/U15");
		System.out.println("connect cable A with MIO0 and cable B with MIO9");
		System.out.println("insert sd card with EVTest program");
		System.out.println("apply power to microzed board or press reset button");
		System.out.println("press green button on delta to start testing broad side");
//		FlinkDevice.getInstance().lsflink();
		gpio = FlinkDevice.getGPIO();
		
		Task t = new EVMTest3();
		t.period = 20;
		Task.install(t);
	}
}

enum TestState {STARTBROAD, PINSELECT, MIOTEST, PINTEST, ENDBROAD, STARTSMALL, ENDSMALL}
enum PinState {PUTHIGH, CHECKHIGH, PUTLOW, CHECKLOW, REVPUTHIGH, REVCHECKHIGH, REVPUTLOW, REVCHECKLOW, WAITING}

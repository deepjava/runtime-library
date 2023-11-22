package org.deepjava.runtime.zynq7000.microzed.test;

import java.io.PrintStream;
import org.deepjava.flink.core.FlinkDevice;
import org.deepjava.flink.subdevices.FlinkGPIO;
import org.deepjava.runtime.arm32.Task;
import org.deepjava.runtime.zynq7000.driver.UART;
import org.deepjava.runtime.zynq7000.microzed.Kernel;
import org.deepjava.runtime.zynq7000.microzed.driver.MIO_DIO;

/**
 * Test program for MicroZed EVM and delta robot. Use flink1 configuration.
 * Delta robot must be loaded with https://github.com/eeduro/delta.git, branch 'microzed'.
 * Use eeros-scripts to make and deploy to sd card. The run command "sudo ./delta -c HwConfigBBBlue.json"
 * has to be run from systemd service.
 * The microzed board has to be placed under the delta robot so that the target tool point is
 * above U18/U15 on broad side and above G18/F20 on small side.
 * 
 * The green led of the robot must be connected to microzed MIO pin 9. It signals that the robot has 
 * connected the two test points. The red led of the robot must be connected to microzed MIO pin 0.
 * It signals that a test sequence either on the broad or small side has started. 
 * 
 * @author urs.graf
 */
public class EVMTest3 extends Task {
	static FlinkGPIO gpio;
	TestState state = TestState.FIRST;
	PinState pinState;
	int pin, mio;
	boolean mioTest = true, broadSide = true;
	int error = 0;
	long startTime;
	int count;
	
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
				state = TestState.PIN_SELECT;
				mio++;
			}
			break;
		}
	}

	private void flinkGpioTest() {
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
				state = TestState.PIN_SELECT;
				pin++;
			}
			break;
		}
	}
	
	public void action() {
		switch (state) {
		case FIRST:
			System.out.println("\n\nput evm with broad side under target tool point");
			System.out.println("target tool point is approx. 1.3cm above U18/U15");
			System.out.println("press blue button on delta to start testing broad side");
			System.out.println("--------------------------or---------------------------");
			System.out.println("put evm with small side under target tool point");
			System.out.println("target tool point is approx. 1.3cm above G18/F20");
			System.out.println("press green button on delta to start testing small side\n");
			System.out.println("the red button will stop the robot immediately -> emergency mode");
			System.out.println("the blue button will put it back to ready state");
			state = TestState.START;
			MIO_DIO.init(0, false);
			MIO_DIO.init(9, false);
			count = nofActivations;
			break;
		case START:	// make sure last sequence is finished
			if (!MIO_DIO.in(0)) state = TestState.WAIT_START;
			break;
		case WAIT_START: // determine length of side signaling pulse
			if (MIO_DIO.in(0)) {
				startTime = Kernel.timeUs();
				state = TestState.WAIT_SIDE;
			}
			break;
		case WAIT_SIDE:
			if (!MIO_DIO.in(9)) {
				state = TestState.DETECT_SIDE;
			}
			break;
		case DETECT_SIDE:
			long diff = Kernel.timeUs() - startTime;
//			System.out.println(diff);
			if (diff > 200000) {
				System.out.println("\nTest broad side");
				broadSide = true; 
				state = TestState.PIN_SELECT;
				pinState = PinState.PUTHIGH;
				mio = 10;	// start with MIO10
				pin = 81;	// start with T10
				mioTest = true;
				error = 0;
			} else {
				System.out.println("\nTest small side");
				broadSide = false;
				state = TestState.PIN_SELECT;
				pinState = PinState.PUTHIGH;
				pin = 0;	// start with B19
				mioTest = false;
				error = 0;
			}
			break;
		case PIN_SELECT:
			startTime = Task.time();
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
						state = TestState.MIO_TEST;
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
						state = TestState.FIRST;
					} else {
						System.out.print("test gpio");
						System.out.print(pin);
						System.out.print("/");
						System.out.print(pin+1);
						System.out.print(": ");
						state = TestState.GPIO_PIN_TEST;
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
					state = TestState.FIRST;
				} else {
					System.out.print("test gpio");
					System.out.print(pin);
					System.out.print("/");
					System.out.print(pin+1);
					System.out.print(": ");
					state = TestState.GPIO_PIN_TEST;
					pinState = PinState.PUTHIGH;
				}
			}
			break;
		case MIO_TEST:
			if (Task.time() - startTime > 2000) {
				System.out.println("Testing stopped");
				state = TestState.FIRST;
			}
			mioTest();
			break;
		case GPIO_PIN_TEST:
			if (Task.time() - startTime > 2000) {
				System.out.println("Testing stopped");
				state = TestState.FIRST;
			}
			flinkGpioTest();
			break;
		}
	}
	
	static {
		UART uart1 = UART.getInstance(UART.pUART1);
		uart1.start(115200, (short)0, (short)8);
		System.out = new PrintStream(uart1.out);
		System.err = System.out;
		System.out.println("\n\n\nEVM Test");
		System.out.println("delta robot must be setup and homed, blue and green led must be on");
		System.out.println("connect cable A with MIO0 and cable B with MIO9");
//		FlinkDevice.getInstance().lsflink();
		gpio = FlinkDevice.getGPIO();
		
		Task t = new EVMTest3();
		t.period = 20;
		Task.install(t);
	}
}

enum TestState {FIRST, START, WAIT_START, WAIT_SIDE, DETECT_SIDE, PIN_SELECT, MIO_TEST, GPIO_PIN_TEST}
enum PinState {PUTHIGH, CHECKHIGH, PUTLOW, CHECKLOW, REVPUTHIGH, REVCHECKHIGH, REVPUTLOW, REVCHECKLOW, WAITING}

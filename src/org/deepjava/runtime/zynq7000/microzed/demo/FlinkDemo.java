package org.deepjava.runtime.zynq7000.microzed.demo;

import java.io.PrintStream;

import org.deepjava.flink.core.*;
import org.deepjava.flink.subdevices.*;
import org.deepjava.runtime.arm32.Task;
import org.deepjava.runtime.zynq7000.driver.UART;

public class FlinkDemo extends Task implements FlinkDefinitions {
	
	double duty;
	
	static FlinkDevice fDev;
	static FlinkInfo info;
	static FlinkGPIO gpio;
	static FlinkPWM pwm;
	static FlinkCounter fqd;
	static FlinkPPWA ppwa;
	static FlinkADC adc;
	static FlinkTCRT1000 tcrt1000;
	static FlinkStepperMotor stepper;
	
	public void action() {
		for (int i = 0; i <= 3; i++) {
			gpio.setValue(i, !gpio.getValue(i));
		}
		duty = (double) pwm.getHighTime(1) / pwm.getPeriod(1);
		duty += 0.1; if (duty > 0.9) duty = 0;
		pwm.setHighTime(0, (int) (duty * pwm.getPeriod(0)));
		pwm.setHighTime(1, (int) (duty * pwm.getPeriod(1)));
		duty = pwm.getHighTime(1) / pwm.getPeriod(1);
		System.out.print("gpio 6: "); System.out.print(gpio.getValue(6)); System.out.print("\t");
		System.out.print("ppwa: "); System.out.print(ppwa.getHighTime(0)); System.out.print("\t");
		System.out.print("fqd: "); System.out.print(fqd.getCount(1)); System.out.print("\t");
		System.out.print("adc: "); System.out.print(adc.getValue(3)); System.out.print("\t");
		System.out.print("tcrt1000: "); System.out.print(tcrt1000.getValue(0)); System.out.print("\t");
		System.out.print("stepper: "); System.out.println(stepper.getSteps(0));
	}

	static {
		UART uart = UART.getInstance(UART.pUART1);
		uart.start(115200, (short)0, (short)8);
		System.out = new PrintStream(uart.out);
		System.err = System.out;
		System.out.println("\n\rflink demo");
		
		fDev = FlinkDevice.getInstance();
		fDev.lsflink();
	
		info = FlinkDevice.getInfo();
		System.out.print("info description: ");
		System.out.println(info.getDescription());
		gpio = FlinkDevice.getGPIO();
		pwm = FlinkDevice.getPWM();
		ppwa = FlinkDevice.getPPWA();
		fqd = FlinkDevice.getCounter();
		adc = FlinkDevice.getADC128S102();
		tcrt1000 = FlinkDevice.getTCRT1000();
		stepper = FlinkDevice.getStepperMotor();
		
		for(int i = 0; i <= 3; i++) gpio.setDir(i, true);
		for(int i = 6; i <= 7; i++) gpio.setDir(i, false);
		for(int i = 0; i <= 3; i++) gpio.setValue(i, i % 2 == 0);	
		
		final int freq = 1000; // 1kHz
		pwm.setPeriod(0, pwm.getBaseClock() / freq);
		pwm.setHighTime(0, (int) (pwm.getBaseClock() / freq * 0.2)); 
		pwm.setPeriod(1, pwm.getBaseClock() / freq);
		pwm.setHighTime(1, 0); 
		
		Task t = new FlinkDemo();
		t.period = 500;
		Task.install(t);
		
		System.out.println(stepper.getBaseClock());
		System.out.println(stepper.getMotorConfig(0));
		stepper.setMotorConfig(0, 0xe);
		stepper.setSteps(0, 10000);
		stepper.setPrescalerStartSpeed(0, 100000000);		
		stepper.setPrescalerTopSpeed(0, 10000000);
		stepper.setPrescalerAcceleration(0, 10000000);
		stepper.setMotorConfigMask(0, 0x20);
	}

}

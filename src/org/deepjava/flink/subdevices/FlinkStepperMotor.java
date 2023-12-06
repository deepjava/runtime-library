package org.deepjava.flink.subdevices;

import org.deepjava.flink.core.FlinkDefinitions;
import org.deepjava.flink.core.FlinkSubDevice;

public class FlinkStepperMotor implements FlinkDefinitions {

	/** Handle to the subdevice within our flink device */
	public FlinkSubDevice dev;
	private static int BASE_CLOCK_ADDRESS = 0;
	private static int CONFIG_REG_ADDRESS = BASE_CLOCK_ADDRESS + REGISTER_WIDTH;
	private int setConfigRegAddr;
	private int resetConfigRegAddr;
	private int prescalerStartRegAddr;
	private int prescalerTopRegAddr;
	private int accRegAddr;
	private int stepsRegAddr;
	private int stepsDoneRegAddr;
	
	/**
	 * Creates a stepper motor subdevice.
	 * @param dev handle to the subdevice
	 */
	public FlinkStepperMotor(FlinkSubDevice dev){
		this.dev = dev;
		setConfigRegAddr = CONFIG_REG_ADDRESS + dev.nofChannels * REGISTER_WIDTH;
		resetConfigRegAddr = setConfigRegAddr + dev.nofChannels * REGISTER_WIDTH;
		prescalerStartRegAddr = resetConfigRegAddr + dev.nofChannels * REGISTER_WIDTH;
		prescalerTopRegAddr = prescalerStartRegAddr + dev.nofChannels * REGISTER_WIDTH;
		accRegAddr = prescalerTopRegAddr + dev.nofChannels * REGISTER_WIDTH;
		stepsRegAddr = accRegAddr + dev.nofChannels * REGISTER_WIDTH;
		stepsDoneRegAddr = stepsRegAddr + dev.nofChannels * REGISTER_WIDTH;
	}
	
	/**
	 * Resets the steps of all motors.
	 */
	public void resetAllSteps() {
		int addr = dev.baseAddress + MOD_CONF_OFFSET;
		dev.write(addr, 1);
		dev.write(addr, 0);
	}
	
	/**
	 * Returns the base clock of the underlying hardware counter.
	 * @return	the base clock in Hz.
	 */
	public int getBaseClock() {
		return dev.read(BASE_CLOCK_ADDRESS);
	}
	
	/**
	 * Configures the motor (direction, full/half step, two/one phase, mode).
	 * @param ch channel
	 * @param conf configuration word
	 */
	public void setMotorConfig(int ch, int conf) {
		if (ch < dev.nofChannels) 
			dev.write(CONFIG_REG_ADDRESS + ch * REGISTER_WIDTH, conf);
	}

	/**
	 * Reads the motor configuration (direction, full/half step, two/one phase, mode).
	 * @param ch channel
	 * @return configuration word
	 */
	public int getMotorConfig(int ch) {
		if (ch < dev.nofChannels) {
			return (dev.read(CONFIG_REG_ADDRESS + ch * REGISTER_WIDTH));
		} else {
			return 0;
		}
	}

	/**
	 * Configures the motor (direction, full/half step, two/one phase, mode).
	 * This method allows to set individual bits. '1' in this word set the corresponding 
	 * bits, '0' leave them unaltered.
	 * @param ch channel
	 * @param conf configuration word
	 */
	public void setMotorConfigMask(int ch, int conf) {
		if (ch < dev.nofChannels) 
			dev.write(setConfigRegAddr + ch * REGISTER_WIDTH, conf);
	}

	/**
	 * Configures the motor (direction, full/half step, two/one phase, mode).
	 * This method allows to reset individual bits. '1' in this word set the corresponding 
	 * bits to '0', '0' leave them unaltered.
	 * @param ch channel
	 * @param conf configuration word
	 */
	public void resetMotorConfigMask(int ch, int conf) {
		if (ch < dev.nofChannels) 
			dev.write(resetConfigRegAddr + ch * REGISTER_WIDTH, conf);
	}


	/**
	 * Sets the prescaler for the start speed. The start speed is given by
	 * the base clock of the submodule devided by this prescaler.
	 * @param ch channel
	 * @param pre prescaler value
	 */
	public void setPrescalerStartSpeed(int ch, int pre) {
		if (ch < dev.nofChannels) 
			dev.write(prescalerStartRegAddr + ch * REGISTER_WIDTH, pre);
	}

	/**
	 * Sets the prescaler for the top speed. The top speed is given by
	 * the base clock of the submodule devided by this prescaler.
	 * @param ch channel
	 * @param pre prescaler value
	 */
	public void setPrescalerTopSpeed(int ch, int pre) {
		if (ch < dev.nofChannels) 
			dev.write(prescalerTopRegAddr + ch * REGISTER_WIDTH, pre);
	}

	/**
	 * Sets the prescaler for the acceleration. The acceleration is given by
	 * the base clock of the submodule devided by 2 * prescaler.
	 * @param ch channel
	 * @param pre prescaler value
	 */
	public void setPrescalerAcceleration(int ch, int pre) {
		if (ch < dev.nofChannels) 
			dev.write(accRegAddr + ch * REGISTER_WIDTH, pre);
	}

	/**
	 * Sets the destination position.
	 * @param ch channel
	 * @param steps number of steps
	 */
	public void setSteps(int ch, int steps) {
		if (ch < dev.nofChannels) 
			dev.write(stepsRegAddr + ch * REGISTER_WIDTH, steps);
	}

	/**
	 * Reads the current position.
	 * @param ch channel
	 * @return number of steps
	 */
	public int getSteps(int ch) {
		if (ch < dev.nofChannels) 
			return dev.read(stepsDoneRegAddr + ch * REGISTER_WIDTH);
		else 
			return 0;
	}
}

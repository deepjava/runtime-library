package org.deepjava.flink.subdevices;

import org.deepjava.flink.core.FlinkDefinitions;
import org.deepjava.flink.core.FlinkSubDevice;

public class FlinkTCRT1000 implements FlinkDefinitions {

	/** Handle to the subdevice within our flink device */
	public FlinkSubDevice dev;
	private static int RESOLUTION_ADDRESS = 0;
	private static int VALUE_0_ADDRESS = RESOLUTION_ADDRESS + REGISTER_WIDTH;
	private int resolution;
	private int mask;
	
	/**
	 * Creates a ADC subdevice.
	 * @param dev handle to the subdevice
	 */
	public FlinkTCRT1000(FlinkSubDevice dev){
		this.dev = dev;
		resolution = dev.read(RESOLUTION_ADDRESS);
		mask = resolution - 1;
	}
	
	/** 
	 * Reads the resolution field of the subdevice. The field denotes
	 * the number of resolvable steps, e.g. a 10 bit converter delivers 
	 * 1024 steps.
	 * @return number of resolvable steps
	 */
	public int getResolution() {
		return resolution;
	}
	
	/**
	 * Reads the digital value of a channel. Channel number must be 
	 * smaller than the total number of channels.
	 * @param channel channel number
	 * @return digital value
	 */
	public int getValue(int channel) {
		if (channel < dev.nofChannels) {
			return (dev.read(VALUE_0_ADDRESS + channel * REGISTER_WIDTH) & mask);
		} else {
			return 0;
		}
	}

}

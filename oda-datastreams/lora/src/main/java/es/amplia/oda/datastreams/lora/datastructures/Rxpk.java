package es.amplia.oda.datastreams.lora.datastructures;

@SuppressWarnings("unused")
public class Rxpk {
	long tmst;
	String time;
	int chan;
	int rfch;
	double freq;
	int stat;
	String modu;
	String datr;
	String codr;
	double lsnr;
	int rssi;
	int size;
	String data;

	public Rxpk() {
		// This constructor is for the serializer, who need to create a empty object where add the attributes with setters.
	}

	/**
	 * Get the timestamp of the moment when the RX was finished.
	 * @return the timestamp of the RX finished event.
	 */
	public long getTmst() {
		return tmst;
	}

	public void setTmst(long tmst) {
		this.tmst = tmst;
	}

	/**
	 * Get the UTC time of the RX packet in ISO 8601 format.
	 * @return the UTC time and date.
	 */
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * Get the number of the concentrator IF channel used for this RX.
	 * @return an int with the channel number.
	 */
	public int getChan() {
		return chan;
	}

	public void setChan(int chan) {
		this.chan = chan;
	}

	/**
	 * Get the number of the concentrator RF channel used for this RX.
	 * @return an int with the channel number.
	 */
	public int getRfch() {
		return rfch;
	}

	public void setRfch(int rfch) {
		this.rfch = rfch;
	}

	/**
	 * Get the frequency used to transmit the RX in MHz
	 * @return the MHz value of the frequency
	 */
	public double getFreq() {
		return freq;
	}

	public void setFreq(double freq) {
		this.freq = freq;
	}

	/**
	 * CRC status. Values can be:
	 * 		*  1: If CRC is OK.
	 * 		*  0: If there isn't CRC.
	 * 		* -1: If CRC is fail.
	 * @return the number of the CRC.
	 */
	public int getStat() {
		return stat;
	}

	public void setStat(int stat) {
		this.stat = stat;
	}

	/**
	 * Get the moudulation identifier. This can be:
	 * 		* LORA
	 * 		* FSK
	 * @return the value of modulation.
	 */
	public String getModu() {
		return modu;
	}

	public void setModu(String modu) {
		this.modu = modu;
	}

	/**
	 * Get the LoRa datarate used. This have to be specifics identifiers.
	 * @return the identifier of the datarate
	 */
	public String getDatr() {
		return datr;
	}

	public void setDatr(String datr) {
		this.datr = datr;
	}

	/**
	 * Get the ECC coding rate identifier.
	 * @return the identifier of the conding rate
	 */
	public String getCodr() {
		return codr;
	}

	public void setCodr(String codr) {
		this.codr = codr;
	}

	/**
	 * Get the LoRa snr ratio in dB
	 * @return the snr ratio value
	 */
	public double getLsnr() {
		return lsnr;
	}

	public void setLsnr(double lsnr) {
		this.lsnr = lsnr;
	}

	/**
	 * Get the RSSI value in dB
	 * @return the rssi value
	 */
	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	/**
	 * Get the RF packet payload's size in bytes.
	 * @return size of the payload.
	 */
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Get the RF packet payload, encoded in base64 and, most probably, encrypted in AES.
	 * @return the payload.
	 */
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String toShortString() {
		return "Payload received (" + this.time + "): " + this.data;
	}

	@Override
	public String toString() {
		return "Rxpk{" +
				"tmst=" + tmst +
				", time='" + time + '\'' +
				", chan=" + chan +
				", rfch=" + rfch +
				", freq=" + freq +
				", stat=" + stat +
				", modu='" + modu + '\'' +
				", datr='" + datr + '\'' +
				", codr='" + codr + '\'' +
				", lsnr=" + lsnr +
				", rssi=" + rssi +
				", size=" + size +
				", data='" + data + '\'' +
				'}';
	}
}

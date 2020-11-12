package es.amplia.oda.datastreams.lora.datastructures;

@SuppressWarnings("unused")
public class Stat {
	private String time;
	private int rxnb;
	private int rxok;
	private int rxfw;
	private double ackr;
	private int dwnb;
	private int txnb;
	private String pfrm;
	private String mail;
	private String desc;

	public Stat() {
		// This constructor is for the serializer, who need to create a empty object where add the attributes with setters.
	}

	/**
	 * Get the current system time
	 * @return a String with the hour and date formatted
	 */
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * Get the number of the radio packets received by the gateway until now.
	 * This info can be useful to know if we lost any packet.
	 * @return an unsigned int with the quantity of packets received
	 */
	public int getRxnb() {
		return rxnb;
	}

	public void setRxnb(int rxnb) {
		this.rxnb = rxnb;
	}

	/**
	 * Get the number of radio packets received with a valid CRC.
	 * This info can be useful to know if we lost any packet.
	 * @return an unsigned int with the quantity of packets with an OK CRC received
	 */
	public int getRxok() {
		return rxok;
	}

	public void setRxok(int rxok) {
		this.rxok = rxok;
	}

	/**
	 * Get the number of the packets correctly forwarded by the packet forwarder until now.
	 * This quantity of packets have to be the same as the packets received by the ODA.
	 * This info can be useful to know if we lost any packet.
	 * @return an unsigned int with the quantity of packets that the gateway sent.
	 */
	public int getRxfw() {
		return rxfw;
	}

	public void setRxfw(int rxfw) {
		this.rxfw = rxfw;
	}

	/**
	 * Get the percentage of the packets that were acknowledged.
	 * @return a double with the percentage between 0.0 and 100.0.
	 */
	public double getAckr() {
		return ackr;
	}

	public void setAckr(double ackr) {
		this.ackr = ackr;
	}

	/**
	 * Get the datagrams received from the downlink port from the server.
	 * @return an int with the quantity of packets received on gateway from north.
	 */
	public int getDwnb() {
		return dwnb;
	}

	public void setDwnb(int dwnb) {
		this.dwnb = dwnb;
	}

	/**
	 * Get the numbers of packets transmitted to the server by the packet forwarder.
	 * @return a integer with the quantity of packets sent.
	 */
	public int getTxnb() {
		return txnb;
	}

	public void setTxnb(int txnb) {
		this.txnb = txnb;
	}


	public String getPfrm() {
		return pfrm;
	}

	public void setPfrm(String pfrm) {
		this.pfrm = pfrm;
	}

	/**
	 * Get the contact email for the gateway.
	 * @return email direction.
	 */
	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * Get the description for the gateway.
	 * @return description of gateway.
	 */
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * Create a short readable message with the at and the desc.
	 * @return a short message to show.
	 */
	public String toShortString() {
		return "Sent a status message at: " + this.time + " from device " + this.desc;
	}

	@Override
	public String toString() {
		return "Stat{" +
				"time='" + time + '\'' +
				", rxnb=" + rxnb +
				", rxok=" + rxok +
				", rxfw=" + rxfw +
				", ackr=" + ackr +
				", dwnb=" + dwnb +
				", txnb=" + txnb +
				", pfrm='" + pfrm + '\'' +
				", mail='" + mail + '\'' +
				", desc='" + desc + '\'' +
				'}';
	}
}

/*
 *  {
 *     "time":"2020-11-04.15:56:55.GMT",
 *     "rxnb":0,
 *     "rxok":0,
 *     "rxfw":0,
 *     "ackr":50.0,
 *     "dwnb":0,
 *     "txnb":0,
 *     "pfrm":"IMST.+.Rpi",
 *     "mail":"einar@spordata.no",
 *     "desc":"Spordata.GW,Asker"
 *  }
 */
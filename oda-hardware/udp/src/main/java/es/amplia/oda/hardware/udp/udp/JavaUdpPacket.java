package es.amplia.oda.hardware.udp.udp;

import es.amplia.oda.core.commons.udp.UdpPacket;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class JavaUdpPacket implements UdpPacket {
	DatagramPacket packet;

	public JavaUdpPacket(byte[] packetContent) {
		this.packet = new DatagramPacket(packetContent, packetContent.length);
	}

	@Override
	public byte[] getDataAsBytes() {
		return packet.getData();
	}

	@Override
	public String getDataAsString() {
		byte[] res = getDataAsBytes();
		return new String(res, 0, res.length);
	}

	@Override
	public void setData(byte[] packetContent) {
		packet = new DatagramPacket(packetContent, packetContent.length);
	}

	@Override
	public void setData(String packetContent) {
		packet = new DatagramPacket(packetContent.getBytes(), packetContent.length());
	}

	@Override
	public InetAddress getAddress() {
		return packet.getAddress();
	}

	@Override
	public int getPort() {
		return packet.getPort();
	}

	@Override
	public void setAddress(InetAddress address) {
		this.packet.setAddress(address);
	}

	@Override
	public void setPort(int port) {
		this.packet.setPort(port);
	}

	public DatagramPacket getDatagramPacket() {
		return this.packet;
	}
}

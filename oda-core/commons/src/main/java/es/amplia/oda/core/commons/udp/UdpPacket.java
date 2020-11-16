package es.amplia.oda.core.commons.udp;

import java.net.InetAddress;

public interface UdpPacket {

	byte[] getDataAsBytes();

	String getDataAsString();

	void setData(byte[] packetContent);

	void setData(String packetContent);

	InetAddress getAddress();

	int getPort();

	void setAddress(InetAddress address);

	void setPort(int port);
}

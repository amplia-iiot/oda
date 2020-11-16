package es.amplia.oda.datastreams.lora.packets;

import es.amplia.oda.core.commons.udp.UdpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Base64;

// Not used yet
public class UplinkData {

	private static final Logger LOGGER = LoggerFactory.getLogger(UplinkData.class);

	private final byte[] phyPayload;
	private final byte mhdr;
	private final byte[] macPayload;
	private final byte[] mic;
	private final byte[] fhdr;
	private final byte[] fport;
	private final byte[] frmPayload;
	private final byte[] devaddr;
	private final byte[] fcrtl;
	private final byte[] fcnt;

	public UplinkData(String phyPayload) {
		byte[] decodedPhyPayload = Base64.getDecoder().decode(phyPayload);
		if(decodedPhyPayload.length < 12) {
			throw new UdpException("LoRa uplinked data is too short. Something went wrong in the processing");
		}

		this.phyPayload = decodedPhyPayload;
		this.mhdr = this.phyPayload[0];
		this.macPayload = Arrays.copyOfRange(this.phyPayload, 1, this.phyPayload.length - 4);
		this.mic = Arrays.copyOfRange(this.phyPayload, this.phyPayload.length - 4, this.phyPayload.length);
		this.fhdr = Arrays.copyOfRange(macPayload, 0, 7);
		this.fport = Arrays.copyOfRange(macPayload, 7, 8);
		this.frmPayload = Arrays.copyOfRange(macPayload, 8, macPayload.length);
		this.devaddr = Arrays.copyOfRange(fhdr, 0, 4);
		this.fcrtl = Arrays.copyOfRange(fhdr, 4, 5);
		this.fcnt = Arrays.copyOfRange(fhdr, 5, 7);
	}

	public byte[] getPhyPayload() {
		return phyPayload;
	}

	public byte getMhdr() {
		return mhdr;
	}

	public byte[] getMacPayload() {
		return macPayload;
	}

	public byte[] getMic() {
		return mic;
	}

	public byte[] getFhdr() {
		return fhdr;
	}

	public byte[] getFport() {
		return fport;
	}

	public byte[] getFrmPayload() {
		return frmPayload;
	}

	public String decryptFrmPayload(byte[] appSKey) {
		String decryptedData = null;
		try {
			decryptedData = decrypt(phyPayload, appSKey);
		} catch (Exception exception) {
			LOGGER.warn("Couldn't decrypt payload of {} message", this.phyPayload);
		}
		return decryptedData;
	}

	private String decrypt(byte[] phyPayload, byte[] appSKey) throws NoSuchPaddingException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "SunJCE");
		SecretKeySpec secretKeySpec = new SecretKeySpec(appSKey, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		return new String(cipher.doFinal(phyPayload));
	}

	public byte[] getDevaddr() {
		return devaddr;
	}

	public byte[] getFcrtl() {
		return fcrtl;
	}

	public byte[] getFcnt() {
		return fcnt;
	}

	public byte[] getDataOfFieldWithBitScale(int initBit, int endBit) {
		return null;
	}
}

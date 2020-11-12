package es.amplia.oda.datastreams.lora.datastructures;

import java.util.List;

@SuppressWarnings("unused")
public class LoraDataPacket {
	private List<Rxpk> rxpk;

	public LoraDataPacket() {
		// This constructor is for the serializer, who need to create a empty object where add the attributes with setters.
	}

	public List<Rxpk> getRxpk() {
		return rxpk;
	}

	public void setRxpk(List<Rxpk> rxpk) {
		this.rxpk = rxpk;
	}

	public String toShortString() {
		StringBuilder str = new StringBuilder();
		for (Rxpk value : rxpk) {
			str.append(value.toShortString());
		}
		return str.toString();
	}

	@Override
	public String toString() {
		return "LoraPackets{" +
				rxpk +
				'}';
	}
}

/*
 * 	JSON of a data packet:
 *
 *  {
 *    "rxpk":[
 *       {
 *          "tmst":3693527500,
 *          "time":"2020-11-05T08:05:09.806459Z",
 *          "chan":1,
 *          "rfch":1,
 *          "freq":433.375000,
 *          "stat":1,
 *          "modu":"LORA",
 *          "datr":"SF12BW125",
 *          "codr":"4/5",
 *          "lsnr":7.5,
 *          "rssi":-97,
 *          "size":37,
 *          "data":"QMA2ASYAAAAySVpwwdC3FN+8D5hYi/HLusnuN53H5TZFmuCOJQ=="
 *       }
 *    ]
 *  }
 */
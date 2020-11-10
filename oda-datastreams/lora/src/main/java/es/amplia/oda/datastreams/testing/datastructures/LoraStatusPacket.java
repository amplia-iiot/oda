package es.amplia.oda.datastreams.testing.datastructures;

@SuppressWarnings("unused")
public class LoraStatusPacket {
	private Stat stat;

	public LoraStatusPacket() {
		// This constructor is for the serializer, who need to create a empty object where add the attributes with setters.
	}

	public Stat getStat() {
		return stat;
	}

	public void setStat(Stat stat) {
		this.stat = stat;
	}

	public String toShortString() {
		return this.stat.toShortString();
	}

	@Override
	public String toString() {
		return "LoraStayAlive{" +
				"stat=" + stat +
				'}';
	}
}

/*
 * 	JSON of a status packet:
 *
 *  {
 *    "stat":{
 *       "time":"2020-11-04.15:56:55.GMT",
 *       "rxnb":0,
 *       "rxok":0,
 *       "rxfw":0,
 *       "ackr":50.0,
 *       "dwnb":0,
 *       "txnb":0,
 *       "pfrm":"IMST.+.Rpi",
 *       "mail":"einar@spordata.no",
 *       "desc":"Spordata.GW,Asker"
 *    }
 *  }
 */
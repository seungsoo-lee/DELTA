package org.deltaproject.channelagent.fuzzing;

import java.util.LinkedHashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

public class SeedPacket {
	private Map<OFMessage, Integer> map;
	private OFType type;

	public SeedPacket(OFType t) {
		this.type = t;
		map = new LinkedHashMap<OFMessage, Integer>();
	}

	public OFType getOFType() {
		return this.type;
	}

	public void putData(OFMessage m, int len) {
		// ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(len);
		// of.writeTo(buf);
		//
		// int length = buf.readableBytes();
		// byte[] msg = new byte[length];
		// buf.getBytes(buf.readerIndex(), msg);

		map.put(m, len);
	}

	public Map<OFMessage, Integer> getMsgMap() {
		return map;
	}
}

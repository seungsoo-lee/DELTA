package org.deltaproject.channelagent.pkthandler;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.Packet;

import java.io.IOException;

public class Sender {
	private NetworkInterface device = null;
	private JpcapSender sender;

	private void __build_sender(NetworkInterface phys_device) throws IOException {
		JpcapCaptor captor = JpcapCaptor.openDevice(phys_device, 2000, false, 3000);
		this.sender = captor.getJpcapSenderInstance();
	}

	public Sender(NetworkInterface device) throws NullPointerException, IOException {
		this.device = device;
		if (this.device == null)
			throw new NullPointerException("No device has been given! sender");

		this.__build_sender(this.device);
	}

	public NetworkInterface getDevice() {
		return device;
	}

	public void setDevice(NetworkInterface device) throws IOException, NullPointerException {
		this.device = device;

		if (this.device == null)
			throw new NullPointerException("No device has been given! sender");
		this.__build_sender(device);
	}

	public JpcapSender getSender() {
		return sender;
	}

	public void setSender(JpcapSender sender) {
		this.sender = sender;
	}

	public synchronized void send(Packet pack) {
		this.sender.sendPacket(pack);
	}
}

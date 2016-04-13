package nss.delta.channelagent.pkthandler;

import java.io.IOException;

import jpcap.packet.Packet;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;

public class Listener extends Thread {
	class default_handler implements PacketReceiver {
		public void receivePacket(Packet p) {
			System.out.println("PACKET\t-\t" + p.toString());
		}
	}

	private JpcapCaptor captor;
	private NetworkInterface device;
	private PacketReceiver handler;

	private void __build_captor(NetworkInterface phys_device) throws IOException {
		this.captor = JpcapCaptor.openDevice(phys_device, 65535, false, 20);
	}

	public Listener(NetworkInterface device, PacketReceiver pr) throws IOException {
		this.device = device;
		if (this.device == null)
			throw new NullPointerException("No device has been given! sender");

		this.__build_captor(this.device);

		this.handler = pr;
	}

	public void listen() {
		if (this.handler == null) {
			this.handler = new default_handler();
		}

		this.captor.loopPacket(-1, this.handler);
	}

	public void setFilter(String filter_name, boolean optimize) {
		try {
			this.captor.setFilter(filter_name, optimize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.listen();
	}

	public JpcapCaptor getListener() {
		return this.captor;
	}

	public void setPacketReadTimeout(int timeout) {
		this.captor.setPacketReadTimeout(timeout);
	}

	public void finish() {
		this.captor.breakLoop();
	}

}

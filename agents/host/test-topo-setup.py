#!/usr/bin/python
import sys
import os
from mininet.cli import CLI 
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.node import OVSSwitch, Controller, RemoteController

def DeltaNetwork():
#Make topology
	net = Mininet(topo=None, controller=None, build=False)

#Add switch
	s0 = net.addSwitch('s0', dpid='00:00:00:00:00:01')
	s1 = net.addSwitch('s1', dpid='00:00:00:00:00:02')
	s2 = net.addSwitch('s2', dpid='00:00:00:00:00:03')
	s3 = net.addSwitch('s3') # for connection with DELTA

#Add hosts
	h1 = net.addHost('h1', ip='10.0.0.1', mac='00:00:00:00:00:11')
	h2 = net.addHost('h2', ip='10.0.0.2', mac='00:00:00:00:00:22')

#Add links
	net.addLink(s0, h1)
	net.addLink(s2, h2)
	net.addLink(s0, s1)
	net.addLink(s1, s2)

	net.addLink(s3, h1, intfName2='eth1')
	
#	net.build()
	net.start()

#Add hardware interface to switch1 
	s3.attach('eth1')

#Set ip
	h1.cmd("ifconfig eth1 192.168.200.10 netmask 255.255.255.0")
	
#connect a controller
	os.system("sudo ovs-vsctl set-controller s0 tcp:"+sys.argv[1]+":"+sys.argv[2])
	os.system("sudo ovs-vsctl set-controller s1 tcp:"+sys.argv[1]+":"+sys.argv[2])
	os.system("sudo ovs-vsctl set-controller s2 tcp:"+sys.argv[1]+":"+sys.argv[2])

	CLI(net)
	net.stop()

if __name__=='__main__':
	if len(sys.argv) != 3:
		print ("Usage: sudo python topo-setup.py <Controller IP> <Controller Port>")
		sys.exit(0)
	
	DeltaNetwork()

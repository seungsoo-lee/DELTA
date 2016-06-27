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
	s0 = net.addSwitch('s0')
	s1 = net.addSwitch('s1')

#Add hosts
	h1 = net.addHost('h1', ip='10.0.0.1')
	h2 = net.addHost('h2', ip='10.0.0.2')

#Add links
	net.addLink(s0, h1)
	net.addLink(s0, h2)

	net.addLink(s1, h1, intfName2='eth1')
	
#	net.build()
	net.start()

#Add hardware interface to switch1 
	s1.attach('eth1')

#Set ip
	h1.cmd("ifconfig eth1 192.168.200.10 netmask 255.255.255.0")
	
#connect a controller
	os.system("sudo ovs-vsctl set-controller s0 tcp:"+sys.argv[1]+":"+sys.argv[2])

	CLI(net)
	net.stop()

if __name__=='__main__':
	if len(sys.argv) != 3:
		print ("Usage: sudo python topo-setup.py <Controller IP> <Controller Port>")
		sys.exit(0)
	
	DeltaNetwork()

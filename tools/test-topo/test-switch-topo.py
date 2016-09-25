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
        s0 = net.addSwitch('s0', dpid='00:00:00:00:00:01', protocols=sys.argv[3])

#Add hosts
        h1 = net.addHost('h1', ip='10.0.0.1', mac='00:00:00:00:00:11')
        h2 = net.addHost('h2', ip='10.0.0.2', mac='00:00:00:00:00:22')

#Add links
        net.addLink(s0, h1)
        net.addLink(s0, h2)

#       net.build()
        net.start()

#Add hardware interface to switch1

#connect a controller
        os.system("sudo ovs-vsctl set-controller s0 tcp:"+sys.argv[1]+":"+sys.argv[2])

        CLI(net)
        net.stop()

if __name__=='__main__':
        if len(sys.argv) != 4:
                print ("Usage: sudo python topo-setup.py <Controller IP> <Controller Port> <OF version>")
                sys.exit(0)

        DeltaNetwork()

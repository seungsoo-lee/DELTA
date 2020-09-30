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
        proto = sys.argv[5]
        s0 = net.addSwitch('s0', dpid='00:00:00:00:00:01', protocols=proto)
        s1 = net.addSwitch('s1') # for connection with DELTA

#Add hosts
        h1 = net.addHost('h1', ip='10.0.0.1/24', mac='00:00:00:00:00:11')
        h2 = net.addHost('h2', ip='10.0.0.2/24', mac='00:00:00:00:00:22')

#Add links
        net.addLink(s0, h1)
        net.addLink(s0, h2)

        net.addLink(s1, h1, intfName2='eth0')

#       net.build()
        net.start()

#Add hardware interface to switch1
        s1.attach('eth0')

#Set ip
        os.system("sudo ovs-ofctl add-flow s1 in_port=1,actions=output:2")
        os.system("sudo ovs-ofctl add-flow s1 in_port=2,actions=output:1")

        h1.cmd("ifconfig eth0 10.0.2.10/24 netmask 255.255.255.0")
        #h1.cmd("dhclient eth0")

#connect a controller
        os.system("sudo ovs-vsctl set-controller s0 tcp:"+sys.argv[1]+":"+sys.argv[2])

        h1.cmd("java -jar $HOME/delta-agent-host-1.0-SNAPSHOT.jar "+sys.argv[3]+" "+sys.argv[4])
        CLI(net)
        net.stop()

if __name__=='__main__':
        if len(sys.argv) != 6:
                print ("Usage: sudo python topo-setup.py <Controller IP> <Controller Port> <AM_IP> <AM_PORT> <OF_VER>")
                sys.exit(0)

        DeltaNetwork()

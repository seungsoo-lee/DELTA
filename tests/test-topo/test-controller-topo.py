#!/usr/bin/python
import sys
import os
from mininet.cli import CLI 
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.node import OVSSwitch, Controller, RemoteController
from mininet.log import setLogLevel, info

def DeltaNetwork():
    net = Mininet(controller=None)
    net.addController( 'c0', controller=RemoteController, ip=sys.argv[1], port=int(sys.argv[2]))
    h1 = net.addHost('h1', ip='10.0.0.1/24', mac='00:00:00:00:00:11')
    h2 = net.addHost('h2', ip='10.0.0.2/24', mac='00:00:00:00:00:22')
    s1 = net.addSwitch( 's1' )
    net.addLink( h1, s1 )
    net.addLink( h2, s1 )

    net.start()
    ssladdr = sys.argv[1] + ':' + sys.argv[2]
    s1.cmd('ovs-vsctl set-controller s1 ssl:'+ssladdr)

    CLI( net )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'debug' )
    if len(sys.argv) != 3:
        print ("Usage: sudo python test-controller-topo.py <Controller IP> <Controller Port>")
        sys.exit(0)
    DeltaNetwork()

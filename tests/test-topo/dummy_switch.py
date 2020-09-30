import sys
import socket, threading
import loxi
import loxi.connection
import loxi.of13 as ofp
from enum import Enum
import logging, os, struct, time
logging.basicConfig(stream=sys.stdout, level=logging.DEBUG)
import pdb

class SwitchState(Enum):
    CLOSED = 1
    HELLO_WAIT = 2
    FEATURE_WAIT = 3
    ESTABLISHED = 4

class PacketDispatcher(threading.Thread):

    cxn = loxi.connection
    ds = None

    def __init__(self, cxn, ds):
        threading.Thread.__init__(self)
        self.kill_received = False
        self.cxn = cxn
        self.ds = ds

    def run(self):
        logging.debug('packetDispatcher: start!')

        while self.kill_received is not True:
            msg = self.cxn.recv_any()
            if msg != None:
#                print "Received message (%s) xid (%s)" % (type(msg).__name__, msg.xid)
                if msg.type is ofp.OFPT_FEATURES_REQUEST:
                    ds.handshakeHandler()
                elif ds.state is SwitchState.ESTABLISHED:
                    if msg.type is ofp.OFPT_STATS_REQUEST:
                        ds.multipartHandler(msg)
                    elif msg.type is ofp.OFPT_GET_CONFIG_REQUEST:
                        ds.configHandler(msg)
                    elif msg.type is ofp.OFPT_ROLE_REQUEST:
                        ds.roleHandler(msg)
                    elif msg.type is ofp.OFPT_FLOW_MOD:
                        ds.flowHandler(msg)
                    elif msg.type is ofp.OFPT_BARRIER_REQUEST:
                        ds.barrierHandler(msg)
                    elif msg.type is ofp.OFPT_ECHO_REQUEST:
                        ds.echoHandler(msg)

class DummySwitch:
    threads = []
    cxn = loxi.connection
    state = SwitchState
    flow_tables = []

    def connectToCtrl(self, ip, port):
        self.cxn = loxi.connection.connect(ip, int(port), ofp=loxi.of13)
        self.state = SwitchState.FEATURE_WAIT

        disp_thd = PacketDispatcher(self.cxn, self)
        disp_thd.start()
        self.threads.append(disp_thd)

    def handshakeHandler(self):
        f_reply = ofp.message.features_reply(datapath_id=struct.unpack("<L", os.urandom(4))[0], capabilities=0x1)
        self.cxn.send(f_reply)
        self.state = SwitchState.ESTABLISHED

    def multipartHandler(self,  msg):
        reply = None

        if msg.stats_type is ofp.OFPST_PORT_DESC:
            reply = ofp.message.port_desc_stats_reply(xid=msg.xid)
        elif msg.stats_type is ofp.OFPST_METER_FEATURES:
            reply = ofp.message.meter_features_stats_reply(xid=msg.xid)
        elif msg.stats_type is ofp.OFPST_DESC:
            reply = ofp.message.desc_stats_reply(xid=msg.xid, hw_desc="Xeon", sw_desc="DELTA")
        elif msg.stats_type is ofp.OFPST_FLOW:
            reply = ofp.message.flow_stats_reply(msg.xid, msg.flags, self.flow_tables)
        elif msg.stats_type is ofp.OFPST_TABLE_FEATURES:
            reply = ofp.message.table_features_stats_reply(xid=msg.xid, flags=msg.flags)

        if reply is not None:
            self.cxn.send(reply)


    def configHandler(self, msg):
        g_reply = ofp.message.get_config_reply()
        self.cxn.send(g_reply)

    def roleHandler(self, msg):
        r_reply = ofp.message.role_reply(msg.xid, msg.role, msg.generation_id)
        self.cxn.send(r_reply)

    def flowHandler(self, msg):
        f_entry = ofp.common.flow_stats_entry(0, 0, 0, msg.priority, msg.idle_timeout, msg.hard_timeout, msg.flags, msg.cookie, 0, 0, msg.match, msg.instructions)
        self.flow_tables.append(f_entry)

    def barrierHandler(self, msg):
        b_reply = ofp.message.barrier_reply(msg.xid)
        self.cxn.send(b_reply)

    def echoHandler(self, msg):
        e_reply = ofp.message.echo_reply(msg.xid)
        self.cxn.send(e_reply)

    def joinThreads(self):
        try:
            while True:
                time.sleep(.1)
        except KeyboardInterrupt:
            print "Sending Kill to threads..."
            for t in self.threads:
                t.kill_received = True


if __name__=="__main__":
    if len(sys.argv) < 3:
        sys.exit('Usage: python dummy_switch.py [ip_address] [port]') 

    ds = DummySwitch()
    ds.connectToCtrl(sys.argv[1], sys.argv[2])
    ds.joinThreads()

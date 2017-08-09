from threading import Thread
from ryu.base import app_manager
import am_interface

class AppAgent(app_manager.RyuApp):
    def __init__(self, *args, **kwargs):
        from am_interface import AMInterface
        super(AppAgent, self).__init__(*args, **kwargs)
    
        # Run AMInterface Thread    
        ami = AMInterface(self)
        server_address = ami.setServerAddr()
        t = Thread(target=ami.connectServer, args=(server_address,))
        t.start()
        print "[App-Agent] Starting AppAgent on Ryu"

    # 3.1.020
    def testControlMessageDrop(self):
        print "testControlMessageDrop"

    # 3.1.030
    def testInfiniteLoops(self):
        print "[ATTACK] Start Infinite Loop"
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

    # 3.1.090
    def testEventListenerUnsubscription(self):
        print "testEventListenerUnsubscription"

if __name__ == "__main__":
    a = AppAgent()

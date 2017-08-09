from threading import Thread
import AMInterface as ami
from ryu.base import app_manager

class AppAgent(app_manager.RyuApp):
    def __init__(self, *args, **kwargs):
        super(AppAgent, self).__init__(*args, **kwargs)
        server_address = ami.setServerAddr()
        t = Thread(target=ami.connectServer, args=(server_address,))
        t.start()
        print "[App-Agent] Starting AppAgent on Ryu"

    # 3.1.020
    def testControlMessageDrop():
        print "testControlMessageDrop"

    # 3.1.030
    def testInfiniteLoops():
        print "testInfiniteLoops"

    # 3.1.040
    def testInternalStorageAbuse():
        print "testInternalStorageAbuse"

    # 3.1.070
    def testFlowRuleModification():
        print "testFlowRuleModification"

    # 3.1.080
    def testFlowTableClearance():
        print "testFlowTableClearance"

    # 3.1.090
    def testEventListenerUnsubscription():
        print "testEventListenerUnsubscription"
    if __name__ == "__main__":
        server_address = ami.setServerAddr()
        print "[App-Agent] {0}".format(server_address)
        t = Thread(target=ami.connectServer, args=(server_address,))
        t.start()

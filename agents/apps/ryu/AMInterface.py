import socket
import AppAgent

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect the socket to the port where the server is listening
server_address = ('10.0.2.2', 3366)
print 'connecting to %s port %s' % server_address
sock.connect(server_address)

try:
    # Send data
    message = 'AppAgent'
    m = '\x00' + '\x08' + message
    print "Client: " + message
    sock.send(m)
    data = sock.recv(1024)

    data = sock.recv(1024)
    if "3.1.020" in data:
        print "3.1.020"
        AppAgent.testControlMessageDrop()
    elif "3.1.030" in data:
        print "3.1.030"
        AppAgent.testInfiniteLoops()
    elif "3.1.040" in data:
        print "3.1.040"
        AppAgent.testInternalStorageAbuse()
    elif "3.1.070" in data:
        print "3.1.070"
        AppAgent.testFlowRuleModification()
    elif "3.1.080" in data:
        print "3.1.080"
        AppAgent.testFlowTableClearance()
    elif "3.1.090" in data:
        print "3.1.090"
        AppAgent.testEventListenerUnsubscription()
    elif "OK" in data:
        print "OK"
    else:
        print "nothing"

        #AppAgent.testFunction(data)
        #print "Server: " + data
finally:
    sock.close()

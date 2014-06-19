import time
import threading
import socket
import pyperclip
import struct
import sys

MCAST_GRP = '224.1.1.1'
if len(sys.argv) > 1:
	MCAST_PORT = int(sys.argv[1])
else:
	MCAST_PORT = 9292

print "USING PORT : " + str(MCAST_PORT)  

class Msg():
	def __init__(self,msg):
		self._value = msg

	def change(self,msg):
		self._value = msg

	def value(self):
		return self._value


class ClippyWatcher(threading.Thread):
	def __init__(self,msg):
		super(ClippyWatcher,self).__init__()
		self._pause = 5
		self._stopping = False
		self._msg = msg
	
	def run(self,):       
		recent_value = ""
		while not self._stopping:
			lock = threading.RLock()    
			lock.acquire()
			recent_value = self._msg.value()
			tmp_value = pyperclip.paste()
			lock.release()	
			if tmp_value != recent_value:
				recent_value = tmp_value	
				self.broadcast(recent_value)
			time.sleep(self._pause)
			

	def stop(self):
		print "stopping watcher"
		self._stopping = True

	def broadcast(self,msg):
		sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
		sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)
		sock.sendto(msg, (MCAST_GRP, MCAST_PORT))				


class ClippyReceiver(threading.Thread):
	def __init__(self,msg):
		super(ClippyReceiver,self).__init__()
        	self._pause = 5
		self._stopping = False
		self._msg = msg
	
	def run(self): 
		sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
		sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
		sock.bind((MCAST_GRP, MCAST_PORT))
		mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)
		sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

		while not self._stopping:
  			msg = sock.recv(10240)		
			print msg
			lock = threading.RLock()
			lock.acquire()
			self._msg.change(msg)
			lock.release()			
			pyperclip.copy(msg)
			time.sleep(self._pause)

	def stop(self):
		print "stopping receiver"
        	self._stopping = True






def main():
	msg = Msg("")
	watcher = ClippyWatcher(msg)
	receiver = ClippyReceiver(msg)	
	watcher.daemon = True
	receiver.daemon = True
	watcher.start()
	receiver.start()

	while True:
		try:
			time.sleep(10)
		except KeyboardInterrupt:
			watcher.stop()
			receiver.stop()
			break


if __name__ == "__main__":
    main()

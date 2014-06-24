import wx
import time
import threading
import socket
import pyperclip
import struct
import os
import json
from wx.lib.intctrl import IntCtrl


MCAST_GRP = '224.1.1.1'
MCAST_PORT = 9292
CONFIG_FILE = 'clippy.config'


class Msg():
    def __init__(self,msg):
        self._value = msg

    def change(self,msg):
        self._value = msg

    def value(self):
        return self._value


class ClippyWatcher(threading.Thread):
    def __init__(self,msg,port):
        super(ClippyWatcher,self).__init__()
        self._pause = 5
        self._stopping = False
        self._msg = msg
        self._port = port

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
        sock.sendto(msg, (MCAST_GRP, self._port))


class ClippyReceiver(threading.Thread):
    def __init__(self,msg,port):
        super(ClippyReceiver,self).__init__()
        self._pause = 5
        self._stopping = False
        self._msg = msg
        self._port = port

    def run(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.bind((MCAST_GRP, self._port))
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



class ClippyFrame(wx.Frame):

    def __init__(self,parent,title):
        self.port = 0
        self.init_clippy()

        ### Frame

        wx.Frame.__init__(self,parent,title = title, size = (300,200),style= wx.SYSTEM_MENU | wx.CAPTION | wx.CLOSE_BOX)

        ### Menu

        fileMenu= wx.Menu()
        menuAbout = fileMenu.Append(wx.ID_ABOUT, "&About", "developed by Rahul Madhavan (rahulmadhavan21@gmail.com)")
        menuExit  = fileMenu.Append(wx.ID_EXIT, "&Exit", "Adios !!!")

        menuBar = wx.MenuBar()
        menuBar.Append(fileMenu,"File")
        self.SetMenuBar(menuBar)

        self.Bind(wx.EVT_MENU,self.OnAbout,menuAbout)
        self.Bind(wx.EVT_MENU,self.OnExit, menuExit)



        ### UI Elements

        self.current_port_no_label = wx.StaticText(self,wx.ID_ANY,"Current Port No : ")
        self.port_text_box = IntCtrl(self,wx.ID_ANY, value = self.port,min=1000, max = 100000)

        self.port_no_label = wx.StaticText(self,wx.ID_ANY," Port No : ")
        self.current_port = wx.StaticText(self,wx.ID_ANY,str(self.port))

        self.restart_button = wx.Button(self,-1,"Restart Clippy")
        self.restart_button.Disable()

        ### Binders
        self.Bind(wx.EVT_BUTTON,self.OnRestart,self.restart_button)
        self.Bind(wx.EVT_TEXT,self.OnTextChange,self.port_text_box)


        ### Sizers

        flex_sizer = wx.FlexGridSizer(2,2,20,35)

        flex_sizer.AddMany([(self.current_port_no_label),
                            (self.current_port, 1, wx.EXPAND),
                            (self.port_no_label),
                            (self.port_text_box, 1, wx.EXPAND)])


        v_sizer = wx.BoxSizer(wx.VERTICAL)

        v_sizer.Add(flex_sizer,1,wx.EXPAND)
        v_sizer.Add(self.restart_button,1,wx.EXPAND | wx.ALIGN_CENTER)

        f_sizer = wx.BoxSizer(wx.VERTICAL)
        f_sizer.Add(v_sizer,1,wx.EXPAND | wx.ALL, 20)



        self.SetSizer(f_sizer)
        self.Show()

    def OnAbout(self,event):
        dlg = wx.MessageDialog(self,"developed by Rahul Madhavan (rahulmadhavan21@gmail.com)","Clippy", style = wx.ICON_INFORMATION)
        dlg.ShowModal()
        dlg.Destroy()

    def OnExit(self,event):
        self.Close(True)

    def OnRestart(self,e):
        self.watcher.stop()
        self.receiver.stop()
        port = self.port_text_box.GetValue()
        self.persist_json_config({'port':port})
        self.init_clippy()
        self.current_port.SetLabelText(str(self.port))
        self.restart_button.Disable()

    def OnTextChange(self,e):
        temp_port = self.port_text_box.GetValue()
        if self.port != temp_port and self.port_text_box.IsInBounds():
            self.restart_button.Enable()
        else:
            self.restart_button.Disable()

    def init_clippy(self):
        self.msg = Msg("")
        config = self.fetch_clippy_configuration()
        port = config["port"]
        self.port = port
        print "USING PORT : "+str(port)
        self.watcher = ClippyWatcher(self.msg,port)
        self.receiver = ClippyReceiver(self.msg,port)
        self.watcher.daemon = True
        self.receiver.daemon = True
        self.watcher.start()
        self.receiver.start()

    def fetch_clippy_configuration(self):
        config = {}
        data_dir = wx.StandardPaths.Get().GetUserDataDir()
        filename = CONFIG_FILE
        file_abs_path = os.path.join(data_dir,filename)
        if os.path.isfile(file_abs_path):
            fp = open(file_abs_path,'r')
            str = fp.read()
            config = json.loads(str)
        else:
            print "config file not found"
            config = self.create_json_config()
        return config

    def persist_json_config(self,config):
        data_dir = wx.StandardPaths.Get().GetUserDataDir()
        filename = CONFIG_FILE
        file_abs_path = os.path.join(data_dir,filename)
        with open(file_abs_path,'w') as outfile:
            json.dump(config, outfile)
        return config

    def create_json_config(self):
        config = {"port":MCAST_PORT}
        data_dir = wx.StandardPaths.Get().GetUserDataDir()
        filename = CONFIG_FILE
        file_abs_path = os.path.join(data_dir,filename)
        if not os.path.exists(data_dir):
            os.makedirs(data_dir)
        with open(file_abs_path,'w') as outfile:
            json.dump(config, outfile)
        return config


app = wx.App(False)
frame = ClippyFrame(None,'Clippy')
print wx.StandardPaths.Get().GetUserDataDir()
app.MainLoop()

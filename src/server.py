"""
  - Use the sudo raspi-config to turn off the serial terminal but enable the serial hardware
  - Install pyserial (probably already installed)
  - Install tornado GLOBALLY like this: python -m pip install tornado
  - This file is in a directory named "web". Copy the entire directory (and subs) to the pi home
  - Add this line to /etc/rc.local (before the exit 0):
  -   /home/pi/ONBOOT.sh 2> /home/pi/ONBOOT.errors > /home/pi/ONBOOT.stdout &
  - Add the following ONBOOT.sh script to /home/pi and make it executable:
  
#!/bin/bash
cd /home/pi/web
/usr/bin/python server.py
  
"""
import tornado.ioloop
import tornado.web
import os
import time
import i_robot_create

roomba = i_robot_create.Create('COM4')
#roomba = i_robot_create('/dev/serial0')

# Wake the serial interface
roomba.set_mode_passive() 

# Switch to control mode
roomba.set_mode_full()
#roomba.set_mode_safe()

# Confirmation that everything is working
roomba.set_song(0,[[0x4F,0x10],[0x4C,0x20],[0x48,0x10]])
roomba.play_song(0)
time.sleep(3)
        
class CGIHandler(tornado.web.RequestHandler):
    def post(self):        
        cmd = self.get_argument('command')
        print(cmd)
        eval("roomba."+cmd)

root = os.path.join(os.path.dirname(__file__), "webroot")

handlers = [
    (r"/robot", CGIHandler),        
    (r"/(.*)", tornado.web.StaticFileHandler, {"path": root, "default_filename": "index.html"}),
    ]

app = tornado.web.Application(handlers)
app.listen(8888)
tornado.ioloop.IOLoop.current().start()
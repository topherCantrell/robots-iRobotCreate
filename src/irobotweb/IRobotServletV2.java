package irobotweb;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import irobotcreatepi.IRobotCreateV2;

public class IRobotServletV2  extends HttpServlet
{
    
	private static final long serialVersionUID = 1L;
	
	private static IRobotCreateV2 robot;
	static {
		try {
			 
			// Are we running on the pi or on the PC?
			File f = new File("/etc");
			String devname;
			if(f.exists()) {
				devname = "/dev/ttyUSB0"; // Pi
			} else {
				devname = "COM12"; // PC
			}			
			
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(devname);
			SerialPort serialPort = (SerialPort) portIdentifier.open("CreatePi",2000);					
			serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);		
			serialPort.setInputBufferSize(256);		
			
			System.out.println("USING PORT '"+devname+"'");
			
			robot = new IRobotCreateV2(serialPort.getInputStream(),serialPort.getOutputStream());
			
			robot.setMode(IRobotCreateV2.MODE.PASSIVE); // Required at startup
			Thread.sleep(1000); // Wait for mode change	
			
			robot.setMode(IRobotCreateV2.MODE.FULL);
			//robot.setMode(IRobotCreateV2.MODE.SAFE);
			Thread.sleep(1000);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
	List<String> parseCommand(String com) {
		List<String> ret = new ArrayList<String>();
		
		int i = com.indexOf('(');
		if(i<0) {
			ret.add(com.toUpperCase().trim());
			return ret;
		}
		int j = com.indexOf(')');
		ret.add(com.substring(0, i).toUpperCase().trim());
		String params = com.substring(i+1,j);
		String[] pas = params.split(",");
		for(String p : pas) {
			ret.add(p.toUpperCase().trim());
		}
		
		return ret;
	}		

	@Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		String command = request.getParameter("command");
						
		if(command!=null) {
			List<String> com = parseCommand(command);
			switch(com.get(0)) {
			case "DRIVE":
				int vel = Integer.parseInt(com.get(1));
				int rad = 32768;
				if(com.size()>2) {
					rad = Integer.parseInt(com.get(2));
				}
				robot.drive(vel, rad);
				response.getWriter().println("{}");
				break;			
			default:
				response.setStatus(403);				
			}			
			
		} 
				
    }

}

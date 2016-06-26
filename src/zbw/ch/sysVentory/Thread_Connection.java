package zbw.ch.sysVentory;

import java.io.IOException;


public class Thread_Connection extends Thread
{
	public int repeattime = 300000;
	public static final int ERRORTIME = 5000;
	public GlobalData gData;
	public UI_Main gui;
	public ServerConnection server;
	public String responseString = "";
	
	public static final int CONNECTION = 2;
	public static final int TRANSFER_GET = 3;
	public static final int TRANSFER_SENDXML = 4;
	public static final int WAIT = 1;
	public static final int ERROR = -1;
	
	public static final int EXECUTESCAN = 2;
	public static final int SAVEXMLDATA = 3;
	public int status = -1;
	private int counter = 0;

	public Thread_Connection(int _time, GlobalData _gData, UI_Main _gui)
	{
		this.repeattime = _time;
		this.gData = _gData;
		this.gui = _gui;
		this.server = new ServerConnection(gData);
		this.gData.setConnectionState(CONNECTION);
	}
    public void run() {
        while(true)
        {
        	status = this.gData.getConnectionState();
        	setStatusText();
//        	gui.setStatusConn(Integer.toString(status));
          try
          {
  			switch(status)
  			{
  				case WAIT:
  					counter++;
  					if(gData.getConnectionState() != TRANSFER_SENDXML && counter > gData.getConnRefreshTime()) // 30 entspricht etwa 30 sekunden pause
  						gData.setConnectionState(CONNECTION);
  					
  					break;
  				case CONNECTION:
  					counter=0;
  					if(!verbindungsaufbau())
  					{
  						if(gData.getConnectionState() != TRANSFER_SENDXML)
  							gData.setConnectionState(ERROR);
  						break;
  					}
  					saveParameter();
  					if(gData.getConnectionState() != TRANSFER_SENDXML)
  						gData.setConnectionState(WAIT);  					
  					break;  					  					
  				case TRANSFER_GET:
  					break;
  				case TRANSFER_SENDXML:
  					counter=0;
  					
  					if(server.SendXML().isEmpty())
  					{
  						gData.setConnectionState(ERROR);
  						break;
  					}
					System.out.println();
  					gData.setConnectionState(CONNECTION);
  					break;
  				case ERROR:
  					sleep(ERRORTIME); // warte bisschen bis verbindungs-wiederaufbau
  					if(verbindungsaufbau())
  					{
  						gData.setConnectionState(WAIT);
  					}
  					break;
  				default:
  					counter = 0;
  					gData.setConnectionState(WAIT);
  					break;
  			}        	  
        	sleep(repeattime);
          }
          catch(InterruptedException e)
          {
          } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
      }
	private boolean verbindungsaufbau()
	{
		try {
			responseString = server.SendGetRequest();
	//		System.out.println("response: " + responseString);
		} catch (IOException e) {
			// e.printStackTrace();
			return false;
		}
		if(responseString.isEmpty())
		{
			return false;
		}
		return true;
	}
	private void saveParameter()
	{
		String timeout = "";
		String iprange = "";
		boolean scanNow = false;
		String str = "";
		
		if(!responseString.isEmpty())
		{
			try
			{
				String[] parts = responseString.split("\n");
				String[] parts2 = parts[0].split(" ");
				iprange = parts2[1];
				
				parts2 = parts[1].split(" ");
				timeout = parts2[1];
				
				if(parts[2].indexOf("true") >= 0)
				{
					scanNow = true;
					System.out.println("scannow = true");
				}
				else
				{
					scanNow = false;
					System.out.println("scannow = false");
				}					
				
				timeout = timeout.replace("\"", "");
				iprange = iprange.replace("\"", "");

				gData.setIprange(iprange);			
				gData.setIntervall(Long.parseLong(timeout));
				gData.setScanNow(scanNow);
				
				System.out.println("iprang: " + iprange);
				System.out.println("timeout: " + timeout);	
				
				gui.setParam(iprange + " - " + timeout + " - " + scanNow);
			}
			catch(Exception e)
			{
				
			}
		}
		responseString = "";		
	}
	
	private void setStatusText() {
		
		String statusTxt = "";
		switch(status)
		{
		case ERROR:
			statusTxt = "ERROR";
			break;
		case WAIT:
			statusTxt = "WAIT";
			break;
		case CONNECTION:
			statusTxt = "GET PARAMETER";
			break;
		case TRANSFER_SENDXML:
			statusTxt = "TRANSFER: SEND XML";
			break;
		default:
			statusTxt = "WAIT";
		
		}
		gui.setStatusConn(statusTxt);
		
	}	
}



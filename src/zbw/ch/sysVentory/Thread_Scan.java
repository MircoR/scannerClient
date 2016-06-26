package zbw.ch.sysVentory;

import java.util.Timer;

public class Thread_Scan extends Thread
{

	public GlobalData gData;	
	public UI_Main gui;	

	
	public static final int WAIT = 1;
	public static final int ERROR = -1;	
	public static final int EXECUTESCAN = 2;
	public static final int SAVEXMLDATA = 3;

	public static final int TRANSFER_SENDXML = 4;
	public static final int RESETTIMER = 5;
	
	public Timer timer = new Timer();	
	public long localIntervall = -1; 
	
	public int status = -1;
	
	private ReschedulableTimer rescheduleTimer;
	private Runnable r;
	private boolean firstcall = true;
	
	public Thread_Scan(GlobalData _gData, UI_Main _gui)
	{
		this.gData = _gData;
		this.gui = _gui;

	    r = new ScheduleTask(gData);
	    rescheduleTimer = new ReschedulableTimer();
	    rescheduleTimer.schedule(r, 1000);
	
	}
	
	public void run()
	{
		while(true)
        {
	       	status = this.gData.getScanState();
        	setStatusText();			
			switch(status)
			{
				case WAIT:
			      	if(time4script())
			      		this.gData.setScanState(EXECUTESCAN);
					
					break;
				case EXECUTESCAN:
					System.out.println("start scan....");					
					runScript();
					System.out.println("scan done");
					this.gData.setScanState(SAVEXMLDATA);
					break;
				case SAVEXMLDATA:
					this.gData.setConnectionState(TRANSFER_SENDXML);
					this.gData.setScanState(RESETTIMER);
					break;
				case RESETTIMER:
					rescheduleTimer.reschedule(gData.getIntervall()*1000); // *60*60
					gData.settimerExpired(false);
					this.gData.setScanState(WAIT);
					break;
				default:
					this.gData.setScanState(WAIT);
					break;
			}
        }
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
		case EXECUTESCAN:
			statusTxt = "EXECUTESCAN";
			break;
		case SAVEXMLDATA:
			statusTxt = "SAVEXMLDATA";
			break;
		case TRANSFER_SENDXML:
			statusTxt = "TRANSFER_SENDXML";
			break;
		case RESETTIMER:
			statusTxt = "RESETTIMER";
			break;
		default:
			statusTxt = "WAIT";
		
		}
		gui.setStatusScan(statusTxt);
		
	}

	public void runScript()
	{
		ExecutePowershellScript script = new ExecutePowershellScript(this.gData);
		try {
			script.runScript();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
	}
	
	public boolean time4script()
	{
		if(gData.getScanNow())
		{
			if(gData.getConnectionState() == WAIT)
			{
				return true;
			}
		}

		if(gData.gettimerExpired())
		{
			if(gData.getIprange().isEmpty())
				return false;
			
			if(gData.getIntervall() == -1)
			{
				gData.settimerExpired(false);
				System.out.println("timer: no valid intervall");

				rescheduleTimer.reschedule(3000);  // noch kein gültiges Intervall empfangen, reset timer auf 3 sek
			}
			else if(validIntervall())
			{
				if(firstcall)
				{
					firstcall = false;
					rescheduleTimer.reschedule(gData.getIntervall()*1000);//*60*60*1000); umrechnung von h in ms
					gData.settimerExpired(false);
					System.out.println("timer: firstcall");
					return false;
				}
				if(gData.getScanState() != EXECUTESCAN)
				{
					if(gData.getConnectionState() != ERROR && gData.getConnectionState() != TRANSFER_SENDXML)
					{
						System.out.println("timer: true -> start scan");
						return true;
					}
					else
					{
						rescheduleTimer.reschedule(gData.getIntervall()*1000); // *60*60
						gData.settimerExpired(false);
					}
				}
			}
		}
		return false;
	}

	private boolean validIntervall()
	{
		long intervall = gData.getIntervall();
		if(intervall >= 1 && intervall <= 5500) // intervall zwischen 1 h und 1 Monat?
			return true;

		return false;
	}

	


}
	  
  

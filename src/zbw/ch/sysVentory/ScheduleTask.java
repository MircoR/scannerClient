package zbw.ch.sysVentory;

public class ScheduleTask implements Runnable
{
	public GlobalData gData;
	
	public ScheduleTask(GlobalData _gData)
	{
		this.gData = _gData;
	}
    public void run()
    {
  		gData.settimerExpired(true);    		
   		System.out.println("timer abgelaufen");

    }	
}

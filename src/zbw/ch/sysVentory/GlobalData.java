package zbw.ch.sysVentory;

import java.util.Date;

public class GlobalData {
	
	private String iprange;
	private Date startdate;
	private long intervall;
	private String compId;
	private int Connection_State = 1;
	private int Scan_State = 1;
	private boolean scanNow = false;
	private boolean timerExpired = false;
	private String URL;
	private int ConnRefreshTime = 0;
	
	public GlobalData()
	{
		// default init
		this.iprange = "";
		this.intervall = -1;
		this.ConnRefreshTime = 30;

	}
	public int getConnRefreshTime()
	{
		return ConnRefreshTime;
	}
	public void setConnRefreshTime(int refreshtime)
	{
		ConnRefreshTime = refreshtime;
	}	
	public String getURL()
	{
		return URL;
	}
	public void setURL(String url)
	{
		this.URL = url;
	}
	public String getIprange()
	{
		return iprange;
	}
	public Date getStartdate()
	{
		return startdate;
	}
	public long getIntervall()
	{
		return intervall;
	}
	public void setIprange(String _iprange)
	{
		iprange= _iprange;
	}
	public void setStartdate(Date _date)
	{
		startdate = _date;
	}
	public void setIntervall(long _intervall)
	{
		intervall = _intervall;
	}
	public void setCompId(String _compId)
	{
		compId = _compId;
	}
	public String getCompId()
	{
		return compId;
	}
	public int getConnectionState()
	{
		return Connection_State;
	}
	public void setConnectionState(int _state)
	{
		this.Connection_State = _state;
	}
	public int getScanState()
	{
		return Scan_State;
	}
	public void setScanState(int _state)
	{
		this.Scan_State = _state;
	}
	public void setScanNow(boolean _set)
	{
		this.scanNow = _set;
	}
	public boolean getScanNow()
	{
		return this.scanNow;
	}
	public void settimerExpired(boolean _set)
	{
		this.timerExpired = _set;
	}
	public boolean gettimerExpired()
	{
		return this.timerExpired;
	}	
	
}

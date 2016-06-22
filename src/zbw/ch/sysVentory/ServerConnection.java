package zbw.ch.sysVentory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.net.*;
import java.io.*;
import java.io.InputStreamReader;

/**
 * @author riedener
 *
 */
public class ServerConnection {

	public String urladdress = "";
	public String charset = java.nio.charset.StandardCharsets.UTF_8.name();
	public String param1 = "bar";
	public String param2 = "value2";
	
	public GlobalData gData;
	
	public ServerConnection(GlobalData _gData)
	{
		gData = _gData;
		urladdress = _gData.getURL() + "/getrequest";
	}
		
	public String SendGetRequest() throws IOException
	{
	    String Param = gData.getCompId();
	    String response = "";
		try
		{
			Param = "getData=" + URLEncoder.encode( Param.trim(), "UTF-8" );
			URL u = new URL( urladdress + "?" + Param );
			response = new Scanner( u.openStream() ).useDelimiter( "\\Z" ).next();
		}
		catch(Exception e)
		{
			System.out.println("connection error: getRequest");
		}
		return response;
	}
	
	public String SendXML() throws IOException
	{
		PostXML sendXML = new PostXML(gData);
		try {
			sendXML.postXML();
			System.out.println("xml transferred");
		} catch (Exception e) {
			System.out.println("connection error: send XML");
				//e.printStackTrace();
			return "";
		}
		return "wow";
	}
/*
	public String getResponse() throws IOException
	{
        URL url = new URL("http://localhost");
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                                    yc.getInputStream()));
        String inputLine = "";
        while ((inputLine = in.readLine()) != null) 
            System.out.println(inputLine);
        in.close();
  
		return "";
	}
	*/
}

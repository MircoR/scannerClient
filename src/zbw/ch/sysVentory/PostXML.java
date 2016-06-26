package zbw.ch.sysVentory;


import java.io.File;
import java.io.FileInputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Example how to use unbuffered chunk-encoded POST request.
 */
public class PostXML
{
	
	public GlobalData gData;
	
	public PostXML(GlobalData _gData)
	{
		this.gData = _gData;
	}
	
    public void postXML() throws Exception
    {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String filename = getFileName(); 
        
        try {
            HttpPost httppost = new HttpPost(gData.getURL() + "/getXML");
            File file = new File(filename);

           InputStreamEntity reqEntity = new InputStreamEntity(
                    new FileInputStream(file), -1, ContentType.APPLICATION_OCTET_STREAM);
            reqEntity.setChunked(true);
            // It may be more appropriate to use FileEntity class in this particular
            // instance but we are using a more generic InputStreamEntity to demonstrate
            // the capability to stream out data from any arbitrary source
            //
            // FileEntity entity = new FileEntity(file, "binary/octet-stream");

            httppost.setEntity(reqEntity);

            System.out.println("Executing request: " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println(EntityUtils.toString(response.getEntity()));
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        deleteXMLFile(filename);
    }

	public String getFileName()
	{
		String filename = ".\\scan\\" + gData.getCompId() + ".xml";
//		String filename = ".\\scan\\test.xml";// 
		System.out.println("filename" + filename);
		return filename;
	}
	
    public void deleteXMLFile(String filename)
    {
    	// lösche alle xmls aus Folder \scan
        File file = new File(filename);
    	if(file.exists())
    	{
    		file.delete();
    		System.out.print("deleted: ");
    	}
    	System.out.println(file);
    }	

}


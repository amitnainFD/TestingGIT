package com.freshdirect.fdstore.content.customerrating;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.log.LoggerFactory;

@Deprecated
public class DownloadFeedProcessTask {

	
	private static final Logger LOGGER = LoggerFactory.getInstance(DownloadFeedProcessTask.class);
	
	private String FTP_URL = "";
	private String FEED_FILE = "";
	private String FTP_SOURCEPATH = "";
	private String FTP_USER = "";
	private String FTP_PASSWORD = "";
	private String DOWNLOAD_PATH = "";

	public BazaarvoiceFeedProcessResult process(){
		
		FTP_URL = FDStoreProperties.getBazaarvoiceFtpUrl();
		FEED_FILE = FDStoreProperties.getBazaarvoiceDownloadFeedFile();
		FTP_SOURCEPATH = FDStoreProperties.getBazaarvoiceDownloadFeedSourcePath();
		FTP_USER = FDStoreProperties.getBazaarvoiceFtpUsername();
		FTP_PASSWORD = FDStoreProperties.getBazaarvoiceFtpPassword();
		DOWNLOAD_PATH = FDStoreProperties.getBazaarvoiceDownloadFeedTargetPath();
		
		try {
			downloadFeedFile();
			extractFeedFile();
			
		} catch (FDResourceException e) {
			LOGGER.error("Bazaarvoice feed creation failed!",e);
			return new BazaarvoiceFeedProcessResult(false, e.getMessage());
		}
		
		LOGGER.info("Feed creation complete.");
		return new BazaarvoiceFeedProcessResult(true, null);
	}
	
	private void downloadFeedFile() throws FDResourceException {
		
		LOGGER.info("downloading Bazaarvoice feed file from " + FTP_USER +"@"+ FTP_URL);

		FTPClient client = new FTPClient();
        FileOutputStream fos = null;
        
        try {
            client.connect(FTP_URL);
			client.setDefaultTimeout(600000);
			client.setDataTimeout(600000);
            
            if (!client.login(FTP_USER, FTP_PASSWORD)) {
            	throw new FDResourceException("ftp login failed"); 
            }
			client.setFileType(FTPClient.BINARY_FILE_TYPE);
    		client.enterLocalPassiveMode();
    		client.changeWorkingDirectory(FTP_SOURCEPATH);

            fos = new FileOutputStream(DOWNLOAD_PATH + FEED_FILE);
            if (!client.retrieveFile(FEED_FILE, fos)) {
            	throw new FDResourceException("ftp file download failed");
            }
            fos.flush();
            client.logout();

        } catch (Exception e) {
            throw new FDResourceException("downloadFeedFile", e);
            
        } finally {
            try {
                if (fos != null) {
                	fos.close();
                }
                client.disconnect();
            } catch (IOException e) {
            }
        }
    }
	
	private void extractFeedFile() throws FDResourceException {
    	
		FileOutputStream fos = null;
		GZIPInputStream gzis = null;
        try {
			byte[] buffer = new byte[1024];
			gzis = new GZIPInputStream(new FileInputStream(DOWNLOAD_PATH + FEED_FILE));
	 
    	   File newFile = new File(DOWNLOAD_PATH + FEED_FILE.substring(0, FEED_FILE.length() - 3));
 
    	   LOGGER.info("file unzip : "+ newFile.getAbsoluteFile());
 
            new File(newFile.getParent()).mkdirs();
 
            fos = new FileOutputStream(newFile);             
 
            int len;
            
            while ((len = gzis.read(buffer)) > 0) {
            	fos.write(buffer, 0, len);
            }
 
	 
        } catch (IOException e) {
        	throw new FDResourceException("extractFeedFile", e);
            
        } finally {
            try {
                if (fos != null) {
                	fos.flush();
                    fos.close();
                }
                if (gzis != null) {
	    	    	gzis.close();
                }
                
            } catch (IOException e) {
            	throw new FDResourceException("extractFeedFile", e);
            }
        }
	}
	


}

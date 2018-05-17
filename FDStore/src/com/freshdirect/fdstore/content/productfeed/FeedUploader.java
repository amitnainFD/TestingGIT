package com.freshdirect.fdstore.content.productfeed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Category;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class FeedUploader {
	
	private static Category LOGGER = LoggerFactory.getInstance( FeedUploader.class );
	
	private final static JSch jsch = new JSch();
	
	public static void uploadToFtp(String ftpUrl, String ftpUser, String ftpPassword, String ftpDirectory, String prodFeedFilePath) throws FDResourceException {
		
		FTPClient client = new FTPClient();
		client.setDefaultTimeout(600000);
		client.setDataTimeout(600000);
		
        FileInputStream fis = null;
        LOGGER.info("FTP: connecting to host " + ftpUrl);
        
        try {
            client.connect(ftpUrl);
            
            if (!client.login(ftpUser, ftpPassword)) {
            	throw new FDResourceException("feed ftp login failed "+ ftpUrl); 
            }
    		client.enterLocalPassiveMode();

            File tmpFile = new File(prodFeedFilePath);
    		fis = new FileInputStream(tmpFile);
            if (!client.storeFile(tmpFile.getName(), fis)) {
            	throw new FDResourceException("feed ftp file store failed "+ ftpUrl);
            }
            
            client.logout();

        } catch (IOException e) {
            throw new FDResourceException("feed ftp uploadFile: "+ ftpUrl + e.getMessage(), e);
            
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                client.disconnect();
            } catch (IOException e) {
            }
        }
	}
	public static void uploadToSftp(String sftpHost, String sftpUser, String sftpPasswd, String sftpDirectory, String prodFeedFilePath) throws FDResourceException {
		
		ChannelSftp sftp = null;
		Session session = null;
		Channel channel = null;
		
		LOGGER.info("SFTP: connecting to host " + sftpHost);
		
		try {
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session = jsch.getSession(sftpUser, sftpHost, 22);
			session.setPassword(sftpPasswd);				
			session.setConfig(config);
			
			session.connect();
			channel = session.openChannel("sftp");
			sftp = (ChannelSftp) channel;
			LOGGER.info("SFTP: Connecting..");
			FileInputStream fis = new FileInputStream(prodFeedFilePath);
			sftp.connect();
			
			if(sftpDirectory == null) {
				sftpDirectory = "";
			}
			sftp.put(fis, sftpDirectory + prodFeedFilePath);
			fis.close();
			
		} catch(Exception e) {
			throw new FDResourceException("feed sftp uploadFile: "+e.getMessage(), e);
		} finally{
			LOGGER.info("SFTP: Disconnecting..");
			if(null !=sftp){
				sftp.disconnect();
			}
			if(null != session){
				session.disconnect();
			}			
			if(null != channel){
				channel.disconnect();
			}
		}
		
	}
	
	public static void uploadToS3(String accessKey, String secretKey, String bucketName, String prodFeedFilePath) throws FDResourceException {
		
		LOGGER.info("Amazon S3: connecting to bucket " + bucketName);
       
        try {
        	AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

            AmazonS3 s3 = new AmazonS3Client(credentials);
            Region usWest2 = Region.getRegion(Regions.US_EAST_1);
            s3.setRegion(usWest2);
            
            File file = new File(prodFeedFilePath);
            s3.putObject(new PutObjectRequest(bucketName, file.getName(), file));

           
        } catch (Exception ase) {
        	LOGGER.error("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
        	LOGGER.error("Error Message:    " + ase.getMessage());
        	throw new FDResourceException("feed s3 uploadFile: " + ase.getMessage(), ase);
        } 
		
	}

}

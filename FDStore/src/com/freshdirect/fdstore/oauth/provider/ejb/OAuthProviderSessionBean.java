/*
 * Copyright 2007 AOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freshdirect.fdstore.oauth.provider.ejb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Category;

import com.freshdirect.fdstore.oauth.provider.OAuthProviderDao;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.ConfigHelper;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * Utility methods for providers that store consumers, tokens and secrets in 
 * local cache (HashSet). Consumer key is used as the name, and its credentials are 
 * stored in HashSet.
 *
 * @author Praveen Alavilli, integrated and customized by Tamas Gelesz
 */
@SuppressWarnings("rawtypes")
public class OAuthProviderSessionBean extends SessionBeanSupport {

	private static final long serialVersionUID = -8205445897305502630L;
	private static Category LOGGER = LoggerFactory.getInstance( OAuthProviderSessionBean.class );
    private static final OAuthValidator VALIDATOR = new SimpleOAuthValidator();
    private static Properties consumerProperties = null;
    private static final Map<String, OAuthConsumer> ALL_CONSUMERS = Collections.synchronizedMap(new HashMap<String,OAuthConsumer>(10));
    private static final int ACCESSOR_DELETE_TIME_INTERVAL_HOURS = 24;
    
    static {
    	try {
			consumerProperties = ConfigHelper.getPropertiesFromClassLoader("oauthprovider.properties");
       
	        // for each entry in the properties file create a OAuthConsumer
	        for(Map.Entry prop : consumerProperties.entrySet()) {
	            String consumer_key = (String) prop.getKey();
	            // make sure it's key not additional properties
	            if(!consumer_key.contains(".")){
	                String consumer_secret = (String) prop.getValue();
	                if(consumer_secret != null){
	                    String consumer_description = (String) consumerProperties.getProperty(consumer_key + ".description");
	                    String consumer_callback_url =  (String) consumerProperties.getProperty(consumer_key + ".callbackURL");
	                    // Create OAuthConsumer w/ key and secret
	                    OAuthConsumer consumer = new OAuthConsumer(consumer_callback_url,consumer_key,consumer_secret,null);
	                    consumer.setProperty("name", consumer_key);
	                    consumer.setProperty("description", consumer_description);
   	                    ALL_CONSUMERS.put(consumer_key, consumer);
	                }
	            }
	        }
		} catch (IOException e) {
			LOGGER.error("oauthprovider.properties load failed",e);
		}
    }

    public void deleteOldAccessors() throws OAuthException {
        Connection conn = null;
        try {
        	Calendar calendar = Calendar.getInstance();
        	calendar.add(Calendar.HOUR, -1 * ACCESSOR_DELETE_TIME_INTERVAL_HOURS);
        	conn = getConnection();
        	OAuthProviderDao.deleteAccessors(conn, calendar.getTime());

        } catch (SQLException e) {
			throw new OAuthException(e);
		} finally {
        	close(conn);
        }
    }
    
    public OAuthConsumer getConsumer(OAuthMessage requestMessage) throws IOException, OAuthProblemException {
        
        OAuthConsumer consumer = null;
        String consumer_key = requestMessage.getConsumerKey();
        
        consumer = ALL_CONSUMERS.get(consumer_key);
        
        if(consumer == null) {
            OAuthProblemException problem = new OAuthProblemException("token_rejected");
            throw problem;
        }
        
        return consumer;
    }
    
    /**
     * Get the access token and token secret for the given oauth_token. 
     * @throws OAuthException 
     */
    public OAuthAccessor getAccessor(OAuthMessage requestMessage) throws IOException, OAuthException {
        
        String consumerToken = requestMessage.getToken();

        Connection conn = null;
        OAuthAccessor accessor = null;
        try {
        	conn = getConnection();
        	accessor = OAuthProviderDao.getAccessorByToken(conn, consumerToken, ALL_CONSUMERS);

        } catch (SQLException e) {
			throw new OAuthException(e);
		} finally {
        	close(conn);
        }
        
        if(accessor == null){
            OAuthProblemException problem = new OAuthProblemException("token_expired");
            throw problem;
        }
        
        return accessor;
    }

    /**
     * Set the access token 
     */
    public OAuthAccessor markAsAuthorized(OAuthAccessor accessor, String userId) throws OAuthException {
        
        accessor.setProperty("user", userId);   
        accessor.setProperty("authorized", Boolean.TRUE);
        
        Connection conn = null;
        try {
        	conn = getConnection();
        	OAuthProviderDao.storeAccessor(conn, accessor);

        } catch (SQLException e) {
			throw new OAuthException(e);
		} finally {
        	close(conn);
        }
		
		return accessor;
    }
    

    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    public OAuthAccessor generateRequestToken(OAuthAccessor accessor) throws OAuthException {

        // generate oauth_token and oauth_secret
        String consumer_key = (String) accessor.consumer.getProperty("name");
        // generate token and secret based on consumer_key
        
        // for now use md5 of name + current time as token
        String token_data = consumer_key + System.nanoTime();
        String token = DigestUtils.md5Hex(token_data);
        // for now use md5 of name + current time + token as secret
        String secret_data = consumer_key + System.nanoTime() + token;
        String secret = DigestUtils.md5Hex(secret_data);
        
        accessor.requestToken = token;
        accessor.tokenSecret = secret;
        accessor.accessToken = null;
        
        Connection conn = null;
        try {
        	conn = getConnection();
        	OAuthProviderDao.storeAccessor(conn, accessor);

        } catch (SQLException e) {
			throw new OAuthException(e);
		} finally {
        	close(conn);
        }
		
		return accessor;
    }
    
    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    public OAuthAccessor generateAccessToken(OAuthAccessor accessor) throws OAuthException {

        // generate oauth_token and oauth_secret
        String consumer_key = (String) accessor.consumer.getProperty("name");
        // generate token and secret based on consumer_key
        
        // for now use md5 of name + current time as token
        String token_data = consumer_key + System.nanoTime();
        String token = DigestUtils.md5Hex(token_data);
        
        accessor.requestToken = null;
        accessor.accessToken = token;
        
        Connection conn = null;
        try {
        	conn = getConnection();
        	OAuthProviderDao.storeAccessor(conn, accessor);

        } catch (SQLException e) {
			throw new OAuthException(e);
		} finally {
        	close(conn);
        }
		
		return accessor;
    }

    public String getProviderUrl(){
    	return consumerProperties.getProperty("provider.url");
    }
    
    public void validateMessage(OAuthMessage message, OAuthAccessor accessor) throws OAuthException, IOException, URISyntaxException {
    	VALIDATOR.validateMessage(message, accessor);
    }
}

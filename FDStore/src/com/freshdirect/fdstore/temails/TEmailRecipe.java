package com.freshdirect.fdstore.temails;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.MediaI;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.mail.FDEmailFactory;
import com.freshdirect.framework.util.log.LoggerFactory;

public class TEmailRecipe extends Recipe {

	public static final Category LOGGER = LoggerFactory.getInstance(TEmailRecipe.class);
	
	public TEmailRecipe(ContentKey key) {
		super(key);
		// TODO Auto-generated constructor stub
	}
	
	public String getRecipeDescription(){
		return loadMedia(super.getDescription());
	}
	
	public String getRecipeIngredientsMedia(){
		return loadMedia(super.getIngredientsMedia());
	}
	
	public String getRecipePreparationMedia(){
		return loadMedia(super.getPreparationMedia());
	}
	
	public String getRecipeCopyrightMedia(){
		return loadMedia(super.getCopyrightMedia());
	}

	/**
	 *  Load media contents.
	 *  
	 *  @param media the media to load
	 *  @return the contents of the media, as a string,
	 *          or an empty string on errors
	 */
	private String loadMedia(MediaI media) {
		// this is the same code as in FDRecipeEmail
		// TODO: somehow refactor to avoid code duplication
		
		if (media == null) {
			return "";
		}
		
		InputStream     in  = null;
		StringBuffer		out = new StringBuffer(); 
		try {

			URL url = resolve(FDStoreProperties.getMediaPath(), media.getPath());
			in = url.openStream();
			if (in == null) {
				return "";
			}

			byte[] buf = new byte[4096];
			int i;
			while ((i = in.read(buf)) != -1) {
				out.append(new String(buf, 0, i));
			}

			return out.toString();

		} catch (FileNotFoundException e) {
			LOGGER.warn("Media file not found " + media.getPath());
			return "";

		} catch (IOException e) {
			LOGGER.warn("Failed to load resource", e);

			return "";
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ex) {
			}
		}
	}

	
	/**
	 *  Resolve a path.
	 *  
	 *  @param rootPath the base path
	 *  @param childPath the path to resolve, relative to rootPath
	 *  @return a full URL to childPath, in relation to rootPath
	 */
	public static URL resolve(String rootPath, String childPath) throws IOException {
		// this is the same code as in FDRecipeEmail
		// TODO: somehow refactor to avoid code duplication
		
		URL url = new URL(rootPath);
		if (childPath.startsWith("/")) {
			childPath = childPath.substring(1, childPath.length());
		}
		url = new URL(url, childPath);

		if (!url.toString().startsWith(rootPath)) {
			throw new IOException("Child path not under root");
		}

		return url;
	}
	
}

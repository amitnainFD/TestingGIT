package com.freshdirect.wcms;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.loader.CacheLoaderFactory;

import com.freshdirect.fdstore.content.CMSPageRequest;
import com.freshdirect.fdstore.content.CMSWebPageModel;

public class FDCacheLoaderFactory extends CacheLoaderFactory {
	
	public class CMSCacheLoaderAdapter implements CacheLoader{
		
		@Override
		public CacheLoader clone(Ehcache arg0)
				throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void dispose() throws CacheException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Status getStatus() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void init() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object load(Object arg0) throws CacheException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object load(Object arg0, Object arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map loadAll(Collection arg0) {
			CMSPageRequest request = new CMSPageRequest();
			List<CMSWebPageModel> models = CMSContentFactory.getInstance().getCMSPageByParameters(request);
			Map map = new LinkedHashMap();
			for(CMSWebPageModel page: models){
				map.put(page.getTitle(), page);
			}
			
			return map;
		}

		@Override
		public Map loadAll(Collection arg0, Object arg1) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	CacheLoader webPageCacheLoader = null;
	
	@Override
	public CacheLoader createCacheLoader(Ehcache cache, Properties properties) {
		if(cache.getName().equals("cmsPageCache")){
			webPageCacheLoader = new CMSCacheLoaderAdapter();
			return webPageCacheLoader;
		}
		return null;
	}
}
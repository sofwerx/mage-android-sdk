package mil.nga.giat.mage.sdk.glide;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.resize.ImageManager;
import com.bumptech.glide.resize.cache.DiskCache;
import com.bumptech.glide.resize.cache.DiskLruCacheWrapper;

public class MageDiskCache implements DiskCache {
	private static final int DEFAULT_DISK_CACHE_SIZE = 250 * 1024 * 1024;
	private DiskCache cache;
	
	public MageDiskCache(Context context) throws IOException {
		cache = DiskLruCacheWrapper.get(ImageManager.getPhotoCacheDir(context), DEFAULT_DISK_CACHE_SIZE);
	}
	
	@Override
    public InputStream get(String key) {
		Log.d("cache", "Get the key: " + key);
		return cache.get(key);
//		return null;
    }

    @Override
    public void put(String key, Writer writer) {
    	Log.d("cache", "Put the key: " + key);
		cache.put(key, writer);
    }

	@Override
	public void delete(String key) {
		Log.d("cache", "Delete the key: " + key);
		cache.delete(key);
	}

}

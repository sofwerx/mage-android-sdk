package mil.nga.giat.mage.sdk.glide;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;

public class MageDiskCache implements DiskCache {
	private static final int DEFAULT_DISK_CACHE_SIZE = 250 * 1024 * 1024;
	private DiskCache cache;
	
	public MageDiskCache(Context context) throws IOException {
		cache = DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(context), DEFAULT_DISK_CACHE_SIZE);
	}

	@Override
	public void delete(Key key) {
		Log.d("cache", "Delete the key: " + key);
		cache.delete(key);
	}

	@Override
	public File get(Key key) {
		Log.d("cache", "Get the key: " + key);
		return cache.get(key);
	}

	@Override
	public void put(Key key, Writer writer) {
    	Log.d("cache", "Put the key: " + key);
		cache.put(key, writer);
	}

}

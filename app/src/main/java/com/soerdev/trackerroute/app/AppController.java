package com.soerdev.trackerroute.app;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.soerdev.trackerroute.volley.LruBitmapChace;

import static com.android.volley.VolleyLog.TAG;

public class AppController extends Application{

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    LruBitmapChace lruBitmapChace;

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance(){
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if(mImageLoader == null){
            getLruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, lruBitmapChace);
        }
        return this.mImageLoader;
    }

    public LruBitmapChace getLruBitmapCache() {
        if(lruBitmapChace == null)
            lruBitmapChace = new LruBitmapChace();
        return  this.lruBitmapChace;
    }

    public <T> void addToRequestQueue(Request<T> request, String tag){
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(request);
    }

    public <T> void addToRequestQueue(Request<T> request){
        request.setTag(request);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequest(Object tag){
        if(mRequestQueue != null){
            mRequestQueue.cancelAll(tag);
        }
    }
}

package com.feicui.administrator.volley;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    private RequestQueue referenceQueue = null;
    private String url = "http://avatar.csdn.net/6/6/D/1_lfdfhl.jpg";
    private ImageView imageView;
    private NetworkImageView networkImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        networkImageView = (NetworkImageView) findViewById(R.id.networkImageView);
        // 初始化请求序列
        referenceQueue = Volley.newRequestQueue(this);

        // Volley加载方法1：利用ImageRequest为ImageView加载网络图片，不需要自定义图片内存缓存
        volleyImageRequest();
        setTitle("ImageRequest");

        // Volley加载方法2：利用ImageLoader为ImageView加载网络图片,需要自定义图片内存缓存
        volleyImageLoader();
        setTitle("ImageLoader");

        // Volley加载方法3：利用ImageLoader和NetworkImageView为ImageView加载网络图片,需要自定义图片内存缓存
        volleyNetworkImageView();
        setTitle("NetworkImageView");
    }


    /**
     * 利用ImageRequest为ImageView加载网络图片，不需要自定义图片内存缓存
     */
    private void volleyImageRequest() {
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        }, 300, 300, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // 获取图片失败，加载一个默认图片
                imageView.setImageResource(R.drawable.ic_launcher);
            }
        });
        referenceQueue.add(imageRequest);
    }

    /**
     * 利用ImageLoader为ImageView加载网络图片,需要自定义图片内存缓存
     */
    private void volleyImageLoader() {
        ImageLoader imageLoader = new ImageLoader(referenceQueue, new MyBitmapCache().getInstance());
        ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(imageView, R.drawable.ic_launcher, R.drawable.ic_launcher);
        imageLoader.get(url, imageListener, 250, 250);
    }

    /**
     * Volley加载方法3：利用ImageLoader和NetworkImageView为ImageView加载网络图片,需要自定义图片内存缓存
     */
    private void volleyNetworkImageView() {
        ImageLoader imageLoader = new ImageLoader(referenceQueue, new MyBitmapCache().getInstance());
        networkImageView.setDefaultImageResId(R.drawable.ic_launcher);
        networkImageView.setErrorImageResId(R.drawable.ic_launcher);
        networkImageView.setImageUrl(url, imageLoader);
    }

    static class MyBitmapCache implements ImageLoader.ImageCache {

        private static MyBitmapCache bitmapCache = null;
        private static LruCache<String, Bitmap> lruCache = null;

        private MyBitmapCache() {
            int memoryCount = (int) Runtime.getRuntime().maxMemory();
            /**获取剩余内存的8分之一作为缓存*/
            int cacheSize = memoryCount / 8;
            lruCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
//                    return bitmap.getByteCount();
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };
        }

        public static MyBitmapCache getInstance() {
            if (bitmapCache == null) {
                bitmapCache = new MyBitmapCache();
            }
            return bitmapCache;
        }

        @Override
        public Bitmap getBitmap(String url) {
            return lruCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            lruCache.put(url, bitmap);
        }
    }
}

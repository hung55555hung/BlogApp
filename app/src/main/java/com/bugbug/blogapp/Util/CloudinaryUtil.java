package com.bugbug.blogapp.Util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bugbug.blogapp.Config.CloudinaryConfig;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudinaryUtil {
    public static void uploadImage(Context context, Uri imageUri, UploadResultListener listener) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            Exception exception;

            @Override
            public void run() {
                try {
                    File file = uriToFile(context, imageUri);
                    Cloudinary cloudinary = CloudinaryConfig.getInstance();
                    Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "post"
                    ));
                    String imageUrl = uploadResult.get("secure_url").toString();
                    new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(imageUrl));

                } catch (Exception e) {
                    exception = e;
                    Log.e("CloudinaryUtil", "Error uploading image", exception);
                    new Handler(Looper.getMainLooper()).post(() -> listener.onFailure(exception));
                } finally {
                    executorService.shutdown(); // Ensure the executor service is shut down
                }
            }
        });
    }

    private static File uriToFile(Context context, Uri uri) throws IOException {
        String fileName = new Date().getTime() + "";
        File tempFile = new File(context.getCacheDir(), fileName);
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    public interface UploadResultListener {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }
}

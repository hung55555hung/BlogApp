package com.bugbug.blogapp.Util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bugbug.blogapp.Config.CloudinaryConfig;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudinaryUtil {
    private static final String TAG = "CloudinaryUtil";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public static void uploadImage(Context context, Uri imageUri, UploadImageResultListener listener) {
        executorService.execute(() -> {
            File tempFile = null;
            try {
                tempFile = uriToFile(context, imageUri);
                Cloudinary cloudinary = CloudinaryConfig.getInstance();
                Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", "user"
                ));
                String imageUrl = uploadResult.get("secure_url").toString();
                new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(imageUrl));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure(e));
            } finally {
                cleanupTempFile(tempFile);
            }
        });
    }

    public static void uploadPostImages(Context context, List<Uri> imageUris, UploadPostImagesListener listener) {
        List<String> imageUrls = new ArrayList<>();
        uploadNextPostImage(context, imageUris, 0, imageUrls, listener);
    }

    private static void uploadNextPostImage(Context context, List<Uri> imageUris, int index, List<String> imageUrls, UploadPostImagesListener listener) {
        if (index >= imageUris.size()) {
            new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(imageUrls));
            return;
        }

        Uri imageUri = imageUris.get(index);
        executorService.execute(() -> {
            File tempFile = null;
            try {
                tempFile = uriToFile(context, imageUri);
                Cloudinary cloudinary = CloudinaryConfig.getInstance();
                Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", "post"
                ));
                String imageUrl = uploadResult.get("secure_url").toString();
                imageUrls.add(imageUrl);

                uploadNextPostImage(context, imageUris, index + 1, imageUrls, listener);

            } catch (Exception e) {
                Log.e(TAG, "Error uploading post image at index " + index, e);
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure(e, index));
            } finally {
                cleanupTempFile(tempFile);
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

    private static void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                Log.w(TAG, "Failed to delete temporary file: " + tempFile.getAbsolutePath());
            }
        }
    }
    public static void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "ExecutorService shut down");
        }
    }

    // Listener for single user image upload
    public interface UploadImageResultListener {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    // Listener for multiple post images upload
    public interface UploadPostImagesListener {
        void onSuccess(List<String> imageUrls);
        void onFailure(Exception e, int failedIndex);
    }
}
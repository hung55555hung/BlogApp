package com.bugbug.blogapp.Config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryConfig {
    private static final String CLOUD_NAME = "dl61uqpx8";
    private static final String API_KEY = "283661958819713";
    private static final String API_SECRET = "u149O7LzOtTvsd65FqBhVo0oP6Q";
    private static Cloudinary cloudinary;
    public static Cloudinary getInstance(){
        if(cloudinary == null){
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", CLOUD_NAME,
                    "api_key", API_KEY,
                    "api_secret", API_SECRET
            ));
        }
        return cloudinary;
    }
}

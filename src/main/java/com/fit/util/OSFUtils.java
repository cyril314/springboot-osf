package com.fit.util;


import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OSFUtils {
    private static final String AK = "1mAeoCNoX25n_QiPGK-aS8895kQ4RedWWYb6LCpK";
    private static final String SK = "kJBUkzruYDjmnmx8UDsjMHD2OEw5SzTi36WP2BD4";
    private static final String bucket = "osfimg";

    public static void delPhotoInBucket(String key) {
        //client.deleteObject(bucket, key);
        try {
            BucketManager bucketManager = new BucketManager(Auth.create(AK, SK));
            bucketManager.delete(bucket, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getUpToken() {
        return Auth.create(AK, SK).uploadToken(bucket, null, 3600, new StringMap().putNotEmpty("returnBody", "{\"key\": $(key), \"hash\": $(etag), \"width\": $(imageInfo.width), \"height\": $(imageInfo.height)}"));
    }

    public static String uploadPhoto(byte[] img, String key) {
        UploadManager uploadManager = new UploadManager();
        try {
            uploadManager.put(img, key, getUpToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;

//		ObjectMetadata meta = new ObjectMetadata();
//		try {
//			meta.setContentLength(img.available());
//
//			//上传到图片服务器
//			PutObjectResult result = client.putObject(bucket, key, img, meta);
//			return result.getETag();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
    }

    public static List<Integer> toList(Set<Integer> set) {
        if (set == null) {
            return new ArrayList<Integer>();
        }

        List<Integer> list = new ArrayList<Integer>();
        for (Integer ele : set) {
            list.add(ele);
        }
        return list;
    }

    public static boolean validateEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

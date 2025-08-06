package com.example.nexusdoc.ui.chat.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static String getFileName(Context context, Uri uri) {
        String fileName = "fichier";
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return fileName;
    }

    public static long getFileSize(Context context, Uri uri) {
        long fileSize = 0;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0) {
                        fileSize = cursor.getLong(sizeIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return fileSize;
    }

    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.exists() ? file.length() : 0;
    }

    public static String fileToBase64(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        byte[] fileBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(fileBytes, Base64.DEFAULT);
    }

    public static String uriToBase64(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            byte[] fileBytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(fileBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String base64ToTempFile(Context context, String base64Data, String fileName) {
        try {
            byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
            File tempFile = new File(context.getCacheDir(), fileName);

            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            fileOutputStream.write(decodedBytes);
            fileOutputStream.close();

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
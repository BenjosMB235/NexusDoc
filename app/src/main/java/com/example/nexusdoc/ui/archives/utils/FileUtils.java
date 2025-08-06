package com.example.nexusdoc.ui.archives.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class FileUtils {

    public static class FileInfo {
        public String name;
        public String mimeType;
        public long size;

        public FileInfo(String name, String mimeType, long size) {
            this.name = name;
            this.mimeType = mimeType;
            this.size = size;
        }
    }

    public static FileInfo getFileInfo(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();

        String name = getFileName(context, uri);
        String mimeType = getMimeType(context, uri);
        long size = getFileSize(context, uri);

        if (name == null || mimeType == null || size <= 0) {
            return null;
        }

        return new FileInfo(name, mimeType, size);
    }

    public static String getFileName(Context context, Uri uri) {
        String fileName = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        }

        if (fileName == null) {
            fileName = uri.getPath();
            if (fileName != null) {
                int cut = fileName.lastIndexOf('/');
                if (cut != -1) {
                    fileName = fileName.substring(cut + 1);
                }
            }
        }

        return fileName != null ? fileName : "document_" + System.currentTimeMillis();
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);

        if (mimeType == null) {
            String fileName = getFileName(context, uri);
            if (fileName != null) {
                String extension = getFileExtension(fileName);
                if (extension != null) {
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }
            }
        }

        return mimeType;
    }

    public static long getFileSize(Context context, Uri uri) {
        long size = 0;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            }
        }

        return size;
    }

    public static byte[] readFileBytes(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            if (inputStream == null) return null;

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();

        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) return null;

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return null;
        }

        return fileName.substring(lastDot + 1).toLowerCase(Locale.getDefault());
    }

    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format(Locale.getDefault(), "%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static boolean isImageFile(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public static boolean isPdfFile(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    public static boolean isTextFile(String mimeType) {
        return mimeType != null && (mimeType.startsWith("text/") ||
                mimeType.equals("application/json") ||
                mimeType.equals("application/xml"));
    }
}
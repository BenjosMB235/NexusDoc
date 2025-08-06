package com.example.nexusdoc.ui.archives.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageProcessor {
    private static final String TAG = "ImageProcessor";

    private final Context context;

    public enum Quality {
        LOW(60, 1024),
        MEDIUM(80, 1920),
        HIGH(95, 2560);

        public final int compressionQuality;
        public final int maxDimension;

        Quality(int compressionQuality, int maxDimension) {
            this.compressionQuality = compressionQuality;
            this.maxDimension = maxDimension;
        }
    }

    public static class ProcessedImage {
        public byte[] imageData;
        public byte[] thumbnailData;
        public int width;
        public int height;
        public long originalSize;
        public long processedSize;

        public ProcessedImage(byte[] imageData, byte[] thumbnailData,
                              int width, int height, long originalSize) {
            this.imageData = imageData;
            this.thumbnailData = thumbnailData;
            this.width = width;
            this.height = height;
            this.originalSize = originalSize;
            this.processedSize = imageData.length;
        }
    }

    public ImageProcessor(Context context) {
        this.context = context;
    }

    public ProcessedImage processImage(Uri imageUri, Quality quality) throws IOException {
        // Lire l'image originale
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Impossible d'ouvrir l'image");
        }

        // Obtenir les informations EXIF pour la rotation
        ExifInterface exif = new ExifInterface(inputStream);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        inputStream.close();

        // Lire l'image en bitmap
        inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (originalBitmap == null) {
            throw new IOException("Impossible de décoder l'image");
        }

        long originalSize = originalBitmap.getByteCount();

        try {
            // Corriger l'orientation
            Bitmap rotatedBitmap = rotateImageIfRequired(originalBitmap, orientation);

            // Redimensionner si nécessaire
            Bitmap resizedBitmap = resizeImageIfNeeded(rotatedBitmap, quality.maxDimension);

            // Compresser l'image principale
            ByteArrayOutputStream mainStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality.compressionQuality, mainStream);
            byte[] imageData = mainStream.toByteArray();
            mainStream.close();

            // Créer la miniature
            Bitmap thumbnail = createThumbnail(resizedBitmap, 300);
            ByteArrayOutputStream thumbStream = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 75, thumbStream);
            byte[] thumbnailData = thumbStream.toByteArray();
            thumbStream.close();

            // Nettoyer les bitmaps
            if (rotatedBitmap != originalBitmap) rotatedBitmap.recycle();
            if (resizedBitmap != rotatedBitmap) resizedBitmap.recycle();
            thumbnail.recycle();
            originalBitmap.recycle();

            return new ProcessedImage(
                    imageData, thumbnailData,
                    resizedBitmap.getWidth(), resizedBitmap.getHeight(),
                    originalSize
            );

        } catch (Exception e) {
            originalBitmap.recycle();
            throw new IOException("Erreur traitement image: " + e.getMessage(), e);
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            default:
                return bitmap; // Pas de rotation nécessaire
        }

        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }

            return rotatedBitmap;
        } catch (OutOfMemoryError e) {
            Log.w(TAG, "Pas assez de mémoire pour la rotation, image conservée telle quelle");
            return bitmap;
        }
    }

    private Bitmap resizeImageIfNeeded(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap; // Pas de redimensionnement nécessaire
        }

        float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            if (resizedBitmap != bitmap) {
                bitmap.recycle();
            }

            return resizedBitmap;
        } catch (OutOfMemoryError e) {
            Log.w(TAG, "Pas assez de mémoire pour le redimensionnement, image conservée telle quelle");
            return bitmap;
        }
    }

    private Bitmap createThumbnail(Bitmap bitmap, int thumbnailSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculer les dimensions pour un crop carré centré
        int cropSize = Math.min(width, height);
        int x = (width - cropSize) / 2;
        int y = (height - cropSize) / 2;

        try {
            // Crop carré
            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, cropSize, cropSize);

            // Redimensionner à la taille de miniature
            Bitmap thumbnail = Bitmap.createScaledBitmap(croppedBitmap, thumbnailSize, thumbnailSize, true);

            if (croppedBitmap != bitmap) {
                croppedBitmap.recycle();
            }

            return thumbnail;
        } catch (OutOfMemoryError e) {
            Log.w(TAG, "Pas assez de mémoire pour la miniature, création d'une miniature simple");
            return Bitmap.createScaledBitmap(bitmap, thumbnailSize, thumbnailSize, true);
        }
    }

    public ProcessedImage processImageData(byte[] imageData, Quality quality) throws IOException {
        // Convertir en bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        if (bitmap == null) {
            throw new IOException("Impossible de décoder l'image");
        }

        long originalSize = imageData.length;

        try {
            // Redimensionner si nécessaire
            Bitmap resizedBitmap = resizeImageIfNeeded(bitmap, quality.maxDimension);

            // Compresser l'image principale
            ByteArrayOutputStream mainStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality.compressionQuality, mainStream);
            byte[] processedImageData = mainStream.toByteArray();
            mainStream.close();

            // Créer la miniature
            Bitmap thumbnail = createThumbnail(resizedBitmap, 300);
            ByteArrayOutputStream thumbStream = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 75, thumbStream);
            byte[] thumbnailData = thumbStream.toByteArray();
            thumbStream.close();

            // Nettoyer les bitmaps
            if (resizedBitmap != bitmap) resizedBitmap.recycle();
            thumbnail.recycle();
            bitmap.recycle();

            return new ProcessedImage(
                    processedImageData, thumbnailData,
                    resizedBitmap.getWidth(), resizedBitmap.getHeight(),
                    originalSize
            );

        } catch (Exception e) {
            bitmap.recycle();
            throw new IOException("Erreur traitement image: " + e.getMessage(), e);
        }
    }

    public static double calculateCompressionRatio(ProcessedImage processed) {
        if (processed.originalSize == 0) return 0.0;
        return (double) processed.processedSize / processed.originalSize;
    }

    public static String getCompressionInfo(ProcessedImage processed) {
        double ratio = calculateCompressionRatio(processed);
        double savedPercent = (1.0 - ratio) * 100;

        return String.format("Compression: %.1f%% (%.1f KB → %.1f KB)",
                savedPercent,
                processed.originalSize / 1024.0,
                processed.processedSize / 1024.0);
    }
}
package rvictorino.dailypug.helper;


import android.graphics.Bitmap;

public class ImageCropHelper {

    /**
     * Returns a new properly cropped and centered bitmap
     * @param bitmapToCrop input image to crop
     * @param desiredHeight desired height to scale image
     * @param targetWidth target width after cropping
     * @return a new Bitmap image
     */
    public static Bitmap centerCropWallpaper(Bitmap bitmapToCrop, int desiredHeight, int targetWidth) {
        float scale = (float) desiredHeight / bitmapToCrop.getHeight();
        int scaledWidth = (int) (scale * bitmapToCrop.getWidth());

        int imageCenterWidth = scaledWidth /2;
        int widthToCut = imageCenterWidth - targetWidth / 2;
        int leftWidth = scaledWidth - widthToCut;
        Bitmap scaledWallpaper = Bitmap.createScaledBitmap(bitmapToCrop, scaledWidth, desiredHeight, false);
        return Bitmap.createBitmap(scaledWallpaper, widthToCut, 0, leftWidth, desiredHeight);
    }
}

package it.dk.libs.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import it.dk.libs.common.ILogger;
import it.dk.libs.common.Logger;

import java.io.*;

import static it.dk.libs.common.ContractHelper.checkNotNull;

/**
 */
public class MediaHelper {
    private static final String LOG_HASH = MediaHelper.class.getSimpleName();
    
    protected final ILogger mBaseLogFacility;
    protected final Context mContext;
    protected final DisplayMetrics displayMetrics ;
    
    //---------- Constructors
    public MediaHelper(Context context, ILogger logFacility) {
    	mContext = context;
		displayMetrics = mContext.getResources().getDisplayMetrics();
        mBaseLogFacility = checkNotNull(logFacility, Logger.class.getSimpleName());
    }

    /**
     * Resizes an image file and save the result to a file
     * 
     * @param inputFilePath
     * @param outputFilePath
     * @param newWidth
     * @param newHeight
     * 
     * @return true if the operation succeeded, otherwise false
     */
    public boolean resizeImage(
            String inputFilePath,
            String outputFilePath,
            int newWidth,
            int newHeight)
    {
        return resizeImage(new File(inputFilePath), new File(outputFilePath), newWidth, newHeight);
    }
    /**
     * Resizes an image file and save the result to a file. if new width or
     * height is 0, destination image is not resized at all, but is a copy
     * of source image.
     * 
     * TODO check image rotation in exif data
     * 
     * @param sourceFile
     * @param destinationFile
     * @param newWidth
     * @param newHeight
     * 
     * @return true if the operation succeeded, otherwise false
     */
    public boolean resizeImage(
            File sourceFile,
            File destinationFile,
            int newWidth,
            int newHeight)
    {
        mBaseLogFacility.v(LOG_HASH, "Resizing image " + sourceFile + " to destionation " + destinationFile +
                " with new size " + newWidth + "x" + newHeight);

        if (null == sourceFile || !sourceFile.exists()) {
            mBaseLogFacility.i(LOG_HASH, "Source file doesn't exist");
            return false;
        }
        if (null == destinationFile || TextUtils.isEmpty(destinationFile.getAbsolutePath())) {
            mBaseLogFacility.v(LOG_HASH, "Cannot save image, null destination file");
            return false;
        }
        
        //destination file will have same size than original image
        if (0 == newWidth || 0 == newHeight) {
            //simply copy source to destination
            try {
                StreamHelper.copyFile(sourceFile, destinationFile);
                return true;
            } catch (IOException e) {
                mBaseLogFacility.e(LOG_HASH,
                        "Cannot copy source file " + sourceFile.getAbsolutePath() + " to destination file " + destinationFile.getAbsolutePath(),
                        e);
                return false;
            }
        }
        
        Bitmap bmp = null;
        InputStream fis = null;
        String sourceFilePath = sourceFile.getAbsolutePath(); 
        try {
            // get the size of the bitmap
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            opts.inSampleSize = 1;
            bmp = BitmapFactory.decodeFile(sourceFilePath, opts);

            // Scaling by a power of two is more efficient, but may lead to unreasonable roundings
            int originalWidth = opts.outWidth;
            int originalHeight = opts.outHeight;
            //TODO find right scaling also when the size of image is increased, not only decreased
            float xScale = (float)newWidth / (float)originalWidth;
            float yScale = (float)newHeight / (float)originalHeight;
            //preserves aspect ratio
            float scale = Math.min(xScale, yScale);
            
            opts.outWidth = (int)((float)originalWidth * scale);
            opts.outHeight = (int)((float)originalHeight * scale);
            
//            // If the image is big enough we resample it to reduce memory consumption at the expense of quality
//            if (originalWidth > 200 || originalHeight > 200) {
//                // Found the closer power of two to sample
//                double sampleSize = 1.0f/scale;
//                double log2 = Math.log(sampleSize)/Math.log(2);
//                sampleSize = Math.pow(2.0, (double)((int)log2));
//                opts.inSampleSize = (int)sampleSize;
//            } else {
                opts.inSampleSize = (int) (1.0f/scale);
//            }
            
            // get the image for real now
            fis = new FileInputStream(sourceFile);
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inPurgeable = true;
            bmp = BitmapFactory.decodeStream(fis, null, opts);
            mBaseLogFacility.i(LOG_HASH, "Real new final size of resized bitmap: " + bmp.getWidth() + "x" + bmp.getHeight());
            boolean res = saveToFile(bmp, destinationFile);
            //allow this bitmap to be recycled
            if (null != bmp) bmp.recycle();
            return res;
            
        } catch (IOException e) {
            mBaseLogFacility.e(LOG_HASH, "Cannot resize image", e);
            return false;
            
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    mBaseLogFacility.e(LOG_HASH, "Cannot close input stream", e);
                }
            }
        }
    }
    
    /**
     * Resize a bitmap
     * 
     * @param sourceBitmap
     * @param newWidth
     * @param newHeight
     * 
     * @return the resized bitmap or null if the operation had issues
     */
    public Bitmap resizeImage(
            Bitmap sourceBitmap,
            int newWidth,
            int newHeight)
    {
        if (null == sourceBitmap) {
            mBaseLogFacility.i(LOG_HASH, "Cannot resize a null bitmap");
            return null;
        }
        if (0 == newWidth) {
            mBaseLogFacility.i(LOG_HASH, "Wrong new width");
            return null;
        }
        if (0 == newHeight) {
            mBaseLogFacility.i(LOG_HASH, "Wrong new height");
            return null;
        }
        
        int originalWidth = sourceBitmap.getWidth();
        int originalHeight = sourceBitmap.getHeight();
        
        float xScale = (float)newWidth / (float)originalWidth;
        float yScale = (float)newHeight / (float)originalHeight;
        //preserves aspect ratio
        float scale = Math.max(xScale, yScale);
        
        //creates a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scale, scale);
        
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(
                sourceBitmap,
                0, 0,
                originalWidth, originalHeight,
                matrix,
                true);
        
        return resizedBitmap;
    }
    
    /**
     * Resize a bitmap to a given file
     * 
     * @param sourceBitmap
     * @param outputFilePath
     * @param newWidth
     * @param newHeight
     * @return true if the operation succeeded, otherwise false
     */
    public boolean resizeImage(
            Bitmap sourceBitmap,
            String outputFilePath,
            int newWidth,
            int newHeight)
    {
        return resizeImage(sourceBitmap, new File(outputFilePath), newWidth, newHeight);
    }
    /**
     * Resize a bitmap to a given file
     * 
     * @param sourceBitmap
     * @param destinationFile
     * @param newWidth
     * @param newHeight
     * @return true if the operation succeeded, otherwise false
     */
    public boolean resizeImage(
            Bitmap sourceBitmap,
            File destinationFile,
            int newWidth,
            int newHeight)
    {
        if (null == destinationFile || TextUtils.isEmpty(destinationFile.getAbsolutePath())) {
            mBaseLogFacility.v(LOG_HASH, "Cannot save image, null destination file");
            return false;
        }
        Bitmap resizedBitmap = resizeImage(sourceBitmap, newWidth, newHeight);
        boolean res = saveToFile(resizedBitmap, destinationFile);
        //allow this bitmap to be recycled
        if (null != resizedBitmap) resizedBitmap.recycle();
        return res;
    }

    
    /**
     * Persists a bitmap to a file
     * 
     * @param bitmapToSave
     * @param destionationFilePath
     * 
     * @return true if the operation succeeded, otherwise false
     */
    public boolean saveToFile(
            Bitmap bitmapToSave,
            String destionationFilePath)
    {
        return saveToFile(bitmapToSave, new File(destionationFilePath));
    }
    /**
     * Persists a bitmap to a file
     * 
     * @param bitmapToSave
     * @param destinationFile
     * 
     * @return true if the operation succeeded, otherwise false
     */
    public boolean saveToFile(
            Bitmap bitmapToSave,
            File destinationFile)
    {
        mBaseLogFacility.v(LOG_HASH, "saveToFile: " + destinationFile);
        if (null == bitmapToSave) {
            mBaseLogFacility.v(LOG_HASH, "Cannot save image, null source bitmap");
            return false;
        }
        if (null == destinationFile || TextUtils.isEmpty(destinationFile.getAbsolutePath())) {
            mBaseLogFacility.v(LOG_HASH, "Cannot save image, null destination file");
            return false;
        }
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(destinationFile);
            bitmapToSave.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return true;
        } catch (IOException e) {
            mBaseLogFacility.e(LOG_HASH, "Error saving image to file " + destinationFile, e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    mBaseLogFacility.e(LOG_HASH, "Cannot close stream", e);
                }
            }
        }
    }
    
    
    /**
     * Handles an image returned from the new image shoot with the camera.
     * Sometimes the image returned is a file, so scale it and save to given
     * file. Other times is a bitmap inside the intent, so scale and save it
     * to a given file. 
     * 
     * @param intent
     * @param sourceFile
     * @param destinationFile
     * @param newWidth
     * @param newHeight
     * @return true if the operation succeeded, otherwise false
     */
    public boolean handleReturnedNewPhoto(
            Intent intent,
            File sourceFile,
            File destinationFile,
            int newWidth,
            int newHeight)
    {
        //image is in sourceFile
        if (sourceFile != null) {
            if (!sourceFile.exists()) {
                mBaseLogFacility.e(LOG_HASH, "Source file for new camera image doesn't exists: " + sourceFile.getAbsolutePath());
                return false;
            }
            mBaseLogFacility.v(LOG_HASH, "Full image is returned, path is " + sourceFile.getAbsolutePath());
            return resizeImage(sourceFile, destinationFile, newWidth, newHeight);

        //fallback on intent
        } else if (intent != null) {
            //Android default behavior
            if (intent.hasExtra("data")) {
                mBaseLogFacility.v(LOG_HASH, "Only the thumbnail is returned");
                Bitmap sourceBitmap = (Bitmap) intent.getExtras().get("data");
                if (null != sourceBitmap) {
                    return resizeImage(sourceBitmap, destinationFile, newWidth, newHeight);
                } else {
                    mBaseLogFacility.e(LOG_HASH, "Custom rom?!?!?, No way to get data from intent extra data");
                    return false;
                }
                
            //some custom ROMS handles thumbnail in that way (MIUI, for example)
            } else if (!TextUtils.isEmpty(intent.getDataString())) {
                String sourceFileName = intent.getDataString();
                sourceFileName = sourceFileName.replaceAll("file://", "");
                File newSourceFile = new File(sourceFileName);
                if (newSourceFile.exists()) {
                    return resizeImage(new File(sourceFileName), destinationFile, newWidth, newHeight);
                } else {
                    mBaseLogFacility.e(LOG_HASH, "Custom rom?!?!?, No way to get data from " + sourceFileName);
                    return false;
                }
            } else {
                mBaseLogFacility.e(LOG_HASH, "Custom rom?!?!?, No way to get file location");
                return false;
            }
        
        //no way to manage the resize
        } else {
            mBaseLogFacility.e(LOG_HASH, "No source file specified, and intent data is null");
            return false;
        }
    }

    /**
     * Returns a bitmap from the new image shoot with the camera
     * 
     * @param intent
     * @param sourceFile
     * @param destinationFile
     * @param newWidth
     * @param newHeight
     * @return true if the operation succeeded, otherwise false
     */
    public boolean handleReturnedPickFromGallery(
            Context context,
            Intent intent,
            File destinationFile,
            int newWidth,
            int newHeight)
    {
        if (null == intent) {
            mBaseLogFacility.i(LOG_HASH, "Intent data is null");
            return false;
        }

        //get the image from the gallery
        Uri pickedImageURI = intent.getData();
        if (null == pickedImageURI) {
            mBaseLogFacility.e(LOG_HASH, "Cannot obtain image file path from a null uri");
            return false;    
        }
        String sourceFilePath = getFilePathFromURI(context, pickedImageURI);
        if (TextUtils.isEmpty(sourceFilePath)) {
            mBaseLogFacility.e(LOG_HASH, "Cannot obtain image file path from uri " + pickedImageURI);
            return false;    
        }
        
        //and scale it to given size
        return resizeImage(
                new File(sourceFilePath),
                destinationFile,
                newWidth,
                newHeight);
    }
    
    /**
     * Get the file full path from a URI result of an image selection activity
     * 
     * @param callerActivity
     * @param contentUri
     * @return
     */
    public String getFilePathFromURI(Context context, Uri contentUri)
    {
        if (null == context || null == contentUri) return null;
        
        if (contentUri.toString().startsWith("file://")) {
            String filePath = contentUri.toString();
            return filePath.replace("file://", ""); 
        }
        Cursor cursor = null;
        try {
            //http://www.androidsnippets.org/snippets/130/
            String [] proj={MediaStore.Images.Media.DATA};
            ContentResolver cr = context.getContentResolver();
            cursor = cr.query(
                    contentUri,
                    proj, // Which columns to return
                    null,       // WHERE clause; which rows to return (all rows)
                    null,       // WHERE clause selection arguments (none)
                    null); // Order-by clause (ascending by name)
            if (null == cursor) return null;
            cursor.moveToFirst();  
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            return filePath;
        } catch (Exception e) {
            mBaseLogFacility.e(LOG_HASH, "Error getting file path for uri " + contentUri, e);
            return null;
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
    }    
    
    public int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public int getPixelFromDP(int dp)
	{
		return (int)((dp * displayMetrics.density) + 0.5);
	}

	public int getDpFromPixel(int px)
	{
		return (int) ((px/displayMetrics.density)+0.5);
	}

}

package it.dk.libs.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import it.dk.libs.common.ILogger;
import it.dk.libs.common.ResultOperation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static it.dk.libs.common.ContractHelper.checkNotNull;
import static it.dk.libs.common.ContractHelper.checkNotNullOrEmpty;

/**
 *
 */
public class ImageMediaHelper {
	
	//---------- Private fields
	protected final ILogger mBaseLogFacility;

	
	
	//---------- Constructors
	public ImageMediaHelper(ILogger logFacility) {
		mBaseLogFacility = checkNotNull(logFacility, "Log Facility");
	}

	/**
	 * Save a source bitmap to a destination file
	 * 
	 * @param context
	 * @param sourceBitmap
	 * @param destFileName if no path separators are present, the file is created
	 *                     in the private storage space of the application, elsewhere
	 *                     the file is created in the path specified
	 * @param format
	 * @param quality
	 * @return
	 */
	public ResultOperation<String> saveImage(
			Context context,
			Bitmap sourceBitmap,
			String destFileName,
			Bitmap.CompressFormat format,
			int quality
		)
	{
		checkNotNull(context, "Context");
		checkNotNull(sourceBitmap, "Bitmap");
		checkNotNullOrEmpty(destFileName, "Output File Name");
		checkNotNull(format, "Bitmap Compress Format");
		
		ResultOperation<String> res = new ResultOperation<String>();
		String finalFullFileName;

	    //save the bitmap in the given format
		//save new image to a file, in the given format
	    FileOutputStream fos = null;
		try {
			//choose between private file or file in another location
			if (destFileName.contains(File.separator)) {
				File file = new File(destFileName);
				fos = new FileOutputStream(file);
				finalFullFileName = file.getAbsolutePath();
			} else {
				fos = context.openFileOutput(destFileName, Context.MODE_WORLD_READABLE);
				finalFullFileName = context.getFilesDir().getAbsolutePath() + File.separator + destFileName;
			}
			sourceBitmap.compress(format, quality, fos);
		} catch (FileNotFoundException e) {
			res.setException(e, ResultOperation.RETURNCODE_ERROR_APPLICATION_ARCHITECTURE);
			return res;
		} finally {
			//TODO remember to put the recycle if the bitmap was created ad-hoc
			//sourceBitmap.recycle();
			if (null != fos)
				try {
					fos.close();
				} catch (IOException e) {
					res.setException(e, ResultOperation.RETURNCODE_ERROR_APPLICATION_ARCHITECTURE);
					return res;
				}
		}
	    
		res.setResult(finalFullFileName);
	    return res;
	}
	
	
	/**
	 * Examines an image and find if its orientation is landscape 
	 * @param filePath the path of the image
	 * @return
	 */
	public ResultOperation<Boolean> isImageLandscape(
			String filePath) {
		BitmapFactory.Options options = new Options();
		//inJustDecodeBounds
		//If set to true, the decoder will return null (no bitmap),
		//but the out... fields will still be set, allowing the 
		//caller to query the bitmap without having to allocate
		//the memory for its pixels.
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		
		//at this point, i have width and size of the image
		boolean isLandscape = options.outWidth >= options.outHeight;
		
		return new ResultOperation<Boolean>(isLandscape);
	}
	
	/**
	 * Examines an image and find if its orientation is landscape 
	 * @param filePath the path of the image
	 * @return
	 */
	public ResultOperation<Boolean> isImagePortrait(
			String filePath) {
		BitmapFactory.Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		
		//at this point, i have width and size of the image
		boolean isLandscape = options.outHeight >= options.outWidth;
		
		return new ResultOperation<Boolean>(isLandscape);
	}
	
	
	
	//---------- Private methods
	
}

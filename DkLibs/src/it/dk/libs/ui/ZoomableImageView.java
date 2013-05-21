package it.dk.libs.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import it.dk.libs.ui.MultiTouchController.MultiTouchObjectCanvas;
import it.dk.libs.ui.MultiTouchController.PointInfo;
import it.dk.libs.ui.MultiTouchController.PositionAndScale;

/**
 * View that show an image and allow to zoom and pan on it
 *
 */
public class ZoomableImageView
	extends View
	implements MultiTouchObjectCanvas<ZoomableImageView.Img>
{

	//---------- Private fields
	private MultiTouchController<ZoomableImageView.Img> multiTouchController;
	private Img mImgtoDisplay;

	private static final int UI_MODE_ROTATE = 1;
	private static final int UI_MODE_ANISOTROPIC_SCALE = 2;
    private int mUIMode = UI_MODE_ROTATE;
	
	

	//---------- Constructors
	public ZoomableImageView(Context context) {
		super(context);
		initVars(context);
	}

	/**
	 * Generally, this is the constructor called
	 */
    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
		initVars(context);
    }	
	
    public ZoomableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVars(context);
    }


	
	
	//---------- Public properties
	
	
	
	//---------- Events
    
    /**
     * Pass touch events to the MT controller
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	return multiTouchController.onTouchEvent(event);
    }

	
    @Override
    protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null != mImgtoDisplay) mImgtoDisplay.draw(canvas);
    }
	
	
	//---------- Public methods
    /**
     * Get the image that is under the single-touch point, or return null (canceling the drag op) if none
     */
	public Img getDraggableObjectAtPoint(PointInfo touchPoint) {
		return mImgtoDisplay;
	}

    /**
     * Get the current position and scale of the selected image.
     * Called whenever a drag starts or is reset.
     */
	public void getPositionAndScale(Img img, PositionAndScale objPosAndScaleOut) {
		objPosAndScaleOut.set(
				img.getCenterX(), img.getCenterY(),
				(mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0,
                (img.getScaleX() + img.getScaleY()) / 2,
                (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0,
                img.getScaleX(), img.getScaleY(),
                (mUIMode & UI_MODE_ROTATE) != 0,
                img.getAngle());
	}

    /**
     * Set the position and scale of the dragged/stretched image.ù
     */
	public boolean setPositionAndScale(Img obj, PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
		boolean ok = false;
		if (null != mImgtoDisplay)
			ok = mImgtoDisplay.setPos(newObjPosAndScale);
	    if (ok) invalidate();
	    return ok;
	}

	/**
     * Select an object for dragging.
     * Called whenever an object is found to be under the point (non-null is returned by getDraggableObjectAtPoint())
     * and a drag operation is starting.
     * Called with null when drag op ends.
     */
	public void selectObject(Img obj, PointInfo touchPoint) {
        invalidate();
	}
	
	
	/**
	 * Increment scale factor of the image
	 * @param increment
	 */
	public void incrementScale(float increment) {
		if (null != mImgtoDisplay) {
			mImgtoDisplay.incrementScaleFactor(increment);
			invalidate();
		}
	}
	
	
	/**
	 * Assign the image using a resource
	 * @param res
	 * @param resId
	 */
	public void assignImage(Resources res, int resId) {
		mImgtoDisplay = new Img(res, resId);
	}
	
	public void assignImage(Resources res, Bitmap bitmap) {
		mImgtoDisplay = new Img(res, bitmap);
	}

	
    /**
     * Called by activity's onPause() method to free memory used for loading the images
     */
    public void onPause() {
    	if (null != mImgtoDisplay) mImgtoDisplay.onPause();
    }

    /**
     * Called by activity's onResume() method to load the images
     */
    public void onResume(Resources res) {
    	if (null != mImgtoDisplay) mImgtoDisplay.onResume(res);
    }

	/**
	 * Initialize local variables
	 * @param context
	 */
	private void initVars(Context context) {
		//initialize only once
		if (null == multiTouchController) {
	        multiTouchController = new MultiTouchController<ZoomableImageView.Img>(this);
	        //default black background
	        setBackgroundColor(Color.BLACK);
		}
	}
	
	
	//---------- Private methods

	
	
	
	//---------- Private classe
	
	//Class from some of Luke Hutchison's sample code
	class Img {
		//---------- Private fields
		private static final float SCREEN_MARGIN = 100;

		private int resId;
		private Drawable drawable;
		private boolean firstLoad;
		private int width, height, displayWidth, displayHeight;
		private float centerX, centerY, scaleX, scaleY, angle;
		private float minX, maxX, minY, maxY;
		final boolean mCanDiscardResource; 

		
		
		//---------- Constructors
		public Img(Resources res, int resId) {
			this(res);
			this.resId = resId;
		}
		
		public Img(Resources res, Bitmap bitmap) {
			this(res);
			drawable = new BitmapDrawable(bitmap);
		}
		
		private Img(Resources res) {
			this.firstLoad = true;
			getMetrics(res);
			//cannot discard the resource when the app is in pause
			mCanDiscardResource = false;
		}

		
		//---------- Public properties
		public Drawable getDrawable() {
			return drawable;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public float getCenterX() {
			return centerX;
		}

		public float getCenterY() {
			return centerY;
		}

		public float getScaleX() {
			return scaleX;
		}

		public float getScaleY() {
			return scaleY;
		}

		public float getAngle() {
			return angle;
		}

		// FIXME: these need to be updated for rotation
		public float getMinX() {
			return minX;
		}

		public float getMaxX() {
			return maxX;
		}

		public float getMinY() {
			return minY;
		}

		public float getMaxY() {
			return maxY;
		}
		
		
		
		//---------- Public methods
		
		/** Called by activity's onResume() method to load the images */
		public void onResume(Resources res) {
			getMetrics(res);
			if (mCanDiscardResource) {
				this.drawable = res.getDrawable(resId);
			}
			this.width = drawable.getIntrinsicWidth();
			this.height = drawable.getIntrinsicHeight();
			float centerX, centerY, scaleX, scaleY;
			if (firstLoad) {
//				cx = SCREEN_MARGIN + (float) (Math.random() * (displayWidth - 2 * SCREEN_MARGIN));
//				cy = SCREEN_MARGIN + (float) (Math.random() * (displayHeight - 2 * SCREEN_MARGIN));
//				float sc = (float) (Math.max(displayWidth, displayHeight) / (float) Math.max(width, height) * Math.random() * 0.3 + 0.2);
				//float sc = 1;
				//center the image with max size available
				centerX = displayWidth / 2.0f;
				centerY = displayHeight / 2.0f;
				float scale = Math.min(displayWidth / (float) width, displayHeight / (float) height); 
				scaleX = scaleY = scale;
				firstLoad = false;
			} else {
				// Reuse position and scale information if it is available
				// FIXME this doesn't actually work because the whole activity is torn down and re-created on rotate
				centerX = this.centerX;
				centerY = this.centerY;
				scaleX = this.scaleX;
				scaleY = this.scaleY;
				// Make sure the image is not off the screen after a screen rotation
				if (this.maxX < SCREEN_MARGIN)
					centerX = SCREEN_MARGIN;
				else if (this.minX > displayWidth - SCREEN_MARGIN)
					centerX = displayWidth - SCREEN_MARGIN;
				if (this.maxY > SCREEN_MARGIN)
					centerY = SCREEN_MARGIN;
				else if (this.minY > displayHeight - SCREEN_MARGIN)
					centerY = displayHeight - SCREEN_MARGIN;
			}
			setPos(centerX, centerY, scaleX, scaleY, 0.0f);
		}

		/** Called by activity's onPause() method to free memory used for loading the images */
		public void onPause() {
			if (mCanDiscardResource) {
				this.drawable = null;
			}
		}

		/** Set the position and scale of an image in screen coordinates */
		public boolean setPos(PositionAndScale newImgPosAndScale) {
			return setPos(
					newImgPosAndScale.getXOff(),
					newImgPosAndScale.getYOff(),
					(mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0
						? newImgPosAndScale.getScaleX()
						: newImgPosAndScale.getScale(),
					(mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0
						? newImgPosAndScale.getScaleY()
						: newImgPosAndScale.getScale(),
					newImgPosAndScale.getAngle()
					);
			// FIXME: anisotropic scaling jumps when axis-snapping
			// FIXME: affine-ize
			// return setPos(newImgPosAndScale.getXOff(), newImgPosAndScale.getYOff(), newImgPosAndScale.getScaleAnisotropicX(),
			// newImgPosAndScale.getScaleAnisotropicY(), 0.0f);
		}

		/** Return whether or not the given screen coords are inside this image */
		public boolean containsPoint(float scrnX, float scrnY) {
			// FIXME: need to correctly account for image rotation
			return (scrnX >= minX && scrnX <= maxX && scrnY >= minY && scrnY <= maxY);
		}

		/**
		 * Draw the image
		 * @param canvas
		 */
		public void draw(Canvas canvas) {
			canvas.save();
			float dx = (maxX + minX) / 2;
			float dy = (maxY + minY) / 2;
			drawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
			canvas.translate(dx, dy);
			//fix image rotation
			//canvas.rotate(angle * 180.0f / (float) Math.PI);
			canvas.translate(-dx, -dy);
			drawable.draw(canvas);
			canvas.restore();
		}

		/**
		 * Add an increment to the scale scale factor of the image.
		 * If the increment is negative, the image will be decreased 
		 * @param increment
		 */
		public void incrementScaleFactor(float increment) {
			float newScaleX = this.scaleX + increment > 0 ? this.scaleX + increment : this.scaleX;
			float newScaleY = this.scaleY + increment > 0 ? this.scaleY + increment : this.scaleY;
			setPos(this.centerX, this.centerY, newScaleX, newScaleY, this.angle);
		}
		
		
		//---------- Private methods
		
		/** Set the position and scale of an image in screen coordinates */
		private boolean setPos(float centerX, float centerY, float scaleX, float scaleY, float angle) {
			float ws = (width / 2) * scaleX;
			float hs = (height / 2) * scaleY;
			float newMinX = centerX - ws;
			float newMinY = centerY - hs;
			float newMaxX = centerX + ws;
			float newMaxY = centerY + hs;
			if (newMinX > displayWidth - SCREEN_MARGIN 
					|| newMaxX < SCREEN_MARGIN 
					|| newMinY > displayHeight - SCREEN_MARGIN
					|| newMaxY < SCREEN_MARGIN)
				return false;
			this.centerX = centerX;
			this.centerY = centerY;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.angle = angle;
			this.minX = newMinX;
			this.minY = newMinY;
			this.maxX = newMaxX;
			this.maxY = newMaxY;
			return true;
		}

		private void getMetrics(Resources res) {
			DisplayMetrics metrics = res.getDisplayMetrics();
			// The DisplayMetrics don't seem to always be updated on screen rotate, so we hard code a portrait
			// screen orientation for the non-rotated screen here...
			// this.displayWidth = metrics.widthPixels;
			// this.displayHeight = metrics.heightPixels;
			this.displayWidth =
				res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
						? Math.max(metrics.widthPixels, metrics.heightPixels)
						: Math.min(metrics.widthPixels, metrics.heightPixels);
			this.displayHeight = 
				res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE 
						? Math.min(metrics.widthPixels, metrics.heightPixels)
						: Math.max(metrics.widthPixels, metrics.heightPixels);
		}
	}

}

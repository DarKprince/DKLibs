package it.dk.libs.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * Helper view used to draw a balloon help on the screen
 *
 */
public class BalloonHelpView  extends FrameLayout {
    public static final int PREFERRED_POSITION_ABOVE = 0;
    public static final int PREFERRED_POSITION_BELOW = 1;
    
    private BalloonHelpCoreView balloonHelpCoreView;
    
    public BalloonHelpView(Context context) {
        super(context);
        initialize(context);
    }

    public BalloonHelpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public BalloonHelpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }
    

    // ---------------------------------------------------- TextView properties
    public void setTextSize(float size) {
        balloonHelpCoreView.setTextSize(size);
    }
    
    public void setTextSize(int unit, float size) {
        balloonHelpCoreView.setTextSize(unit, size);
    }
    
    public void setTextColor(ColorStateList colors) {
        balloonHelpCoreView.setTextColor(colors);
    }
    
    public void setTextColor(int color) {
        balloonHelpCoreView.setTextColor(color);
    }
    
    public void setText(CharSequence text, BufferType type) {
        balloonHelpCoreView.setText(text, type);
    }
    
    public void setTextAppearance(Context context, int resid) {
        balloonHelpCoreView.setTextAppearance(context, resid);
    }
    
    public void setTextScaleX(float size) {
        balloonHelpCoreView.setTextScaleX(size);
    }
    // ------------------------------------------------------------------------
    
    
    public void show(int x, int y, int balloonTextId) {
        show(x, y, balloonTextId, PREFERRED_POSITION_ABOVE);
    }
    
    public void show(int x, int y, String balloonText) {
        show(x, y, balloonText, PREFERRED_POSITION_ABOVE);
    }

    public void show(int x, int y, int balloonTextId, int preferredDirection) {
        show(x, y, getContext().getString(balloonTextId), preferredDirection);
    }
    
    public void show(int x, int y, String balloonText, int preferredDirection) {
        this.setVisibility(View.VISIBLE);
        balloonHelpCoreView.setBubbleMessage(x, y, balloonText, preferredDirection);
    }

    public void hide() {
        this.setVisibility(View.GONE);
    }
    
    
    private void initialize(Context context) {
        //setBackgroundColor(Color.argb(0x88, 0, 0, 0));
        
        //creates balloon view
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        balloonHelpCoreView = new BalloonHelpCoreView(context);
        balloonHelpCoreView.setLayoutParams(frameParams);
        
        //adds view
        addView(balloonHelpCoreView);
    }
    
    
    /**
     * This class is the real balloon with a text inside
     */
    public class BalloonHelpCoreView extends TextView {
        
        private static final int ARROW_WIDTH_DP = 15;
        private static final int ARROW_HEIGHT_DP = 20;
        private static final float BALLOON_MIN_MARGIN_DP = 10;
        private final static int BALLOON_PADDING_LEFTRIGHT_DP = 10;
        private final static int BALLOON_PADDING_TOPBOTTOM_DP = 10;
        private final static int BALLOON_ROUNDED_CORNER_RADIUS_DP = 12;
        private final static int BALLOON_COLOR = Color.WHITE;
        
        private float mBalloonMarginPx;
        private float mArrowWidthPx;
        private float mArrowHeightPx;
        private float mBalloonLeftRightPaddindPx;
        private float mBalloonTopBottomPaddindPx;

        private ShapeDrawable mBalloonShape;
        private ShapeDrawable mArrowShape;
        private TextPaint mTextPaint;
        private StaticLayout mTextLayout;
        private int mBalloonX, mBalloonY;
        private int mArrowX, mArrowY;
        private int mBalloonWidth, mBallonHeight;
        
        private boolean mNeedRecalculation = true;
        private int mX, mY;
        private String mBallonText;
        private int mPreferredDirection;
        

        public BalloonHelpCoreView(Context context) {
            super(context);
        }
        
        public BalloonHelpCoreView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        
        public BalloonHelpCoreView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
        
        
        
        @Override
        protected void onDraw(Canvas canvas) {
            if (mNeedRecalculation) {
                setShapesPositionAndSize(mX, mY, mBallonText, mPreferredDirection);
            }
            if (null == mTextLayout) return;
            
            //draws the balloon
            mBalloonShape.setBounds(
                    mBalloonX,
                    mBalloonY,
                    mBalloonWidth + mBalloonX,
                    mBallonHeight + mBalloonY);
            mBalloonShape.draw(canvas);

            //draws the arraw
            mArrowShape.setBounds(
                    mArrowX,
                    mArrowY,
                    (int) (mArrowX + mArrowWidthPx),
                    (int) (mArrowY + mArrowHeightPx));
            mArrowShape.draw(canvas);

            //draws text inside rectangle
            canvas.save();
            canvas.translate(mBalloonX + mBalloonLeftRightPaddindPx, mBalloonY + mBalloonTopBottomPaddindPx);
            mTextLayout.draw(canvas);
            canvas.restore();
        }

        private float convertToPixel(float dip) {
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
            return px;
        }
        
        /**
         * 
         * @param x
         * @param y
         * @param textMessage
         * @param preferredDirection 
         */
        public void setBubbleMessage(int x, int y, String textMessage, int preferredDirection) {
            mNeedRecalculation = true;
            mX = x;
            mY = y;
            mBallonText = textMessage;
            mPreferredDirection = preferredDirection;
        }

        /**
         * 
         * @param x
         * @param y
         * @param textMessage
         * @param preferredDirection
         */
        private void setShapesPositionAndSize(int x, int y, String textMessage, int preferredDirection) {
            mBalloonMarginPx = convertToPixel(BALLOON_MIN_MARGIN_DP);
            mArrowWidthPx = convertToPixel(ARROW_WIDTH_DP);
            mArrowHeightPx = convertToPixel(ARROW_HEIGHT_DP);
            mBalloonTopBottomPaddindPx = convertToPixel(BALLOON_PADDING_TOPBOTTOM_DP);
            mBalloonLeftRightPaddindPx = convertToPixel(BALLOON_PADDING_LEFTRIGHT_DP);
            
            //creates shape for the ballon
            float roundedCornerRadiusPixel = convertToPixel(BALLOON_ROUNDED_CORNER_RADIUS_DP); 
            RoundRectShape roundedRect = new RoundRectShape(
                    new float[] { roundedCornerRadiusPixel, roundedCornerRadiusPixel, 
                                  roundedCornerRadiusPixel, roundedCornerRadiusPixel, 
                                  roundedCornerRadiusPixel, roundedCornerRadiusPixel,
                                  roundedCornerRadiusPixel, roundedCornerRadiusPixel
                                },
                    null,
                    null
                    );
            mBalloonShape = new ShapeDrawable(roundedRect);
            mBalloonShape.getPaint().setColor(BALLOON_COLOR);
            
            setTextSizeAndPosition(x, y, textMessage);
            boolean below = setBalloonSizeAndPosition(
                    x,
                    y,
                    mTextLayout.getWidth(), mTextLayout.getHeight(),
                    preferredDirection);
            setArrowSizeAndPosition(x, y, below);
            mNeedRecalculation = false;
        }
        
        private void setTextSizeAndPosition(int x, int y, String textMessage) {
            mTextPaint = new TextPaint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(getTextColors().getDefaultColor());
            mTextPaint.setTextSize(getTextSize());
            mTextPaint.setTypeface(getTypeface());

            float trueTextWidth = 0;
            if (!TextUtils.isEmpty(textMessage)) {
                //finds maximux lenght of a text line. Takes into account multiple
                // lines of text
                String lines[] = textMessage.split("\\r?\\n");
                for (String line : lines) {
                    trueTextWidth = Math.max(trueTextWidth, mTextPaint.measureText(line));
                }
            }
            
            //max horizontal space available for text
            float maxAllowedTextWidth = getWidth() - (mBalloonMarginPx * 2) - (mBalloonLeftRightPaddindPx * 2); 
            //finds the right width to use for text inside balloon
            int finalTextWidth = (trueTextWidth <= maxAllowedTextWidth)
                    ? (int) trueTextWidth
                    : (int) maxAllowedTextWidth;

            //height of the layout is automatically calculated
            mTextLayout = new StaticLayout(
                    textMessage,
                    mTextPaint,
                    finalTextWidth,
                    Layout.Alignment.ALIGN_CENTER,
                    1.0f,
                    0.0f,
                    false);
        }
        
        
        /**
         * Find X and Y coordinates where balloon has to be drawn
         * @param x X coordinate where the ballon arrow must point to
         * @param y Y coordinate where the ballon arrow must point to
         * @param preferredDirection 
         */
        private boolean setBalloonSizeAndPosition(
                int x,
                int y,
                int textWidth,
                int textHeight,
                int preferredDirection)
        {
            //gets balloon's size based on text it contains 
            mBalloonWidth = (int) (textWidth + mBalloonLeftRightPaddindPx * 2); 
            mBallonHeight = (int) (textHeight + mBalloonTopBottomPaddindPx * 2);
            
            //optimal balloon x coord to stay in the middle of arrow x coord
            float optimalX = x - mBalloonWidth / 2;
            if (optimalX >= mBalloonMarginPx) {
                mBalloonX = (int) optimalX; //discards margins
            } else {
                mBalloonX = (int) mBalloonMarginPx;
            }
            
            //total balloon height
            float ballonTotalHeight = mBallonHeight + mArrowHeightPx;
            boolean below;
            
            //match preferred direction with available space
            if (preferredDirection == PREFERRED_POSITION_ABOVE) {
                //put the balloon above the desired position
                float optimalY = y - ballonTotalHeight;
                if (optimalY >= 0 ) {
                    //balloon above the specified point
                    below = false;
                    mBalloonY = (int) optimalY;
                } else {
                    //balloon below the specified point
                    below = true;
                    mBalloonY = (int) (y + mArrowHeightPx);
                }
                
            } else {
                float optimalY = y + mArrowHeightPx;
                if (optimalY + mBallonHeight <= getHeight()) {
                    //balloon below the specified point
                    below = true;
                    mBalloonY = (int) optimalY;
                } else {
                    //balloon above the specified point
                    below = false;
                    mBalloonY = (int) (y - ballonTotalHeight);
                }
            }
            
            
            return below;
        }

        /**
         * 
         * @param x
         * @param y
         * @param below
         */
        private void setArrowSizeAndPosition(int x, int y, boolean below) {
            //create shape for balloon's arrow
            Path path = new Path();
            path.moveTo(0, 0);

            if (below) {
                mArrowX = x;
                mArrowY = y;
                //triangle that points below
                path.lineTo(0, mArrowHeightPx);
                path.lineTo(mArrowWidthPx, mArrowHeightPx);
                
            } else {
                mArrowX = x;
                mArrowY = (int) (y - mArrowHeightPx);
                //triangle that points up
                path.lineTo(mArrowWidthPx, 0);
                path.lineTo(0, mArrowHeightPx);
            }

            path.close();
            mArrowShape = new ShapeDrawable(new PathShape(path, mArrowWidthPx, mArrowHeightPx));
            mArrowShape.getPaint().setColor(BALLOON_COLOR);
            
        }
    }
    //---------- Constructors

    //---------- Private fields

    //---------- Public properties

    //---------- Public methods

    //---------- Private methods

}

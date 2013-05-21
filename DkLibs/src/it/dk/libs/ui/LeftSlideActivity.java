package it.dk.libs.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import it.dk.libs.R;
import it.dk.libs.helper.GlobalHelper;
import it.dk.libs.logic.BaseActivity;

public class LeftSlideActivity extends BaseActivity implements AnimationListener{

	protected UIHelper mUI;
	private LeftSlideActivity me;
	private boolean menuLeftOut = false;
	public static final int ACTION_LEFT_BTN = 100;

	//views
	private View app, left_menu;
	private Animation anim;

	private AnimParams animParams;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_left_slide);
		mUI = new UIHelper(this, mHandler);
		animParams = new AnimParams();
	}

	@Override
	public void defineGUI() {
		//define views
		app = mUI.getView(R.id.app);
		left_menu = mUI.getView(R.id.left_menu);
		mUI.getView(R.id.BtnSlideLeft, ACTION_LEFT_BTN );
		//app.findViewById(R.id.BtnSlideLeft).setOnClickListener(this);
	}


	@Override
	public Handler createHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ACTION_LEFT_BTN:
					actionLeftMenu();
					break;
				}
			}
		};
	}

	static class AnimParams {
		int left, right, top, bottom;
		void init(int left, int top, int right, int bottom) {
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}
	}

	/**
	 * Open/close the left sliding menu
	 */
	private void actionLeftMenu() {
		int w = app.getMeasuredWidth();
		int h = app.getMeasuredHeight();
		int orientation = getResources().getConfiguration().orientation;
		int left = 0;
		if(orientation == Configuration.ORIENTATION_PORTRAIT){//portrait
			if(GlobalHelper.isTablet(mContext))
				left = (int) (app.getMeasuredWidth() * 0.40);
			else
				left = (int) (app.getMeasuredWidth() * 0.80);
		}//landscape
		else{
			if(GlobalHelper.isTablet(mContext))
				left = (int) (app.getMeasuredWidth() * 0.20);
			else
				left = (int) (app.getMeasuredWidth() * 0.40);
		}
		if (!menuLeftOut) {
			anim = new TranslateAnimation(0, left, 0, 0);
			left_menu.setVisibility(View.VISIBLE);
			animParams.init(left, 0, left + w, h);
			left_menu.getLayoutParams().width = left;
			anim.setDuration(500);
			anim.setAnimationListener(me);
			anim.setFillAfter(true);
			app.startAnimation(anim);
		} else {
			anim = new TranslateAnimation(0, -left, 0, 0);
			animParams.init(0, 0, w, h);
			left_menu.getLayoutParams().width = left;
			anim.setDuration(500);
			anim.setAnimationListener(me);
			anim.setFillAfter(true);
			app.startAnimation(anim);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		menuLeftOut = !menuLeftOut;
		if (!menuLeftOut) {
			left_menu.setVisibility(View.INVISIBLE);
		}
		layoutApp(menuLeftOut);
	}

	void layoutApp(boolean menuOut) {
		app.layout(animParams.left, animParams.top, animParams.right, animParams.bottom);
		app.clearAnimation();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public void onAnimationStart(Animation animation) {}

}

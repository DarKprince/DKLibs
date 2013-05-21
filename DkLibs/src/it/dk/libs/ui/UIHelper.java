package it.dk.libs.ui;

import android.app.Activity;
import android.os.Handler;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;

import java.lang.reflect.Method;

public class UIHelper{

	private Activity mAct;
	static Handler mHandler;

	public UIHelper(Activity act, Handler handl) {
		mAct = act;
		mHandler = handl;
	}

	/**
	 * Get a generic View casted with Inferenced type
	 * @param 0 is viewID
	 * @param 1 is action (optional)
	 */
	public <T extends View> T getView(Integer... params) {
		View containerView = mAct.getWindow().getDecorView();
		Integer action = null, viewId = params[0];
		if(params.length > 1){
			action = params[1];
			return bindView(containerView, viewId, action);
		}
		else
			return bindView(containerView, viewId);		
	}

	/**
	 * Get a generic View casted with Inferenced type
	 * @param viewId id of resource
	 * @param action method name to invoke for onClick
	 */
	public <T extends View> T getView(Activity caller, int viewId, String methodName){
		View containerView = mAct.getWindow().getDecorView();	
		if(methodName == null)
			return bindView(containerView, viewId);
		else {
			try {
				Method method = caller.getClass().getMethod(methodName);
				return bindView(containerView, viewId, caller, method);
			} catch (Exception e) {
				e.printStackTrace();
				return bindView(containerView, viewId);
			}
		}		
	}

	/**
	 * Get a TextView passing its text as string
	 * @param viewId
	 * @param message
	 * @return
	 */
	public TextView getTextView(int viewId, String message){
		View containerView = mAct.getWindow().getDecorView();
		TextView field = bindView(containerView, viewId);
		field.setText(message);
		return field;
	}
	
	/**
	 * Variante in cui gli si passa il container (utile per i badge)
	 * @param containerView
	 * @param viewId
	 * @param message
	 * @return
	 */
	public TextView getTextView(View containerView, int viewId, String message){
		TextView field = bindView(containerView, viewId);
		field.setText(message);
		return field;
	}

	/**
	 * Get a TextView passing its text resID 
	 * @param viewId
	 * @param messageID
	 * @return
	 */
	public TextView getTextView(int viewId, int messageID){
		View containerView = mAct.getWindow().getDecorView();
		TextView field = bindView(containerView, viewId);
		field.setText(mAct.getText(messageID));
		return field;
	}

	/**
	 * Get a TextView passing its html spanned text
	 * @param viewId
	 * @param htmlTxt
	 * @return
	 */
	public TextView getTextView(int viewId, Spanned htmlTxt){
		View containerView = mAct.getWindow().getDecorView();
		TextView field = bindView(containerView, viewId);
		field.setText(htmlTxt);
		return field;
	}


	/**
	 * Simply get a webview optionally passing the static content to load
	 * @param a
	 * @param viewId
	 * @param contentToLoad : array with -> baseUrl, data, mimeType, encoding, historyUrl
	 * @return
	 */
	public WebView getWebView(Activity a, int viewId, String [] contentToLoad){
		WebView view = new WebView(a.getApplicationContext());
		view = (WebView) a.findViewById(viewId);
		if(contentToLoad != null){
			view.loadDataWithBaseURL(contentToLoad[0], contentToLoad[1], contentToLoad[2], contentToLoad[3], contentToLoad[4]);
		}
		return view;
	}

	@SuppressWarnings("unchecked")
	private <T extends View> T bindView(View containerView, int viewId){
		// We find the view with the given Id
		View foundView = containerView.findViewById(viewId);
		// We return the View with the given cast
		return (T) foundView;
	}


	@SuppressWarnings("unchecked")
	private <T extends View> T bindView(View containerView, int viewId, final int action){
		// We find the view with the given Id
		View foundView = containerView.findViewById(viewId);
		//add click action
		foundView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(action);
			}
		});
		// We return the View with the given cast
		return (T) foundView;
	}


	@SuppressWarnings("unchecked")
	private <T extends View> T bindView(View containerView, int viewId, final Activity caller, final Method method) {
		// We find the view with the given Id
		View foundView = containerView.findViewById(viewId);
		//add click action
		foundView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
					method.invoke(caller, (Object[])null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// We return the View with the given cast
		return (T) foundView;		
	}

}
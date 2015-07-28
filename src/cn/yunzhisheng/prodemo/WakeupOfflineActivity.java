package cn.yunzhisheng.prodemo;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.yunzhisheng.common.USCError;
import cn.yunzhisheng.wakeup.basic.WakeUpRecognizer;
import cn.yunzhisheng.wakeup.basic.WakeUpRecognizerListener;

public class WakeupOfflineActivity extends Activity implements OnClickListener {
	private int AsrType = ASR_ONLINE_TYPE;
	public static final int ASR_ONLINE_TYPE = 0;
	public static final int ASR_OFFLINE_TYPE = 1;
	public static final int WAKEUP_OFFLINE_TYPE = 2;
	public static final int TTS_OFFLINE_TYPE = 3;

	private EditText mTextViewResult;
	private TextView mTextViewTip;
	private TextView mTextViewStatus;

	private WakeUpRecognizer mWakeUpRecognizer;
	private ImageView mLogoImageView;

	private LinearLayout status_panel;
	private ImageView function_button;
	private Dialog mfunctionDialog;
	private TextView type;

	// 唤醒震动提示
	private Vibrator mVibrator;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wakeup);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.status_bar_main);
		mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		mTextViewResult = (EditText) findViewById(R.id.textViewResult);
		mTextViewStatus = (TextView) findViewById(R.id.textViewStatus);
		mTextViewTip = (TextView) findViewById(R.id.textViewTip);
		status_panel = (LinearLayout) findViewById(R.id.status_panel);
		mLogoImageView = (ImageView) findViewById(R.id.logo_imageview);

		function_button = (ImageView) findViewById(R.id.function_button);
		function_button.setOnClickListener(this);
		status_panel.setVisibility(View.INVISIBLE);
		
		// 功能选择
		mfunctionDialog = new Dialog(this, R.style.dialog);
		mfunctionDialog.setContentView(R.layout.function_list_item);
		mfunctionDialog.findViewById(R.id.asr_online_text).setOnClickListener(this);
		mfunctionDialog.findViewById(R.id.asr_offline_text).setOnClickListener(this);
		mfunctionDialog.findViewById(R.id.wakeup_offline_text).setOnClickListener(this);
		mfunctionDialog.findViewById(R.id.tts_offline_text).setOnClickListener(this);
		
		type = (TextView) findViewById(R.id.type);
		type.setText(getString(R.string.wakeup_offline_function));
		
		// 初始化本地离线唤醒
		initWakeUp();
		// 启动本地语音唤醒
		wakeUpStart();
	}

	/**
	 * 初始化本地离线唤醒
	 */
	private void initWakeUp() {
		mWakeUpRecognizer = new WakeUpRecognizer(this, Config.appKey);
		mWakeUpRecognizer.setListener(new WakeUpRecognizerListener() {
			@Override
			public void onWakeUpRecognizerStart() {
				log_i("WakeUpRecognizer onRecordingStart");
				setStatusText("语音唤醒已开始");
				setTipText("请说 [你好魔方] 唤醒");
				toastMessage("语音唤醒已开始");
			}

			@Override
			public void onWakeUpError(USCError error) {
				if (error != null) {
					toastMessage("语音唤醒服务异常  异常信息：" + error.toString());
					setTipText(error.toString());
				}
				showResultView();
			}

			@Override
			public void onWakeUpRecognizerStop() {
				log_i("WakeUpRecognizer onRecordingStop");
				toastMessage("语音唤醒录音已停止");
				setStatusText("语音唤醒已停止");
			}

			@Override
			public void onWakeUpResult(boolean succeed, String text, float score) {
				showResultView();
				if (succeed) {
					mVibrator.vibrate(300);
					toastMessage(text + "(唤醒成功)");
					mTextViewResult.setText(text + "(唤醒成功)\n score=" + score);
				}
			}
		});
	}

	private void showResultView() {
		status_panel.setVisibility(View.VISIBLE);
		mLogoImageView.setVisibility(View.GONE);
	}

	protected void setTipText(String tip) {
		mTextViewTip.setText(tip);
	}

	protected void setStatusText(String status) {
		mTextViewStatus.setText(getString(R.string.lable_status) + "(" + status + ")");
	}

	/**
	 * 启动语音唤醒
	 */
	protected void wakeUpStart() {
		toastMessage("开始语音唤醒");
		/** ---设置唤醒命令词集合--- */
		mTextViewResult.setText("");
		mTextViewTip.setText("");
		mWakeUpRecognizer.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		// 主动停止识别CollinWang注释掉
		// mWakeUpRecognizer.cancel();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		mWakeUpRecognizer.start();
	}

	private void log_i(String log) {
		Log.i("demo", log);
	}

	@Override
	protected void onStop() {
		super.onStop();
		log_i("onStop()");
	}

	@Override
	protected void onDestroy() {
		log_i("onDestroy()");
		mWakeUpRecognizer.cancel();
		super.onDestroy();
	}

	private void toastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.function_button:
			mfunctionDialog.show();
			break;
		case R.id.asr_online_text:
			AsrType = ASR_ONLINE_TYPE;
			changeView();
			break;
		case R.id.asr_offline_text:
			AsrType = ASR_OFFLINE_TYPE;
			changeView();
			break;
		case R.id.wakeup_offline_text:
			AsrType = WAKEUP_OFFLINE_TYPE;
			changeView();
			break;
		case R.id.tts_offline_text:
			AsrType = TTS_OFFLINE_TYPE;
			changeView();
			break;
		default:
			break;
		}
	}

	private void changeView() {
		if (AsrType == WAKEUP_OFFLINE_TYPE) {
			Intent intent = new Intent(this, WakeupOfflineActivity.class);
			this.startActivity(intent);
			this.finish();
		}
		mfunctionDialog.dismiss();
	}

}

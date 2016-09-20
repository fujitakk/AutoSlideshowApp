package jp.techacademy.kaori.fujita.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private static final int PERMISSIONS_REQUEST_CODE = 100;
	private boolean perm;

	private ArrayList<Uri> AryUri;
	private int Position = 0;
	Handler handler;
	Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Android 6.0以降の場合
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// パーミッションの許可状態を確認する
			if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				// 許可されている
				perm = true;
			} else {
				// 許可されていないので許可ダイアログを表示する
				requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
				perm = false;
			}
			// Android 5系以下の場合
		} else {
			perm = true;
		}

		Button button_back = (Button) findViewById(R.id.button_back);
		button_back.setOnClickListener(this);

		Button button_start = (Button) findViewById(R.id.button_start);
		button_start.setOnClickListener(this);
		button_start.setText("再生");

		Button button_next = (Button) findViewById(R.id.button_next);
		button_next.setOnClickListener(this);


		if ( perm == true ) {
			// 画像の情報を取得する
			ContentResolver resolver = getContentResolver();
			Cursor cursor = resolver.query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
					null, // 項目(null = 全項目)
					null, // フィルタ条件(null = フィルタなし)
					null, // フィルタ用パラメータ
					null // ソート (null ソートなし)
			);

			//IDを格納する配列をnew
			AryUri = new ArrayList<>();

			if (cursor.moveToFirst()) {
				do {
					// indexからIDを取得し、そのIDから画像のURIを取得する
					int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
					Long id = cursor.getLong(fieldIndex);
					Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

					//IDを配列に入れる
					AryUri.add(imageUri);
				} while (cursor.moveToNext());
			}
			cursor.close();

			//画像を表示
			ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
			imageVIew.setImageURI(AryUri.get(Position));

			//再生/停止用
			handler = new Handler();
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST_CODE:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					perm = true;
				}
				break;
			default:
				break;
		}
	}

	//進む
	private void moveToNext() {
		if(Position < AryUri.size()-1) {
			Position++;
		} else {
			Position = 0;
		}

		ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
		imageVIew.setImageURI(AryUri.get(Position));

//		Log.d("■■■Android", "進むPosition = " + Position);
	}

	//戻る
	private void moveToPrevious() {
		if(Position == 0) {
			Position = AryUri.size()-1;
		} else {
			Position--;
		}

		ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
		imageVIew.setImageURI(AryUri.get(Position));

//		Log.d("■■■Android", "戻るPosition = " + Position);
	}


	@Override
	public void onClick(View v) {

			if (v.getId() == R.id.button_back) {
				//戻る
				moveToPrevious();

			} else if (v.getId() == R.id.button_start) {
				//再生/停止
				//ボタン表示名を取得
				String str = ((TextView) findViewById(R.id.button_start)).getText().toString();

				if ( str.equals("再生")) {
					//タイマーを再生する
					timer = new Timer();
					timer.schedule(new MyTimer(), 2000, 2000);

					//ボタン名を停止に変更
					((TextView) findViewById(R.id.button_start)).setText("停止");

					findViewById(R.id.button_next).setEnabled(false);
					findViewById(R.id.button_back).setEnabled(false);

				} else {
					//タイマーを停止する
					timer.cancel();

					//ボタン名を再生に変更
					((TextView) findViewById(R.id.button_start)).setText("再生");

					findViewById(R.id.button_next).setEnabled(true);
					findViewById(R.id.button_back).setEnabled(true);
				}

			} else if (v.getId() == R.id.button_next) {
				//進む
				moveToNext();
			}

	}

	class MyTimer extends TimerTask {
		@Override
		public void run() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					moveToNext();
				}
			});
		}
	}


}


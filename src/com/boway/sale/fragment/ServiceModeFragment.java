package com.boway.sale.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import com.boway.sale.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceModeFragment extends Fragment implements OnClickListener {
	
	private static final String TAG = "ServiceModeFragment";

	private View view;
	private ImageView qrImage;
	private TextView browserText;
	private TextView linkText;
	private Button phoneBtn;
	private TextView qqText;
	private TextView qrSave;
	private AlertDialog dialog;
	
	static final String CALL_NUMBER = "4007006005";
	
	private static final String qrBAddress = "http://weixin.qq.com/r/nEyNlcrEaZcWrY729xmO";
	int QR_WIDTH = 400;
	int QR_HEIGHT = 400;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.service_mode_layout, container, false);
		qrImage = (ImageView) view.findViewById(R.id.qr_image);
		qrImage.setOnClickListener(this);
		browserText = (TextView) view.findViewById(R.id.boway_browser_text);
		linkText = (TextView) view.findViewById(R.id.browser_link);
		linkText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		linkText.getPaint().setAntiAlias(true);
		phoneBtn = (Button) view.findViewById(R.id.boway_phone_btn);
		phoneBtn.setOnClickListener(this);
		qqText = (TextView) view.findViewById(R.id.boway_qq_text);
		qqText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		qqText.getPaint().setAntiAlias(true);
		qqText.setOnClickListener(this);
		return view;
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.qr_image:
			showQRDialog();
			break;
		case R.id.boway_phone_btn:
			phoneBtn.setEnabled(false);
			handler.postDelayed(run, 3000);
			callHotline();
			break;
		case R.id.boway_qq_text:
//			startQQTalke();
			copyQQNumber();
			break;
		}
	}
	
	Handler handler = new Handler();
	Runnable run = new Runnable() {

		@Override
		public void run() {
			phoneBtn.setEnabled(true);
		}
		
	};
	
	private void startQQTalke() {
		String url = "mqqwpa://im/chat?chat_type=wpa&uin=4007006005&version=1";
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	}
	
	private void copyQQNumber() {
		ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(CALL_NUMBER);
		Toast.makeText(getActivity(), getString(R.string.copy_boway_qq_toast), Toast.LENGTH_SHORT).show();
	}
	
	private void callHotline() {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + CALL_NUMBER));
		startActivity(intent);
	}
	
	private void startBowayWeb() {
		Uri uri = Uri.parse("http://www.boway.com/Service");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
	
	private void createImage() {
		try {
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(qrBAddress, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0x33000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.qr_boway_logo);
            saveImageToGallery(getActivity(), addLogo(bitmap, logo));
//            saveImageToGallery(getActivity(), bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}
	
	private static Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) { 
        if (qrBitmap == null || logoBitmap == null) { 
            return null; 
        } 
   
        int srcWidth = qrBitmap.getWidth(); 
        int srcHeight = qrBitmap.getHeight(); 
        int logoWidth = logoBitmap.getWidth(); 
        int logoHeight = logoBitmap.getHeight(); 
   
        if (srcWidth == 0 || srcHeight == 0 || logoWidth == 0 || logoHeight == 0) { 
            return null; 
        } 
   
        float scaleFactor = srcWidth * 1.0f / 7 / logoWidth; 
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888); 
        try { 
            Canvas canvas = new Canvas(bitmap); 
            canvas.drawBitmap(qrBitmap, 0, 0, null); 
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2); 
            canvas.drawBitmap(logoBitmap, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null); 
            canvas.save(Canvas.ALL_SAVE_FLAG); 
            canvas.restore(); 
        } catch (Exception e) { 
            bitmap = null; 
            e.getStackTrace(); 
        } 
        return bitmap; 
    } 
	
	public static void saveImageToGallery(Context context, Bitmap bmp) {
	    // First save image 
	    File appDir = new File(Environment.getExternalStorageDirectory(), "SaleService");
	    if (!appDir.exists()) {
	        appDir.mkdirs();
	    }
	    String fileName = System.currentTimeMillis() + ".png";
	    File file = new File(appDir, fileName);
	    try {
	        FileOutputStream fos = new FileOutputStream(file);
	        bmp.compress(CompressFormat.PNG, 100, fos);
	        fos.flush();
	        fos.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
		}
	    
	    // Second, turn the image in system gallery.
	    try {
	        MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), fileName, null);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
	    // Third, notify update gallery.
	    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory() + File.separator + "SaleService")));
	    
	    File saveFile = new File(file.getAbsolutePath());
	    if(saveFile.exists()) {
	    	Toast.makeText(context, context.getResources().getString(R.string.save_ok), Toast.LENGTH_SHORT).show();
	    }
	    
	    File dirFile = new File(Environment.getExternalStorageDirectory() + File.separator + "SaleService" + File.separator + fileName);
	    if(dirFile.exists()) {
	    	dirFile.delete();
	    }
	}
	
	private void showQRDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.qr_information_layout, null);
		qrSave = (TextView) view.findViewById(R.id.qr_save);
		qrSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				createImage();
				dialog.dismiss();
			}
		});
		dialog = builder.create();
		dialog.setView(view);
		dialog.show();
	}
	
}

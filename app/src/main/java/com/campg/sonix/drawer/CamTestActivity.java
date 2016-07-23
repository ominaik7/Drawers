package com.campg.sonix.drawer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.campg.sonix.drawer.android.widget.VerticalSeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class CamTestActivity extends Activity {
    private static final String TAG = "CamTestActivity";
    Preview preview;
    Button buttonClick;
    Camera camera = null;
    Activity act;
    Context ctx;
    String str;
    Integer num;
    ImageButton sav,cancel;
    ImageButton capture;
    private static final boolean TRASNPARENT_IS_BLACK = false;
    private static final double SPACE_BREAKING_POINT = 13.0/30.0;
    VerticalSeekBar zoombar;
    private ImageButton flashButton;



    //Flash modes
    private final String[] flashModes = {Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF};
    private int fmi = 0; //flash mode index


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

        }
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        str="" ;//getIntent().getExtras().getString("string");
        num=Integer.parseInt(getIntent().getExtras().getString("num"));
        sav=(ImageButton) findViewById(R.id.save) ;
        cancel=(ImageButton) findViewById(R.id.cancel) ;
        capture=(ImageButton) findViewById(R.id.cap);
        zoombar=(VerticalSeekBar)findViewById(R.id.zoom);
        flashButton = (ImageButton) findViewById(R.id.btn_flash);
        preview = new Preview(this, (SurfaceView) findViewById(R.id.surfaceView));
        preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);
        zoombar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "progress:"+progress);

                if(camera.getParameters().isZoomSupported()){

                    Camera.Parameters params = camera.getParameters();
                    zoombar.setMax(preview.getMaxZoom());
                    params.setZoom(progress);
                    camera.setParameters(params);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                Log.d(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch");
            }
        });

        flashButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setFlashMode();
                    }
                }
        );

        SimpleOrientationListener mOrientationListener = new SimpleOrientationListener(ctx) {

            @Override
            public void onSimpleOrientationChanged(int orientation) {
                if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                    //Toast.makeText(ctx, "its ORIENTATION_LANDSCAPE", Toast.LENGTH_SHORT).show();
                    //ib.startAnimation(RotateAnimation);
                }else if(orientation == Configuration.ORIENTATION_PORTRAIT){
                    //Toast.makeText(ctx, "its ORIENTATION_PORTRAIT", Toast.LENGTH_SHORT).show();

                }
            }
        };
        mOrientationListener.enable();
        capture.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {

                    }
                });
                return false;
            }
        });
        capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    camera.takePicture(null,null,jpegCallback);
                }
                catch (Exception e)
                {
                    Log.e("","pic ",e);
                }
            }
        });

        Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();

        //		buttonClick = (Button) findViewById(R.id.btnCapture);
        //
        //		buttonClick.setOnClickListener(new OnClickListener() {
        //			public void onClick(View v) {
        ////				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //			}
        //		});
        //
        //		buttonClick.setOnLongClickListener(new OnLongClickListener(){
        //			@Override
        //			public boolean onLongClick(View arg0) {
        //				camera.autoFocus(new AutoFocusCallback(){
        //					@Override
        //					public void onAutoFocus(boolean arg0, Camera arg1) {
        //						//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //					}
        //				});
        //				return true;
        //			}
        //		});
    }



    private void setFlashMode(){
        Camera.Parameters params = camera.getParameters();
        switch(fmi){
            case 0: //IF Flash AUTO
            {
                fmi = 1; //Flash ON
                flashButton.setImageResource(R.drawable.flash_on);
                break;
            }
            case 1: //IF Flash ON
            {
                fmi = 2; //Flash OFF
                flashButton.setImageResource(R.drawable.flash_off);
                break;
            }
            case 2: //IF Flash OFF
            {
                fmi = 0; //Flash AUTO
                flashButton.setImageResource(R.drawable.flash_auto);
                break;
            }
            default:
            {
                fmi = 0; //Flash AUTO
                flashButton.setImageResource(R.drawable.flash_auto);
                break;
            }
        }


        params.setFlashMode(flashModes[fmi]);

        Log.d(TAG,params.getFlashMode());

        //Set the new parameters to the camera:
        camera.setParameters(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                releaseCameraAndPreview();
                camera = Camera.open(0);
                camera.startPreview();
                preview.setCamera(camera);
            } catch (RuntimeException ex) {
                Log.e("exc is", " ", ex);
                Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
            }

        }

    }

    private void releaseCameraAndPreview() {
        preview.setCamera(null);
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onPause() {
        if (camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(final byte[] data, Camera camera) {
            sav.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            capture.setVisibility(View.INVISIBLE);
            flashButton.setVisibility(View.INVISIBLE);
            sav.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SaveImageTask().execute(data);
                    resetCam();
                    sav.setVisibility(View.INVISIBLE);
                    cancel.setVisibility(View.INVISIBLE);
                    capture.setVisibility(View.VISIBLE);
                    flashButton.setVisibility(View.VISIBLE);

                }
            });
            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetCam();
                    sav.setVisibility(View.INVISIBLE);
                    cancel.setVisibility(View.INVISIBLE);
                    capture.setVisibility(View.VISIBLE);
                    flashButton.setVisibility(View.VISIBLE);

                }
            });
            /*sav.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);*/
            /*new SaveImageTask().execute(data);
            resetCam();*/
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void>
    {
        private boolean shouldBeBlack(int pixel) {
            int alpha = Color.alpha(pixel);
            int redValue = Color.red(pixel);
            int blueValue = Color.blue(pixel);
            int greenValue = Color.green(pixel);
            if(alpha == 0x00) //if this pixel is transparent let me use TRASNPARENT_IS_BLACK
                return TRASNPARENT_IS_BLACK;
            // distance from the white extreme
            double distanceFromWhite = Math.sqrt(Math.pow(0xff - redValue, 2) + Math.pow(0xff - blueValue, 2) + Math.pow(0xff - greenValue, 2));
            // distance from the black extreme //this should not be computed and might be as well a function of distanceFromWhite and the whole distance
            double distanceFromBlack = Math.sqrt(Math.pow(0x00 - redValue, 2) + Math.pow(0x00 - blueValue, 2) + Math.pow(0x00 - greenValue, 2));
            // distance between the extremes //this is a constant that should not be computed :p
            double distance = distanceFromBlack + distanceFromWhite;
            // distance between the extremes
            return ((distanceFromWhite/distance)>SPACE_BREAKING_POINT);
        }
        public Bitmap convertToMutable(Bitmap imgIn) {
            try {
                //this is the file going to use temporally to save the bytes.
                // This file will not be a image, it will store the raw image data.
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

                //Open an RandomAccessFile
                //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
                //into AndroidManifest.xml file
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

                // get the width and height of the source bitmap.
                int width = imgIn.getWidth();
                int height = imgIn.getHeight();
                Bitmap.Config type = imgIn.getConfig();

                //Copy the byte to the file
                //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
                FileChannel channel = randomAccessFile.getChannel();
                MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
                imgIn.copyPixelsToBuffer(map);
                //recycle the source bitmap, this will be no longer used.
                imgIn.recycle();
                System.gc();// try to force the bytes from the imgIn to be released

                //Create a new bitmap to load the bitmap again. Probably the memory will be available.
                imgIn = Bitmap.createBitmap(width, height, type);
                map.position(0);
                //load it back from temporary
                imgIn.copyPixelsFromBuffer(map);
                //close the temporary file and channel , then delete that also
                channel.close();
                randomAccessFile.close();

                // delete the temp file
                file.delete();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return imgIn;
        }

        /*    private Context mContext;
           private int imageResourceID;

           private ProgressDialog mProgressDialog;

         public SaveImageAsync(Context context, int image) {
              mContext = context;
              imageResourceID = image;
          }
*/
            /*    @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mProgressDialog = new ProgressDialog(mContext);
                    mProgressDialog.setMessage("Saving Image to SD Card");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                }*/

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/camtest");
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                Canvas canvas1,canvas;
                Paint paint1,paint;
                Bitmap bitm = BitmapFactory.decodeByteArray(data[0] , 0, data[0].length);
                int tempW = bitm.getWidth();
                int tempH = bitm.getHeight();
                DisplayMetrics dm = new DisplayMetrics();
                ctx.getApplicationContext();
                WindowManager aa = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                aa.getDefaultDisplay().getMetrics(dm);
                int or = aa.getDefaultDisplay().getRotation();
                Matrix mtx = new Matrix();
                switch (or) {
                    case Surface.ROTATION_0:
                        mtx.postRotate(90);
                        break;
                    case Surface.ROTATION_180:
                        mtx.postRotate(90);
                        break;
                    case Surface.ROTATION_90:
                        mtx.postRotate(0);
                        break;
                    case Surface.ROTATION_270:
                        mtx.postRotate(180);
                        break;
                }
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitm, 0,0,tempW, tempH, mtx, true);
                Bitmap bmpWithBorder = Bitmap.createBitmap(rotatedBitmap.getWidth(), rotatedBitmap.getHeight() + 50, rotatedBitmap.getConfig());
                canvas1 = new Canvas(bmpWithBorder);
                canvas1.drawColor(Color.WHITE);
                canvas1.drawBitmap(rotatedBitmap, 0, 25, null);
          /*      paint1 = new Paint();
                paint1.setColor(Color.WHITE);
                paint1.setStrokeWidth(3);
                canvas1.drawRect(0, 0, tempW+30, tempH+30, paint1);//draw your bg
                canvas1.drawBitmap(rotatedBitmap, 20, 20, paint1);//draw your image on bg*/
                /*Canvas can=new Canvas();
                Paint paint2 = new Paint();

                ColorMatrix cm = new ColorMatrix();
                float a = 77f;
                float b = 151f;
                float c = 28f;
                float t = 120 * -256f;
                cm.set(new float[] { a, b, c, 0, t, a, b, c, 0, t, a, b, c, 0, t, 0, 0, 0, 1, 0 });
                paint2.setColorFilter(new ColorMatrixColorFilter(cm));
                can.drawBitmap(bmpWithBorder, 0, 0, paint2);
*/


                canvas = new Canvas(bmpWithBorder);
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(20);
                paint.setTextSize(20);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                canvas.drawBitmap(bmpWithBorder,0,0,paint);
                canvas.drawText(str+" | "+num++, (bmpWithBorder.getWidth()/2)-20, 20, paint);
                /*Bitmap binarizedImage = convertToMutable(bmpWithBorder);
                // I will look at each pixel and use the function shouldBeBlack to decide
                // whether to make it black or otherwise white
                for(int i=0;i<binarizedImage.getWidth();i++) {
                    for(int c=0;c<binarizedImage.getHeight();c++) {
                        int pixel = binarizedImage.getPixel(i, c);
                        if(shouldBeBlack(pixel))
                            binarizedImage.setPixel(i, c, Color.BLACK);
                        else
                            binarizedImage.setPixel(i, c, Color.WHITE);
                    }
                }*/
                bmpWithBorder.compress(Bitmap.CompressFormat.JPEG,100, outStream);

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data[0].length + " to " + outFile.getAbsolutePath());
                refreshGallery(outFile);
                outStream.flush();
                outStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("h","ey ",e);
            } finally {
            }
            return null;
        }

    }
}
/*
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(Void filename) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }


         /*   FileOutputStream outStream = null;

            // Write to SD Card
           try {
                String output_file_name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + System.currentTimeMillis() + ".jpeg";
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/camtest");
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;*/
  /*  }*/



class Preview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "Preview";

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;
    Camera mCamera;
    Context ctx;
    private Camera.Parameters mParameters;


    Preview(Context context, SurfaceView sv) {
        super(context);
        setWillNotDraw(false);
        mSurfaceView = sv;
//        addView(mSurfaceView);
        ctx = context;
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

    }


    public int getMaxZoom() {
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        mParameters = mCamera.getParameters();
        return mParameters.getMaxZoom();
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();

            // get Camera parameters
            Camera.Parameters params = mCamera.getParameters();

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                // set Camera parameters
                mCamera.setParameters(params);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        float camHeight = (int) (height);
        float newCamHeight;
        float newHeightRatio;

        if (camHeight < height) {
            newHeightRatio = (float) height / (float) mPreviewSize.height;
            newCamHeight = (newHeightRatio * camHeight);
            Log.e(TAG, camHeight + " " + height + " " + mPreviewSize.height + " " + newHeightRatio + " " + newCamHeight);
            setMeasuredDimension((int) (width * newHeightRatio), (int) newCamHeight);
            Log.e(TAG, mPreviewSize.width + " | " + mPreviewSize.height + " | ratio - " + height / width + " | H_ratio - " + newHeightRatio + " | A_width - " + (width * newHeightRatio) + " | A_height - " + newCamHeight);
        } else {
            newCamHeight = camHeight;
            setMeasuredDimension(width, (int) newCamHeight);
            //Log.e(TAG, mPreviewSize.width + " | " + mPreviewSize.height + " | ratio - " + height/width + " | A_width - " + (width) + " | A_height - " + newCamHeight);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.set("orientation", "landscape");
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();
            android.hardware.Camera.CameraInfo info =
                    new android.hardware.Camera.CameraInfo();
            DisplayMetrics dm = new DisplayMetrics();
            ctx.getApplicationContext();
            WindowManager aa = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            aa.getDefaultDisplay().getMetrics(dm);
            int or = aa.getDefaultDisplay().getRotation();
            int orient = 90;
            switch (or) {
                case Surface.ROTATION_0:
                    orient = 90;
                    Log.i("0", "90");;
                    break;
                case Surface.ROTATION_180:
                    orient = 90;
                    Log.i("90", "180");
                    break;
                case Surface.ROTATION_90:
                    orient = 0;
                    Log.i("90", "180");
                    break;
                case Surface.ROTATION_270:
                    orient = 180;
                    Log.i("270", "180");
                    break;
            }
         //   Toast.makeText(ctx, toString().valueOf(orient), Toast.LENGTH_LONG).show();

            mCamera.setDisplayOrientation(orient);
            mCamera.setParameters(parameters);
            mCamera.startPreview();

        }
    }

}

abstract class SimpleOrientationListener extends OrientationEventListener {

    public static final int CONFIGURATION_ORIENTATION_UNDEFINED = Configuration.ORIENTATION_UNDEFINED;
    private volatile int defaultScreenOrientation = CONFIGURATION_ORIENTATION_UNDEFINED;
    public int prevOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private Context ctx;
    private ReentrantLock lock = new ReentrantLock(true);

    public SimpleOrientationListener(Context context) {
        super(context);
        ctx = context;
    }

    public SimpleOrientationListener(Context context, int rate) {
        super(context, rate);
        ctx = context;
    }

    @Override
    public void onOrientationChanged(final int orientation) {
        int currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
        if (orientation >= 330 || orientation < 30) {
            currentOrientation = Surface.ROTATION_0;
           // Toast.makeText(ctx, "its 330 30", Toast.LENGTH_LONG).show();
        } else if (orientation >= 60 && orientation < 120) {
            currentOrientation = Surface.ROTATION_90;
          //  Toast.makeText(ctx, "its 60 120", Toast.LENGTH_LONG).show();
        } else if (orientation >= 150 && orientation < 210) {
            currentOrientation = Surface.ROTATION_180;
        } else if (orientation >= 240 && orientation < 300) {
            currentOrientation = Surface.ROTATION_270;
        }

        if (prevOrientation != currentOrientation && orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            prevOrientation = currentOrientation;
            if (currentOrientation != OrientationEventListener.ORIENTATION_UNKNOWN)
                reportOrientationChanged(currentOrientation);
        }

    }

    private void reportOrientationChanged(final int currentOrientation) {

        int defaultOrientation = getDeviceDefaultOrientation();
        int orthogonalOrientation = defaultOrientation == Configuration.ORIENTATION_LANDSCAPE ? Configuration.ORIENTATION_PORTRAIT
                : Configuration.ORIENTATION_LANDSCAPE;

        int toReportOrientation;

        if (currentOrientation == Surface.ROTATION_0 || currentOrientation == Surface.ROTATION_180)
            toReportOrientation = defaultOrientation;
        else
            toReportOrientation = orthogonalOrientation;

        onSimpleOrientationChanged(toReportOrientation);
    }

    /**
     * Must determine what is default device orientation (some tablets can have default landscape). Must be initialized when device orientation is defined.
     *
     * @return value of {@link Configuration#ORIENTATION_LANDSCAPE} or {@link Configuration#ORIENTATION_PORTRAIT}
     */
    private int getDeviceDefaultOrientation() {
        if (defaultScreenOrientation == CONFIGURATION_ORIENTATION_UNDEFINED) {
            lock.lock();
            defaultScreenOrientation = initDeviceDefaultOrientation(ctx);
            lock.unlock();
        }
        return defaultScreenOrientation;
    }

    /**
     * Provides device default orientation
     *
     * @return value of {@link Configuration#ORIENTATION_LANDSCAPE} or {@link Configuration#ORIENTATION_PORTRAIT}
     */
    private int initDeviceDefaultOrientation(Context context) {

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Configuration config = context.getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();

        boolean isLand = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean isDefaultAxis = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180;

        int result = CONFIGURATION_ORIENTATION_UNDEFINED;
        if ((isDefaultAxis && isLand) || (!isDefaultAxis && !isLand)) {
            result = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            result = Configuration.ORIENTATION_PORTRAIT;
        }
        return result;
    }

    /**
     * Fires when orientation changes from landscape to portrait and vice versa.
     *
     * @param orientation value of {@link Configuration#ORIENTATION_LANDSCAPE} or {@link Configuration#ORIENTATION_PORTRAIT}
     */
    public abstract void onSimpleOrientationChanged(int orientation);

}

package com.example.assignment03_v2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.OutputStream;
import android.content.ContentValues;
import android.graphics.Bitmap.CompressFormat;
import android.provider.MediaStore.Images.Media;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener,
        OnTouchListener {

    //---- Declare Variables Here ----//
    private static final int REQUESTCODE_STORAGE_PERMISSION = 999;
    private int REQUEST_GET_SINGLE_FILE = 1;
    private EditImage ei;
    private Button choosePicture;
    private Button savePicture;
    private int lineSize; //Stores the line thickness
    private Bitmap bm;
    private Bitmap altered;


    private float downX;
    private float downY;
    private float upX;
    private float upY;

    ColorSelect colorSelect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---- Initializing Variables ----//
        lineSize = 2;

        ei = (EditImage) this.findViewById(R.id.edit_image);
        choosePicture = (Button) this.findViewById(R.id.ChoosePictureButton);
        savePicture = (Button) this.findViewById(R.id.SavePictureButton);

        savePicture.setOnClickListener(this);
        choosePicture.setOnClickListener(this);
        ei.setOnTouchListener(this);

        downX = 0;
        downY = 0;
        upX = 0;
        upY = 0;
    }

    public void onClick(View v) {

        if (v == choosePicture || v.getId() == R.id.layout_2) {
            View view2 = findViewById(R.id.layout_2);
            view2.setVisibility(View.INVISIBLE); //turn the splash feature off
            findViewById(R.id.edit_image).setVisibility(View.VISIBLE);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_GET_SINGLE_FILE);
        } else if (v == savePicture) {
            Log.i("onClick","Starting to save the image");
            if (checkPermissionWRITE_EXTERNAL_STORAGE(this)) {
                Log.i("onClick", "App has permission to save image");
                if (altered != null) {
                    Log.i("onClick", "Bitmap is not null");
                    ContentValues contentValues = new ContentValues(3);
                    contentValues.put(Media.DISPLAY_NAME, "Draw On Me");

                    Uri imageFileUri = null;

                    //Below we are asking for the android phone to give permission
                    // to write to the external storage.
                    // Notice the call to storagePermitted.
                    for(int i=0;i<100;i++){
                        if(storagePermitted(MainActivity.this)){
                            imageFileUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
                            break;
                        } else{
                           TextView tv = findViewById(R.id.thick_tv);
                           tv.setText("ERROR");
                           Log.i("onClick","Image did not save. External Write permission was never granted");
                        }

                    }
                    try {
                        OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);
                        altered.compress(CompressFormat.JPEG, 90, imageFileOS);
                        Toast t = Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT);
                        t.show();

                    } catch (Exception e) {

                        Log.i("onClick", e.getMessage());
                    }
                }
            }Log.i("onClick", "No permission to save image");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GET_SINGLE_FILE) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPath(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                    }
                    // Set the image in ImageView
                    bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    altered = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());

                    ei.onImageSelect(bm, altered);

                    ei.setOnTouchListener(this);
                    Log.i("*****image getter", "set background image");
                }
            }
        } catch (Exception e) {
            Log.i("FileSelectorActivity", "File select error", e);
        }
    }

    public String getPath(Uri uri){
        String path = "";

        //where to look
        String[] uriForFiles = {MediaStore.Images.Media.DATA};

        //look for your file
        Cursor cursor = getContentResolver().query(uri, uriForFiles, null, null, null);

        //if found it should be the first and only match
        //get the index of the match and return as string
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;

    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if(ei.getCurrentMode() == "free form" || ei.getCurrentMode() == "symmetry") {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    ei.setDx(downX);
                    downY = event.getY();
                    ei.setDy(downY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    upX = event.getX();
                    ei.setUx(upX);
                    upY = event.getY();
                    ei.setUy(upY);
                    ei.drawSomething();

                    downX = upX;
                    ei.setDx(upX);
                    downY = upY;
                    ei.setDy(upY);
                    break;
                case MotionEvent.ACTION_UP:
                    upX = event.getX();
                    ei.setUx(upX);
                    upY = event.getY();
                    ei.setUy(upY);

                    ei.drawSomething();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
                default:
                    break;

            }
        }
        if(ei.getCurrentMode() != "free form" || ei.getCurrentMode() != "symmetry") {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    ei.setDx(downX);
                    downY = event.getY();
                    ei.setDy(downY);
                    break;
                case MotionEvent.ACTION_UP:
                    upX = event.getX();
                    ei.setUx(upX);
                    upY = event.getY();
                    ei.setUy(upY);

                    ei.drawSomething();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
                default:
                    break;
            }
        }
        return true;
    }


    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionWRITE_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    //Toast.makeText(Login.this, "GET_ACCOUNTS Denied",
                    //Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    /**
     * This function is the listener for shape selection buttons
     * @param v Passes in the button view
     */
    public void selectThickness(View v) {
        TextView tv = findViewById(R.id.thick_tv);
        Log.i("selectThickness", "I am running");

        switch (v.getId()) {
            case R.id.size2:
                tv.setText("2px thick");
                //lineSize=2;
                ei.setThickness(2);
                break;
            case R.id.size4:
                tv.setText("4px thick");
                //lineSize=4;
                ei.setThickness(4);
                break;
            case R.id.size10:
                tv.setText("10px thick");
                //lineSize=10;
                ei.setThickness(10);
                break;
            case R.id.size15:
                tv.setText("15px thick");
                //lineSize=15;
                ei.setThickness(15);
                break;
            default:
                tv.setText("ERROR");
        }
        //setLineWidth();
    }

    /**
     * ColorSelect is the inner enum class that holds
     * the color selection data
     *
     */
    static enum ColorSelect {
        BLACK, WHITE, RED, GREEN, BLUE, ERROR;

        public int getValue(){
            switch (this)
            {
                case BLACK: return Color.BLACK;
                case WHITE: return Color.WHITE;
                case RED: return Color.RED;
                case GREEN: return Color.GREEN;
                case BLUE: return Color.BLUE;
                case ERROR: ;
                default: return -999;
            }
        }

    }

    /**
     *
     * This is the onClick listner for all the color buttons
     * This function gets the color that the user passes in
     *
     * @param v
     */
    public void selectColor (View v) {
        Log.i("selectColor", "Select Color was called");
        ImageView iv = findViewById(R.id.color_iv);
        switch (v.getId()) {
            case R.id.black_b:
                colorSelect = ColorSelect.BLACK;
                ei.setColor(Color.BLACK);
                break;
            case R.id.white_b:
                colorSelect = ColorSelect.WHITE;
                ei.setColor(Color.WHITE);
                break;
            case R.id.red_b:
                colorSelect = ColorSelect.RED;
                ei.setColor(Color.RED);
                break;
            case R.id.green_b:
                colorSelect = ColorSelect.GREEN;
                ei.setColor(Color.GREEN);
                break;
            case R.id.blue_b:
                colorSelect = ColorSelect.BLUE;
                ei.setColor(Color.BLUE);
                break;
            default:
                colorSelect = ColorSelect.ERROR;
        }
        iv.setBackgroundColor(colorSelect.getValue());
        //setPaint();
    }

    /**
     * This function is the listener for shape selection buttons
     * @param v Passes in the button view
     */
    public void selectShape(View v){
        TextView tv = findViewById(R.id.shape_tv);

        switch(v.getId()){
            case R.id.rectangle_b:
                tv.setText("Rectangle");
                ei.modeSwap(1);
                break;
            case R.id.circle_b:
                tv.setText("Circle");
                ei.modeSwap(2);
                break;
            case R.id.freeForm_b:
                tv.setText("Free Form");
                ei.modeSwap(0);
                break;
            case R.id.symmetry_b:
                tv.setText("Symmetry");
                ei.modeSwap(3);
                break;
            case R.id.oval_b:
                tv.setText("Oval");
                ei.modeSwap(4);
                break;
            case R.id.borders_b:
                tv.setText("Borders");
                ei.modeSwap(5);
                ei.drawSomething();
                break;
            default: tv.setText("ERROR");
        }

    }

    /**
     * This function hides the menu layer
     *
     * @param v passes in the button view
     */
    public void hideMenu(View v){
        View view = findViewById(R.id.layout_1);
        view.setVisibility(View.INVISIBLE);
    }

    /**
     * This function shows the menu layer
     *
     * @param v passes in the button view
     */
    public void showMenu(View v){
        View view1 = findViewById(R.id.layout_1);

        view1.setVisibility(View.VISIBLE);
    }

    /**
     * This functions checks if the permission for write to an android phone's
     * external storage is permitted
     * @precondition activity cannot be null
     * @postcondition none
     * @param activity Pass in the current activity you working with
     * @return true if permission is granted. false otherwise
     */
    private static boolean storagePermitted(Activity activity) {

        Boolean readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Boolean writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (readPermission && writePermission) {
            return true;
        }

        ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUESTCODE_STORAGE_PERMISSION);
        return false;
    }

}

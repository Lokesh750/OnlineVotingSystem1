package com.onlie.voting.onlinevotingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.onlie.voting.onlinevotingsystem.Helper.GraphicOverlay;
import com.onlie.voting.onlinevotingsystem.Helper.RectOverlay;


import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private Button faceDetectionButton,InstantVote;
    private GraphicOverlay graphicOverlay;
    private DatabaseReference mref;

    ImageView CameraView;
    AlertDialog alertDialog;
    String check,Phone;
    private ProgressDialog LoadingBar;
    private static final int pic_id = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        LoadingBar=new ProgressDialog(this);
        mref= FirebaseDatabase.getInstance().getReference();

        faceDetectionButton = findViewById(R.id.detect_face_btn);
        graphicOverlay=findViewById(R.id.graphic_overlay);
        InstantVote=findViewById(R.id.instantvote);

        CameraView = findViewById(R.id.camera_view);

        Intent i=getIntent();
        Phone=i.getStringExtra("phone");

        InstantVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LoadingBar.setTitle("Please Wait");
                LoadingBar.setMessage("Please wait while your vote is submitting in our database..");
                LoadingBar.setCanceledOnTouchOutside(false);
                /**LoadingBar.show();
                mref.child("Users").child(Phone).child("Vote").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        LoadingBar.dismiss();
                    }
                });
                 **/

                Intent i =new Intent(CameraActivity.this,SelectParty.class);
                i.putExtra("phone",Phone);
                startActivity(i);
            }
        });

        alertDialog=new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Please wait, Image is Processing..")
                .setCancelable(false)
                .build();

        faceDetectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = getBitmapFromViewUsingCanvas(CameraView);
                graphicOverlay.clear();
                processFaceDetection(bitmap);
            }
        });

        CameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(intent, pic_id);

            }
        });
    }
    private Bitmap getBitmapFromViewUsingCanvas(View view) {
        // Create a new Bitmap object with the desired width and height
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);

        // Create a new Canvas object using the Bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the View into the Canvas
        view.draw(canvas);

        // Return the resulting Bitmap
        return bitmap;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == pic_id) {
            // BitMap is data structure of image file which store the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // Set the image in imageview for display
            CameraView.setImageBitmap(photo);
        }
    }
    private void processFaceDetection(Bitmap bitmap) {

        LoadingBar.setTitle("Please Wait");
        LoadingBar.setMessage("Please wait we are detecting face in the image that you provided..");
        LoadingBar.setCanceledOnTouchOutside(false);
        LoadingBar.show();

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions=new FirebaseVisionFaceDetectorOptions.Builder().build();
        FirebaseVisionFaceDetector firebaseVisionFaceDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(firebaseVisionFaceDetectorOptions);
        firebaseVisionFaceDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        getFaceResult(firebaseVisionFaces);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CameraActivity.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {
        int counter=0;
        for(FirebaseVisionFace face:firebaseVisionFaces)
        {
            Rect rect = face.getBoundingBox();
            RectOverlay rectOverlay=new RectOverlay(graphicOverlay,rect);
            graphicOverlay.add(rectOverlay);

            counter=counter+1;

        }
        LoadingBar.dismiss();
        //alertDialog.dismiss();
        check = ""+counter;
        if(check.equals("1"))
        {
            InstantVote.setVisibility(View.VISIBLE);
            faceDetectionButton.setVisibility(View.INVISIBLE);
        }
        else if(check.equals("0"))
        {
            Toast.makeText(this, "No Face Found", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Place only one face for vote", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        graphicOverlay.clear();
    }


}


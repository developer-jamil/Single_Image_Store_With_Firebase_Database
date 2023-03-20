package com.jamillabltd.singleimagestorewithfirebasedatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    ImageView profileImage;
    Uri imageUri;

    //firebase
    FirebaseStorage firebaseStorage;
    FirebaseDatabase firebaseDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileImage = findViewById(R.id.profileImageId);
        progressBar = findViewById(R.id.progressBarId);
        Button chooseImage = findViewById(R.id.chooseImageId);
        Button saveImage = findViewById(R.id.saveImageButtonId);

        profileImage.setOnClickListener(this);
        chooseImage.setOnClickListener(this);
        saveImage.setOnClickListener(this);

        //firebase
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //get image_1 from firebase
        firebaseDatabase.getReference("Save Single Image").child("image_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Set progressbar visibility to VISIBLE
                progressBar.setVisibility(View.VISIBLE);
                String image = snapshot.getValue(String.class);
                Picasso.with(MainActivity.this)
                        .load(image)
                        .placeholder(R.drawable.image_placeholder)
                        .into(profileImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                // Hide the ProgressBar once the image is loaded
                                progressBar.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {}
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    //onclick to
    @Override
    public void onClick(View view) {

        //profile image
        if (view.getId() == R.id.profileImageId) {
            imageFileChooser();
        }

        //choose image button
        if (view.getId() == R.id.chooseImageId) {
            imageFileChooser();
        }

        //save image button
        if (view.getId() == R.id.saveImageButtonId) {
            saveSelectedImage();
        }


    }


    //image file chooser - fix problem => 1. Manifest Permissions 2. add activity for it 3. proguard keep 4. setting.gradle
    private void imageFileChooser() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowRotation(true) // Add this line to allow image rotation
                .setAllowFlipping(true) // Add this line to allow image flipping
                .setCropMenuCropButtonTitle("Done") // Add this line to change the text of the crop button
                .setActivityTitle("Crop Image") // Add this line to set the title of the crop activity
                .setOutputCompressQuality(50) // Add this line to compress the image to reduce file size
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG) // Add this line to set the image format after compression
                .start(this);
    }

    //set imageview selected image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //image 1
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                imageUri = result.getUri();
                // Do something with the cropped image Uri
                Picasso.with(this).load(imageUri).into(profileImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
                // Handle crop error
                Toast.makeText(this, "An Error From onActivityResult"+error, Toast.LENGTH_SHORT).show();
            }
        }


    }

    //save after select the image
    private void saveSelectedImage() {
        try {
            if (imageUri != null) {
                progressBar.setVisibility(View.VISIBLE);
                //Firebase Storage
                final StorageReference reference = firebaseStorage.getReference("Save Single Image").child("image_1");
                reference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri
                        //RealtimeDatabase
                        -> firebaseDatabase.getReference("Save Single Image").child("image_1").setValue(uri.toString()).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Successfully Uploaded!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                })));
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "No File is Selected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}
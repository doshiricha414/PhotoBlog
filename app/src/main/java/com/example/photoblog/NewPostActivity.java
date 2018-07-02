package com.example.photoblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import io.grpc.Compressor;

public class NewPostActivity extends AppCompatActivity {


    private Toolbar newPostToolbar;
    private ImageView postImage;
    private EditText desc;
    private Button newPost;
    private Uri postImageUri = null;
    private FirebaseAuth mAuth;
    private ProgressBar newPostProgress;
    private String download_url;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;


    public NewPostActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postImage = findViewById(R.id.new_post_image);
        desc = findViewById(R.id.desc);
        newPostProgress = findViewById(R.id.new_post_progress);
        newPost = findViewById(R.id.post_btn);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });
        newPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String description = desc.getText().toString();
                if(!TextUtils.isEmpty(description) && postImageUri!=null){
                    final String randomName = UUID.randomUUID().toString();
                    StorageReference file_path = storageReference.child("post_images").child(randomName+".jpg");
                    file_path.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                getDownloadUrlForImage(randomName,description);

                            }else{
                                    newPostProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });

    }

    private void getDownloadUrlForImage(String randomName, final String description) {


        storageReference.child("post_images/"+randomName+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                 download_url = uri.toString();
                 storeToCloud(download_url,description);
                Toast.makeText(NewPostActivity.this, "New Post Created", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewPostActivity.this, "Failure "+e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
            }

    private void storeToCloud(String download_url,String description) {
        Map<String,Object> postMap = new HashMap<>();
        postMap.put("image_url",download_url);
        postMap.put("description",description);
        postMap.put("user_id",current_user_id);
        postMap.put("timestamp",FieldValue.serverTimestamp());
        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){
                    //Toast.makeText(NewPostActivity.this, "New post created", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(NewPostActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }else{

                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                postImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Crop Image Error "+error, Toast.LENGTH_SHORT).show();
            }
        }
    }

}



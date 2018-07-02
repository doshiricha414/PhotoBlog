package com.example.photoblog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private CircleImageView setUpImage;
    private Toolbar toolbar;
    private Uri mainimageUri;
    private EditText setUpName;
    private Button setup_btn;
    private String image_url;
    private String user_id;
    private boolean isChanged = false;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private ProgressBar pBar;
    private Uri downloadUrl;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        toolbar = findViewById(R.id.setToolbar);
        setSupportActionBar(toolbar);
        setUpName = findViewById(R.id.setName);
        setup_btn = findViewById(R.id.setup_btn);
        getSupportActionBar().setTitle("Account Setup");
        setUpImage = findViewById(R.id.blog_user_image);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        pBar = findViewById(R.id.pBar);
        firebaseFirestore = FirebaseFirestore.getInstance();
        pBar.setVisibility(View.VISIBLE);
        setup_btn.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name = task.getResult().getString("Name");
                        String image = task.getResult().getString("image");
                        mainimageUri = Uri.parse(image);
                        setUpName.setText(name);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.download);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest)
                                .load(image).into(setUpImage);
                    }
                }else{
                    String errorMesssage = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Retrieve Error:"+errorMesssage, Toast.LENGTH_SHORT).show();
                }
                pBar.setVisibility(View.INVISIBLE);
                setup_btn.setEnabled(true);
            }
        });

        setup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user_name = setUpName.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mainimageUri != null) {
                    pBar.setVisibility(View.VISIBLE);
                    if(isChanged) { //Image is changed
                        user_id = firebaseAuth.getCurrentUser().getUid();
                        final StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(mainimageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storeFirestore(user_name,isChanged);
                                } else {
                                    String errorMesssage = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Image Error:" + errorMesssage, Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }else{ //if image is not changed
                    storeFirestore(user_name,isChanged);
                     }
                }
                pBar.setVisibility(View.INVISIBLE);
            }
        });

        setUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    int result = ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    if(result != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE},1);
                        BringImagePicker();
                    }else
                    {
                        BringImagePicker();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainimageUri = result.getUri();
                setUpImage.setImageURI(mainimageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Crop Image Error "+error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void storeFirestore(final String user_name,Boolean isChanged){
        if(isChanged) {
            storageReference.child("profile_images/" + user_id + ".jpg").getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri;
                            image_url = downloadUrl.toString();
                            storeIntoCloud(user_name, image_url);
                        }
                    });
        }else{
            image_url = mainimageUri.toString();
            storeIntoCloud(user_name, image_url);
        }
    }

    public void storeIntoCloud(String user_name,String image_url){
        Map<String,String> userMap = new HashMap<>();
        userMap.put("Name",user_name);
        userMap.put("image",image_url);
        firebaseFirestore.collection("Users").document(user_id).set(userMap).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(SetupActivity.this,MainActivity.class);
                            startActivity(i);
                            //finish();
                        }else{
                            String errorMesssage = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Firestore Error:"+errorMesssage, Toast.LENGTH_SHORT).show();
                        }
                        pBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    public void BringImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }


}

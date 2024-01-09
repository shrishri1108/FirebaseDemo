package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.firebase.databinding.ActivityInsertDataBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.InputStream;
import java.sql.Time;
import java.util.Random;

public class InsertData extends AppCompatActivity {

    private StudentDataHolder studentDataHolder;
    private ActivityInsertDataBinding insertDataBinding;
    private Uri filepath;
    private StorageReference uploader;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        insertDataBinding = ActivityInsertDataBinding.inflate(getLayoutInflater());
        setContentView(insertDataBinding.getRoot());


        insertDataBinding.btnSaveStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inserting data into firebase
                // taking text from input fields and bind them into an adapter.
                uploadingDataToRealTimeFirebaseDatabase();
            }
        });
//
//        insertDataBinding.databaseDataList.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
//        DatabaseListDataAdapter databaseListDataAdapter = new DatabaseListDataAdapter(this);
//        insertDataBinding.databaseDataList.setAdapter(databaseListDataAdapter);


        // Uploading user image interface
        insertDataBinding.imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                insertDataBinding.defaultInterface.setVisibility(View.INVISIBLE);
                insertDataBinding.showImgs.setImageBitmap(null);
                insertDataBinding.showImagOuterInterface.setVisibility(View.VISIBLE);
                Dexter.withActivity(InsertData.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent, "Please select Image"), 1);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
                insertDataBinding.btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        uploadToFirebaseCloudStorage();
                        insertDataBinding.showImagOuterInterface.setVisibility(View.GONE);
                        insertDataBinding.defaultInterface.setVisibility(View.VISIBLE);
                    }
                });
                insertDataBinding.btnSaveStudent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        uploadingDataToRealTimeFirebaseDatabase();
                    }
                });

                insertDataBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        insertDataBinding.showImagOuterInterface.setVisibility(View.GONE);
                        insertDataBinding.defaultInterface.setVisibility(View.VISIBLE);
                    }
                });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data!=null) {
            filepath = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                insertDataBinding.showImgs.setImageBitmap(bitmap);
            } catch (Exception e) {
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadToFirebaseCloudStorage() {

        // Uploading Image to Cloud Storage
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("File Uploader ");
        dialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String file_name_es= "image-  " + SystemClock.currentThreadTimeMillis();
        uploader= storage.getReference().child(file_name_es);
        uploader.putFile(filepath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        dialog.dismiss();
                        insertDataBinding.tvUploadedFileName.setVisibility(View.VISIBLE);
                        Toast.makeText(InsertData.this, " File uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        float percent = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        dialog.setMessage("Uploading:" + (int) percent + "%");
                    }
                });
    }


    private void uploadingDataToRealTimeFirebaseDatabase() {

                uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    studentDataHolder = new StudentDataHolder(
                            insertDataBinding.etName.getText().toString(),
                            insertDataBinding.etAge.getText().toString(),
                            insertDataBinding.etContactNo.getText().toString(),
                            uri.toString(),
                            insertDataBinding.etUserPwd.getText().toString()
                    );
                    // Step -1: get firebase database instance
                    FirebaseDatabase db = FirebaseDatabase.getInstance();

                    // step -2: get child node reference  of  root node at which you want to insert data .
                    // If there is no child inside root node then getReference("student") -------> getReference()
                    DatabaseReference node = db.getReference("student");


                    // Step -3 : Setting the input value into a node
                    // If you want to insert  data further inside a node then you can use child("child_node_name") at the reference of child node of root node
                    // and then using setValue(Object obj) method to insert value at that node .

                    node.child(insertDataBinding.etRollNo.getText().toString())
                            .setValue(studentDataHolder);

                    insertDataBinding.etRollNo.setText("");
                    insertDataBinding.etName.setText("");
                    insertDataBinding.etAge.setText("");
                    insertDataBinding.etUserPwd.setText("");
                    insertDataBinding.etRollNo.setText("");
                    insertDataBinding.tvUploadedFileName.setVisibility(View.GONE);
                    insertDataBinding.etContactNo.setText("");
                    Intent intent=new Intent(InsertData.this, Dashboard.class);
                    Toast.makeText(InsertData.this, " Value inserted ", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
            });

    }

}

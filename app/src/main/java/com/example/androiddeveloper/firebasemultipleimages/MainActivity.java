package com.example.androiddeveloper.firebasemultipleimages;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Integer> ImagesArray = new ArrayList<Integer>();


    private static final int RESULT_LOAD_IMAGE = 1;
    private Button mSelectBtn;
    private RecyclerView mUploadList;

    private List<String> fileNameList;
    private List<String> fileDoneList;

    private UploadListAdapter uploadListAdapter;

    private StorageReference mStorage;

    private LinearLayout gallery;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gallery = findViewById(R.id.gallery);
        inflater = LayoutInflater.from(this);
        mStorage = FirebaseStorage.getInstance().getReference();

    mSelectBtn = (Button) findViewById(R.id.upload);
    mUploadList = (RecyclerView) findViewById(R.id.recyclerview);

    fileNameList = new ArrayList<>();
    fileDoneList = new ArrayList<>();

    uploadListAdapter = new UploadListAdapter(fileNameList,fileDoneList);


    mUploadList.setLayoutManager(new LinearLayoutManager(this));
    mUploadList.setHasFixedSize(true);
    mUploadList.setAdapter(uploadListAdapter);


    mSelectBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),RESULT_LOAD_IMAGE);

        }
    });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==RESULT_LOAD_IMAGE && resultCode==RESULT_OK)
        {
            if (data.getClipData()!=null ||data.getData()!=null){

                gallery.removeAllViews();

                fileNameList.clear();
                fileDoneList.clear();

                int totalItemsSelected;

                try {

                    if (data.getClipData().getItemCount() > 1) {
                        totalItemsSelected = data.getClipData().getItemCount();
                    } else totalItemsSelected = 1;
                }catch (Exception e){
                    totalItemsSelected =1;
                }

                for (int i=0;i<totalItemsSelected;i++)
                {
                    Uri fileUri;

                    try {
                        if (data.getClipData().getItemCount() > 1) {
                            fileUri = data.getClipData().getItemAt(i).getUri();
                        } else fileUri = data.getData();
                    }catch (Exception e){
                        fileUri = data.getData();
                    }
                    String fileName = getFileName(fileUri);
                    fileNameList.add(fileName);
                    fileDoneList.add("uploading");
                    uploadListAdapter.notifyDataSetChanged();


                    View view = inflater.inflate(R.layout.scroll_item,gallery,false);
                    TextView textView = view.findViewById(R.id.imagename);
                    textView.setText(fileName);
                    ImageView imageView = view.findViewById(R.id.scrollimage);
                    imageView.setImageURI(fileUri);
                    gallery.addView(view);



                    StorageReference fileToUpload = mStorage.child("Images").child(fileName);

                    final int finalI = i;

                    fileToUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            fileDoneList.remove(finalI);
                            fileDoneList.add(finalI,"Done");
                            uploadListAdapter.notifyDataSetChanged();
                             Toast.makeText(MainActivity.this,"Done",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }
    }



    public String getFileName(Uri uri){

        String result = null;
        if (uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri,null,null,null,null);
            try {
                if (cursor!=null&&cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }
        if (result==null){
            result = uri.getPath();
            int cut=result.lastIndexOf('/');
            if (cut!=-1){
                result=result.substring(cut+1);
            }
        }
        return result;

    }


}

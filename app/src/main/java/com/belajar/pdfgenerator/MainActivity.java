package com.belajar.pdfgenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PdfCreatorActivity";
    private EditText mContentEditText;
    private Button mCreateButton;
    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContentEditText = findViewById(R.id.edit_text_content);
        mCreateButton = findViewById(R.id.button_create);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContentEditText.getText().toString().isEmpty()){
                    mContentEditText.setError("Isi tulisan terlebih dahulu!");
                    mContentEditText.requestFocus();
                    return;
                }
                try {
                    createPdfWrapper();
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (DocumentException e){

                }
            }
        });

    }

    private void createPdfWrapper() throws  FileNotFoundException, DocumentException {
        int hasWriteStoragePersmission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePersmission != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)){
                       showMessageOKcancel("You need to allow acces to Storage",new DialogInterface.OnClickListener(
                       ){
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                   requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSIONS);
                               }
                           }
                       }) ;
                       return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }else {
            createPdf();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    try {
                        createPdfWrapper();
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }catch (DocumentException e){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(this,"WRITE_EXTERNAL Permission Denied",Toast.LENGTH_SHORT).show();
                }break;
                default:
                    super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }


    private void showMessageOKcancel(String msg, DialogInterface.OnClickListener onClickListener) {
            new AlertDialog.Builder(this).setMessage(msg).setPositiveButton("Ok",onClickListener)
                    .setNegativeButton("Keluar",null)
                    .create()
                    .show();
    }

    private void createPdf() throws FileNotFoundException,DocumentException{
        File docsFolder = new File(Environment.getExternalStorageDirectory() +"/Documents");
        if (!docsFolder.exists()){
            docsFolder.mkdir();
            Log.i(TAG,"Buat Folder PDF Baru");
        }
        pdfFile = new File(docsFolder.getAbsolutePath(),"PDFgenerate.pdf");
        OutputStream outputStream = new FileOutputStream(pdfFile);
        Document document = new Document();
        PdfWriter.getInstance(document,outputStream);
        document.open();
        document.add(new Paragraph(mContentEditText.getText().toString()));
        document.close();
        previewPdf();

    }

    private void previewPdf() {
        PackageManager packageManager = getPackageManager();
        Intent testIntent =  new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent,PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size()>0){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, "com.belajar.pdfgenerator.fileprovider",pdfFile );
            intent.setDataAndType(uri,"application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }else {
            Toast.makeText(this,"Download aplikasi pdf viewer untuk melihat hasil generate",Toast.LENGTH_SHORT).show();
        }
    }
}

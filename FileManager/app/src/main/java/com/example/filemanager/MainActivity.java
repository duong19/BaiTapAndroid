package com.example.filemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> items, copyDir;
    ListView listView;
    String SdPath, rootDir, dialogCopyPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list_item);
        items = new ArrayList<>();

        rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        askPermissionFromUser();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back_directory) {
            if ( !SdPath.equals(rootDir) ) {
                while (SdPath.charAt(SdPath.length()-1) != '/') {
                    SdPath = SdPath.substring(0, SdPath.length() - 1);
                }
                SdPath = SdPath.substring(0, SdPath.length() - 1);
                getInfDir(SdPath);
            }
        }
        else if (id == R.id.action_add_file) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.add_file);
            final EditText editNameFolder = dialog.findViewById(R.id.edit_name_file);
            final EditText editContentFile = dialog.findViewById(R.id.edit_content_file);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            Button btn_create = dialog.findViewById(R.id.btn_create);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nameFile = editNameFolder.getText().toString().trim();
                    String contentFile = editContentFile.getText().toString();
                    String path = SdPath + "/" + nameFile + ".txt";
                    File newFile = new File(path);
                    try {
                        FileWriter writer =new FileWriter(newFile,true);
                        writer.append(contentFile);
                        writer.flush();
                        writer.close();
                        Toast.makeText(MainActivity.this,"Add file " + nameFile + " successfully",Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        getInfDir(SdPath); //load lai cay thu muc
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialog.show();
        }
        else if (id == R.id.action_add_folder) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.add_folder);
            final EditText editNameFolder = dialog.findViewById(R.id.edit_name_folder);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            Button btn_create = dialog.findViewById(R.id.btn_create);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nameFolder = editNameFolder.getText().toString().trim();
                    String path = SdPath + "/" + nameFolder;
                    File newFolder = new File(path);
                    if ( !newFolder.exists() ) {
                        newFolder.mkdir();
                        Toast.makeText(MainActivity.this,"Create " + nameFolder + " successfully",Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        getInfDir(SdPath); //load lai cau truc thu muc hien tai
                    }
                    else {
                        Toast.makeText(MainActivity.this, nameFolder + " existed", Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose action ?");
        menu.add(0, 0, 0, "Rename");
        menu.add(0, 1, 0, "Delete");
        menu.add(0, 2, 0, "Copy to");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final String oldName = items.get(info.position);
        int id = item.getItemId();
        if (id == 0) {      //neu la edit name
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.edit_name);
            final EditText editNameFolder = dialog.findViewById(R.id.edit_name_folder);
            editNameFolder.setText(oldName);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            Button btn_update = dialog.findViewById(R.id.btn_update);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newName = editNameFolder.getText().toString().trim();
                    if (newName != null) {
                        File old = new File(SdPath + "/"+ oldName);
                        File new1 = new File(SdPath + "/"+ newName);
                        boolean check = old.renameTo(new1);
                        if (check) {
                            Toast.makeText(MainActivity.this, "Rename " + oldName + " successfully", Toast.LENGTH_LONG).show();
                            getInfDir(SdPath);
                            dialog.cancel();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Input can't null", Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialog.show();
        }
        else if (id == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            AlertDialog dialog = builder.setMessage("Are you sure you want to delete " + oldName + " ?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            File fileDelete = new File(SdPath + "/" + oldName);
                            deleteFile(fileDelete);
                            getInfDir(SdPath);
                            Toast.makeText(MainActivity.this,"Delete successfully",Toast.LENGTH_LONG).show();
                        }
                    })
                    .create();
            dialog.show();
        }
        else if (id == 2) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.copy_file);
            final ListView list = dialog.findViewById(R.id.list_folder_copy);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
            Button btn_copy = dialog.findViewById(R.id.btn_copy);
            ImageButton btn_back = dialog.findViewById(R.id.btn_back);


            dialogCopyPath = rootDir;
            copyDir = new ArrayList<>();
            getInfDirCopy(list,dialogCopyPath);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String f = copyDir.get(position);
                    File tmp = new File(dialogCopyPath + "/" + f);
                    if ( tmp.exists() && !tmp.isFile() ) {
                        dialogCopyPath = dialogCopyPath + "/" + f;
                        getInfDirCopy(list,dialogCopyPath);
                    }
                }
            });

            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( !dialogCopyPath.equals(rootDir) ) {
                        while (dialogCopyPath.charAt(dialogCopyPath.length()-1) != '/') {
                            dialogCopyPath = dialogCopyPath.substring(0, dialogCopyPath.length() - 1);
                        }
                        //cat tiep dau /
                        dialogCopyPath = dialogCopyPath.substring(0, dialogCopyPath.length() - 1);
                        //load lai thu muc
                        getInfDirCopy(list,dialogCopyPath);
                    }
                }
            });

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            btn_copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newName = "copy_" + oldName;
                    String destinationPath = dialogCopyPath + "/" + newName ;
                    String sourcePath = SdPath + "/" + oldName;
                    try {
                        InputStream is = new FileInputStream(new File(sourcePath));
                        OutputStream os = new FileOutputStream(new File(destinationPath));
                        OutputStreamWriter writer = new OutputStreamWriter(os);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) != -1)
                            os.write(buffer, 0, len);
                        writer.close();
                        os.close();
                        Toast.makeText(MainActivity.this, "Copy file successfully", Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                    dialog.cancel();
                }
            });
            dialog.show();
        }

        return super.onContextItemSelected(item);
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteFile(child);
            }
        }
        file.delete();
    }

    private void getInfDir(String path) {
        items.clear();
        File file = new File(path);
        String[] list = file.list();
        try {
            for (int i = 0 ; i < list.length ; i++) {
                items.add(list[i]);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        registerForContextMenu(listView);
    }

    private void getInfDirCopy(ListView listView, String path) {
        copyDir.clear();
        File file = new File(path);
        String[] list = file.list();
        try {
            for (int i = 0 ; i < list.length ; i++) {
                copyDir.add(list[i]);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,copyDir);
        listView.setAdapter(adapter);
    }

    private void askPermissionFromUser() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permission Denied. Asking for permission.", Toast.LENGTH_LONG);
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1234);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Denied.",Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this,"Permission Granted.",Toast.LENGTH_LONG).show();
        }
    }
}

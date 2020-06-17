package com.example.bt12;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;

import com.example.bt12.adapter.StudentAdapter;
import com.example.bt12.model.StudentModel;

import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bt12.database.StudentDatabase;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    StudentDatabase database;
    List<StudentModel> studentList;
    RecyclerView.Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        studentList = new ArrayList<>();

        database = new StudentDatabase(this,"student.sqlite",null,1);

        database.QueryData("CREATE TABLE IF NOT EXISTS Student(MSSV INTEGER PRIMARY KEY , HoTen VARCHAR(100) , NgaySinh VARCHAR(100), Email VARCHAR(100) , QueQuan VARCHAR(100) );");

        getDataFromSQL();

    }
    //tao menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getDataFromSQLBySearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() < 1) {
                    getDataFromSQL();
                }
                else {
                    getDataFromSQLBySearch(newText);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            dialogAddSV();
        }
        return super.onOptionsItemSelected(item);
    }

    private void dialogAddSV() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_student);
        final EditText editMssv = dialog.findViewById(R.id.edit_mssv);
        final EditText editHoTen = dialog.findViewById(R.id.edit_hoTen);
        final EditText editNgaySinh = dialog.findViewById(R.id.edit_ngaySinh);
        final EditText editEmail = dialog.findViewById(R.id.edit_email);
        final EditText editQueQuan = dialog.findViewById(R.id.edit_queQuan);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_save = dialog.findViewById(R.id.btn_save);

        editNgaySinh.addTextChangedListener(new TextWatcher(){
            private String current = "";
            private String ddmmyyyy = "ddmmyyyy";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{

                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);

                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);


                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    editNgaySinh.setText(current);
                    editNgaySinh.setSelection(sel < current.length() ? sel : current.length());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mssv = editMssv.getText().toString().trim();
                String hoTen = editHoTen.getText().toString().trim();
                String ngaySinh = editNgaySinh.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String queQuan = editQueQuan.getText().toString().trim();
                if (mssv.equals("") || hoTen.equals("") || ngaySinh.equals("") || email.equals("") || queQuan.equals("")) {
                    dialogMessage("Thông tin thiếu");
                }
                else {
                    Cursor tmp = database.GetData("SELECT * FROM Student WHERE Mssv="+Integer.parseInt(mssv)+" ");
                    if( !tmp.moveToFirst()) {
                        database.QueryData("INSERT INTO Student VALUES ("+mssv+",'"+hoTen+"','"+ngaySinh+"','"+email+"','"+queQuan+"')");
                        dialogMessage("Thêm sinh viên thành công");
                        dialog.cancel();
                        getDataFromSQL();
                    }
                    else {
                        dialogMessage("Mã số sinh viên đã tồn tại");
                    }
                }
            }
        });

        dialog.show();
    }

    //dialog edit sinh vien
    public void dialogEditSV(StudentModel student) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_student);
        final EditText editMssv = dialog.findViewById(R.id.edit_mssv);
        final EditText editHoTen = dialog.findViewById(R.id.edit_hoTen);
        final EditText editNgaySinh = dialog.findViewById(R.id.edit_ngaySinh);
        final EditText editEmail = dialog.findViewById(R.id.edit_email);
        final EditText editQueQuan = dialog.findViewById(R.id.edit_queQuan);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_save = dialog.findViewById(R.id.btn_save);

        editMssv.setText(student.getMSSV()+"");
        editMssv.setEnabled(false);
        editHoTen.setText(student.getHoTen());
        editNgaySinh.setText(student.getNgaySinh());
        editEmail.setText(student.getEmail());
        editQueQuan.setText(student.getQueQuan());

        editNgaySinh.addTextChangedListener(new TextWatcher(){
            private String current = "";
            private String ddmmyyyy = "ddmmyyyy";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{

                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);

                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);


                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    editNgaySinh.setText(current);
                    editNgaySinh.setSelection(sel < current.length() ? sel : current.length());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mssv = editMssv.getText().toString().trim();
                String hoTen = editHoTen.getText().toString().trim();
                String ngaySinh = editNgaySinh.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String queQuan = editQueQuan.getText().toString().trim();
                if (hoTen.equals("") || ngaySinh.equals("") || email.equals("") || queQuan.equals("")) {
                    dialogMessage("Thông tin còn thiếu!");
                }
                else {
                    database.QueryData("UPDATE Student SET HoTen='"+hoTen+"',NgaySinh='"+ngaySinh+"',Email='"+email+"',QueQuan='"+queQuan+"' WHERE Mssv="+Integer.parseInt(mssv)+" ");
                    dialogMessage("Sửa thông tin sinh viên thành công.");
                    dialog.cancel();
                    getDataFromSQL();
                }
            }
        });

        dialog.show();
    }

    public void dialogDeleteSV(final StudentModel student) {
        final AlertDialog.Builder  alBuilder = new AlertDialog.Builder(this);
        alBuilder.setTitle("Thông báo!");
        alBuilder.setMessage("Bạn muốn xoá sinh viên có tên: "+student.getHoTen()+" ?");
        alBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int mssv = student.getMSSV();
                database.QueryData("DELETE FROM Student WHERE Mssv="+mssv+" ");
                dialogMessage("Xoá sinh viên thành công");
                getDataFromSQL();
            }
        });
        alBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alBuilder.show();
    }

    private void dialogMessage(String message){
        final AlertDialog.Builder  alBuilder = new AlertDialog.Builder(this);
        alBuilder.setTitle("Thông báo !");
        alBuilder.setMessage(message);
        alBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alBuilder.show();
    }

    private void getDataFromSQL() {
        studentList.clear();
        Cursor dataSV = database.GetData("SELECT * FROM Student");
        while (dataSV.moveToNext()) {
            int mssv = dataSV.getInt(0);
            String hoTen = dataSV.getString(1);
            String ngaySinh = dataSV.getString(2);
            String email = dataSV.getString(3);
            String queQuan = dataSV.getString(4);
            //them vao data
            studentList.add(new StudentModel(mssv,hoTen,ngaySinh,email,queQuan));
        }
        showData();
    }

    private void showData(){
        final RecyclerView recyclerView = findViewById(R.id.layout_recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager); //noi voi view

        adapter = new StudentAdapter(studentList,this);
        recyclerView.setAdapter(adapter);
    }

    private void getDataFromSQLBySearch(String keyword) {
        studentList.clear();

        int check = 0;
        for(int i = 0 ; i < keyword.length() ; i++) {
            if( !Character.isDigit(keyword.charAt(i)) ) {
                check = 1;
            }
        }
        Cursor dataSV;
        if (check == 0) {
            dataSV = database.GetData("SELECT * FROM Student WHERE MSSV="+keyword+" OR HoTen like '%"+keyword+"%'");
        }
        else {
            dataSV = database.GetData("SELECT * FROM Student WHERE HoTen like '%"+keyword+"%'");
        }
        while (dataSV.moveToNext()) {
            int mssv = dataSV.getInt(0);
            String hoTen = dataSV.getString(1);
            String ngaySinh = dataSV.getString(2);
            String email = dataSV.getString(3);
            String queQuan = dataSV.getString(4);
            studentList.add(new StudentModel(mssv,hoTen,ngaySinh,email,queQuan));
        }
        showData();
    }
}

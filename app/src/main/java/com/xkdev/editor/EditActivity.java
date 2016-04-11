package com.xkdev.editor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.xkdev.editor.settings.SettingsActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by user on 06.04.2016.
 */
public class EditActivity extends AppCompatActivity { private final static String DIR_SD = "Editor/MyFiles";
    String filePath;
    private EditText mEditText;
    final static int PICKFILE_CODE = 1;
    SharedPreferences sPref;
    EditText mETFileName;

    final String Logs = "MyLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        mEditText = (EditText) findViewById(R.id.etText);
        filePath = getIntent().getStringExtra("filepath");
        openFileSD(filePath);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Проверка - открывать ли файл при запуске
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        //Смена размера шрифта
        float fSize = Float.parseFloat(sp.getString(getString(R.string.pref_size), "20"));
        mEditText.setTextSize(fSize);

        //Смена стиля шрифта
        String regular = sp.getString(getString(R.string.pref_style), "");
        int typeface = Typeface.NORMAL;

        if(regular.contains(getString(R.string.pref_style_bold))){
            typeface += Typeface.BOLD;
        }
        if(regular.contains(getString(R.string.pref_style_italic))){
            typeface += Typeface.ITALIC;
        }
        mEditText.setTypeface(null, typeface);

        //Смена цвета текста
        String sColor = sp.getString(getString(R.string.pref_color), "");
        int textColor = Color.BLACK;

        if(sColor.contains(getString(R.string.pref_color_black))){
            textColor = Color.BLACK;
        }
        else if(sColor.contains(getString(R.string.pref_color_blue))){
            textColor = Color.BLUE;
        }
        else if(sColor.contains(getString(R.string.pref_color_green))){
            textColor = Color.GREEN;
        }
        else if(sColor.contains(getString(R.string.pref_color_red))){
            textColor = Color.RED;
        }
        mEditText.setTextColor(textColor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_open:
                openFileManager();
                return true;
            case R.id.action_save:
                if(filePath != null)
                    writeFileSD(filePath);
                return true;
            case R.id.action_create:
                createFileSD();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return true;
        }
    }

    //Метод для октрытия файла
    public void openFileSD(String filePath){

        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return;
        }

        File sdFile = new File(filePath);

        try{
            BufferedReader bfReader = new BufferedReader(new FileReader(sdFile));
            String str;
            StringBuilder sBuilder = new StringBuilder();
            while((str = bfReader.readLine()) != null){
                sBuilder.append(str + "\n");
            }
            mEditText.setText(sBuilder.toString());
            Toast.makeText(this, "Открыто: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //Метод для записи файла на sd card
    public void writeFileSD(String filePath){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.d(Logs, "SD карта не доступна: " + Environment.getExternalStorageState());
            return;
        }
        File sdFile = new File(filePath);

        try{
            BufferedWriter bWriter = new BufferedWriter(new FileWriter(sdFile));
            bWriter.write(mEditText.getText().toString());
            bWriter.close();
            sPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("file",filePath);
            ed.commit();
            Toast.makeText(getApplicationContext(), R.string.succes_write_SD + sdFile.getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_write_SD + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    //Метод для создания нового файла
    public void createFileSD(){
        mETFileName = new EditText(EditActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Введите имя файла");
        builder.setView(mETFileName);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mETFileName != null){
                    String fileName = mETFileName.getText().toString() + ".txt";
                    File sdPath = Environment.getExternalStorageDirectory();
                    sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
                    writeFileSD(sdPath + "/" + fileName);

                }
            }
        });
        builder.setCancelable(true);
        builder.show();
    }
    //Метод для открытия файлового менеджера
    public void openFileManager()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, PICKFILE_CODE);

    }
    //Метод для обработки выбора файла
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PICKFILE_CODE){
            if(data!=null){
                filePath = data.getData().getPath();
                openFileSD(filePath);
            }
            else return;
        }
        else
            return;
        if(resultCode == RESULT_CANCELED){
            return;
        }
    }
}
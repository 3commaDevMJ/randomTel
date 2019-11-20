package org.mingmingkim.randomtel;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

    private List<PhoneBook> phoneBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Random rnd = new Random();

        //권한 체크
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();


        setContentView(R.layout.activity_main);
        Intent splashIntent = new Intent(this,LoadingActivity.class);
        startActivity(splashIntent);


        //전화번호부에서 전화번호리스트 가져오기
        phoneBooks = getContacts(this);
        //추출 후 셋팅
        int p = rnd.nextInt(phoneBooks.size()); // 0 <= p < 500 ③
        PhoneBook pb = phoneBooks.get(p);
        TextView name =  (TextView)findViewById(R.id.name);
        TextView tel =  (TextView)findViewById(R.id.tel);
        name.setText(pb.getName());
        tel.setText(pb.getTel());
        final String telNum = pb.getTel();


        //통화버튼 누를시
        Button telBtn = (Button)findViewById(R.id.telBtn);
        telBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                String callTel = "tel:"+telNum;
                Log.d("sss",callTel);
                startActivity(new Intent("android.intent.action.DIAL",Uri.parse(callTel)));
            }
        });

        //문자버튼 누를시
        Button talkBtn = (Button)findViewById(R.id.talkBtn);
        talkBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                String talkTel = "smsto:"+telNum;
                startActivity(new Intent("android.intent.action.VIEW",Uri.parse(talkTel)));
            }
        });
    }


    public List<PhoneBook> getContacts(Context context){

        List<PhoneBook> datas = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();

        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = { ContactsContract.CommonDataKinds.Phone.CONTACT_ID // 인덱스 값, 중복될 수 있음 -- 한 사람 번호가 여러개인 경우
                ,  ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ,  ContactsContract.CommonDataKinds.Phone.NUMBER};
        // 4. ContentResolver로 쿼리를 날림 -> resolver 가 provider 에게 쿼리하겠다고 요청
        Cursor cursor = resolver.query(phoneUri, projection, null, null, null);

        // 4. 커서로 리턴된다. 반복문을 돌면서 cursor 에 담긴 데이터를 하나씩 추출
        if(cursor != null){
            while(cursor.moveToNext()){
                // 4.1 이름으로 인덱스를 찾아준다
                int idIndex = cursor.getColumnIndex(projection[0]); // 이름을 넣어주면 그 칼럼을 가져와준다.
                int nameIndex = cursor.getColumnIndex(projection[1]);
                int numberIndex = cursor.getColumnIndex(projection[2]);
                // 4.2 해당 index 를 사용해서 실제 값을 가져온다.
                String id = cursor.getString(idIndex);
                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);

                PhoneBook phoneBook = new PhoneBook();
                phoneBook.setId(id);
                phoneBook.setName(name);
                phoneBook.setTel(number);
                Log.d("tel",phoneBook.getTel());
                datas.add(phoneBook);
            }
        }
        // 데이터 계열은 반드시 닫아줘야 한다.
        cursor.close();
        return datas;
    }
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }


    };
}

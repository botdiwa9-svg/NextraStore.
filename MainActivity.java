
package com.nextrastore.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout box; android.content.SharedPreferences prefs;
    String adminUser="admin", adminPass="nextra123";

    public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("keys",0);
        showLogin();
    }

    TextView title(String s){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(24); t.setTextColor(Color.DKGRAY);
        t.setPadding(0,16,0,16); return t;
    }
    Button btn(String s){ Button b=new Button(this); b.setText(s); return b; }

    void base(){
        box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(40,30,40,30);
        ScrollView sc=new ScrollView(this); sc.addView(box); setContentView(sc);
    }

    void showLogin(){
        base(); ImageView im=new ImageView(this); im.setImageResource(com.nextrastore.app.R.drawable.nextra_logo);
        im.setAdjustViewBounds(true); box.addView(im,new LinearLayout.LayoutParams(-1,260));
        box.addView(title("Nextra Store"));
        EditText u=new EditText(this); u.setHint("Username / Key"); box.addView(u);
        EditText p=new EditText(this); p.setHint("Password (Admin only)"); p.setInputType(129); box.addView(p);
        Button login=btn("เข้าสู่ระบบ"); box.addView(login);
        login.setOnClickListener(v->{
            String user=u.getText().toString().trim(), pass=p.getText().toString();
            if(adminUser.equals(user)&&adminPass.equals(pass)){ showAdmin(); return; }
            if(prefs.contains("key_"+user)){
                long exp=prefs.getLong("key_"+user,0);
                if(exp>System.currentTimeMillis()) toast("เข้าสู่ระบบสำเร็จ • หมดอายุ "+new Date(exp));
                else toast("คีย์หมดอายุแล้ว");
            } else toast("ไม่พบคีย์");
        });
    }

    void showAdmin(){
        base(); box.addView(title("Nextra Store • Admin Panel"));
        Button create=btn("สร้างคีย์"); box.addView(create);
        Button list=btn("รายการคีย์ / ชดเชยคีย์"); box.addView(list);
        Button logout=btn("ออกจากระบบ"); box.addView(logout);
        create.setOnClickListener(v->createKey());
        list.setOnClickListener(v->listKeys());
        logout.setOnClickListener(v->showLogin());
    }

    void createKey(){
        final EditText days=new EditText(this); days.setHint("อายุคีย์ (วัน)");
        new AlertDialog.Builder(this).setTitle("สร้างคีย์").setView(days)
          .setPositiveButton("สร้าง", (d,w)->{
            int n; try{n=Integer.parseInt(days.getText().toString());}catch(Exception e){n=1;}
            String key="NEX-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
            prefs.edit().putLong("key_"+key,System.currentTimeMillis()+n*86400000L).apply();
            new AlertDialog.Builder(this).setTitle("สร้างสำเร็จ").setMessage(key+"\nอายุ "+n+" วัน")
              .setPositiveButton("ตกลง",null).show();
          }).setNegativeButton("ยกเลิก",null).show();
    }

    void listKeys(){
        base(); box.addView(title("Key Management"));
        Map<String,?> all=prefs.getAll(); boolean found=false;
        for(String k:all.keySet()) if(k.startsWith("key_")){
            found=true; String key=k.substring(4); long exp=prefs.getLong(k,0);
            LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL);
            TextView t=new TextView(this); t.setText(key+"\\nหมดอายุ: "+new Date(exp)); t.setTextSize(16);
            row.addView(t);
            Button extend=btn("ชดเชย +7 วัน"); row.addView(extend);
            Button revoke=btn("ยกเลิกคีย์"); row.addView(revoke); box.addView(row);
            extend.setOnClickListener(v->{long e=Math.max(exp,System.currentTimeMillis())+7*86400000L;
                prefs.edit().putLong(k,e).apply(); listKeys();});
            revoke.setOnClickListener(v->{prefs.edit().remove(k).apply(); listKeys();});
        }
        if(!found) box.addView(title("ยังไม่มีคีย์"));
        Button back=btn("กลับ Admin"); box.addView(back); back.setOnClickListener(v->showAdmin());
    }

    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}

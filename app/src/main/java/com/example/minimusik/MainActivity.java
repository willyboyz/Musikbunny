package com.example.minimusik;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.*;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_AUDIO=10;
    private MediaPlayer player;
    private TextView song,status,elapsed,duration;
    private Button play;
    private SeekBar seek;
    private final Handler handler=new Handler();
    private boolean seeking=false;

    private final Runnable updater=new Runnable(){
        @Override public void run(){
            if(player!=null && player.isPlaying() && !seeking){
                seek.setProgress(player.getCurrentPosition());
                elapsed.setText(time(player.getCurrentPosition()));
            }
            handler.postDelayed(this,400);
        }
    };

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(24),dp(32),dp(24),dp(24));
        page.setBackgroundColor(Color.rgb(17,20,28));

        TextView brand=text("MINI MUSIK",13,Color.rgb(160,176,202),true);
        brand.setLetterSpacing(.16f);
        page.addView(brand,match());

        TextView art=text("♫",90,Color.rgb(220,230,255),true);
        art.setGravity(Gravity.CENTER);
        art.setBackgroundColor(Color.rgb(36,45,64));
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(220));
        ap.setMargins(0,dp(34),0,dp(26));
        page.addView(art,ap);

        song=text("Belum ada lagu",22,Color.WHITE,true);
        song.setGravity(Gravity.CENTER);
        page.addView(song,match());
        status=text("Pilih file musik dari perangkat",14,Color.LTGRAY,false);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp=match(); sp.setMargins(0,dp(8),0,dp(20)); page.addView(status,sp);

        seek=new SeekBar(this); seek.setMax(1000); page.addView(seek,match());
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){}
            public void onStartTrackingTouch(SeekBar s){seeking=true;}
            public void onStopTrackingTouch(SeekBar s){if(player!=null)player.seekTo(s.getProgress());seeking=false;}
        });
        LinearLayout times=new LinearLayout(this);
        elapsed=text("0:00",12,Color.LTGRAY,false);
        duration=text("0:00",12,Color.LTGRAY,false);
        times.addView(elapsed,new LinearLayout.LayoutParams(0,-2,1));
        duration.setGravity(Gravity.RIGHT);
        times.addView(duration,new LinearLayout.LayoutParams(0,-2,1));
        page.addView(times);

        LinearLayout controls=new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp=match(); cp.setMargins(0,dp(26),0,dp(16)); page.addView(controls,cp);
        play=button("▶ PUTAR"); play.setEnabled(false);
        controls.addView(play,new LinearLayout.LayoutParams(dp(145),dp(52)));
        play.setOnClickListener(v->toggle());
        Button stop=button("■ STOP");
        LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(dp(110),dp(52)); stp.setMargins(dp(10),0,0,0); controls.addView(stop,stp);
        stop.setOnClickListener(v->stop());

        Button pick=button("PILIH LAGU");
        page.addView(pick,match());
        pick.setOnClickListener(v->choose());
        setContentView(page);
        handler.post(updater);
    }

    private void choose(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("audio/*");
        startActivityForResult(i,PICK_AUDIO);
    }
    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d);
        if(r==PICK_AUDIO && c==RESULT_OK && d!=null && d.getData()!=null) load(d.getData());
    }
    private void load(Uri uri){
        release();
        try{
            player=new MediaPlayer();
            player.setDataSource(this,uri);
            player.setOnPreparedListener(mp->{
                seek.setMax(mp.getDuration()); seek.setProgress(0);
                duration.setText(time(mp.getDuration())); elapsed.setText("0:00");
                String name=uri.getLastPathSegment();
                song.setText(name==null?"Lagu pilihan":name.substring(name.lastIndexOf('/')+1));
                status.setText("Siap diputar"); play.setEnabled(true); play.setText("▶ PUTAR");
            });
            player.setOnCompletionListener(mp->{play.setText("▶ PUTAR");seek.setProgress(0);elapsed.setText("0:00");});
            player.setOnErrorListener((mp,w,e)->{status.setText("File tidak bisa diputar");play.setEnabled(false);return true;});
            status.setText("Memuat lagu..."); player.prepareAsync();
        }catch(IOException|IllegalStateException ex){status.setText("Gagal membuka file audio");release();}
    }
    private void toggle(){
        if(player==null)return;
        if(player.isPlaying()){player.pause();play.setText("▶ LANJUT");status.setText("Dijeda");}
        else{player.start();play.setText("Ⅱ JEDA");status.setText("Sedang diputar");}
    }
    private void stop(){
        if(player!=null){player.pause();player.seekTo(0);seek.setProgress(0);elapsed.setText("0:00");play.setText("▶ PUTAR");status.setText("Dihentikan");}
    }
    private void release(){if(player!=null){try{player.release();}catch(Exception ignored){}player=null;}}
    private TextView text(String s,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(null,Typeface.BOLD);return t;
    }
    private Button button(String s){
        Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setTextColor(Color.WHITE);b.setAllCaps(false);
        b.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(65,102,176)));return b;
    }
    private LinearLayout.LayoutParams match(){return new LinearLayout.LayoutParams(-1,-2);}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private String time(int ms){int s=Math.max(0,ms/1000);return String.format(Locale.getDefault(),"%d:%02d",s/60,s%60);}
    @Override protected void onDestroy(){handler.removeCallbacks(updater);release();super.onDestroy();}
}

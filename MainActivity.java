package com.example.denzazdnem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(247, 249, 252);
    private static final int INK = Color.rgb(32, 44, 62);
    private static final int MUTED = Color.rgb(111, 122, 141);
    private static final int BLUE = Color.rgb(64, 102, 222);
    private static final int LIGHT_BLUE = Color.rgb(233, 239, 255);
    private static final int GREEN = Color.rgb(29, 140, 95);
    private SharedPreferences prefs;
    private JSONArray tasks = new JSONArray();
    private JSONArray meds = new JSONArray();
    private LinearLayout root, content, tabBar;
    private boolean showMeds = false;
    private String currentDay = "";
    private final Locale ru = new Locale("ru", "RU");

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        prefs = getSharedPreferences("day_by_day_data", MODE_PRIVATE);
        try { tasks = new JSONArray(prefs.getString("tasks", "[]")); } catch (Exception ignored) { tasks = new JSONArray(); }
        try { meds = new JSONArray(prefs.getString("meds", "[]")); } catch (Exception ignored) { meds = new JSONArray(); }
        buildShell();
        render();
    }
    @Override public void onResume() { super.onResume(); if (root != null && !today().equals(currentDay)) render(); }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()); }
    private String prettyDate(String iso) {
        try { return new SimpleDateFormat("d MMMM yyyy", ru).format(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)); }
        catch (Exception e) { return iso; }
    }
    private String plannedEnd(String start, int days) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(start);
            Calendar c = Calendar.getInstance(); c.setTime(d); c.add(Calendar.DAY_OF_YEAR, Math.max(1, days) - 1);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
        } catch(Exception e) { return start; }
    }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextColor(color); t.setTextSize(size);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout.LayoutParams lp(int width, int height) { return new LinearLayout.LayoutParams(width < 0 ? width : dp(width), height < 0 ? height : dp(height)); }
    private void space(LinearLayout parent, int h) { View v = new View(this); parent.addView(v, lp(1, h)); }
    private Button button(String s, boolean primary) {
        Button b = new Button(this); b.setText(s); b.setTextSize(14); b.setAllCaps(false);
        b.setTextColor(primary ? Color.WHITE : BLUE); b.setBackground(bg(primary ? BLUE : LIGHT_BLUE, 14));
        b.setPadding(dp(11), 0, dp(11), 0); b.setMinimumHeight(0); b.setMinHeight(0); return b;
    }
    private LinearLayout card() {
        LinearLayout c = column(); c.setPadding(dp(16), dp(15), dp(16), dp(15)); c.setBackground(bg(Color.WHITE, 18));
        c.setElevation(dp(1)); return c;
    }
    private void buildShell() {
        root = column(); root.setBackgroundColor(BG);
        root.setPadding(dp(18), dp(18), dp(18), 0);
        setContentView(root);
        TextView app = text("День за днём", 27, INK, true); root.addView(app);
        space(root, 5);
        TextView subtitle = text("Мои привычки и курсы приёма", 14, MUTED, false); root.addView(subtitle);
        space(root, 18);
        tabBar = row(); tabBar.setBackground(bg(LIGHT_BLUE, 16)); tabBar.setPadding(dp(4), dp(4), dp(4), dp(4));
        root.addView(tabBar, lp(-1, 52));
        space(root, 16);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        content = column(); content.setPadding(dp(1), dp(1), dp(1), dp(24));
        scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }
    private void render() {
        currentDay = today(); tabBar.removeAllViews(); content.removeAllViews();
        addTab("✓  Дела", false); addTab("＋  Таблеточница", true);
        if (showMeds) renderMeds(); else renderTasks();
    }
    private void addTab(String s, boolean medsTab) {
        Button b = button(s, showMeds == medsTab);
        if (showMeds != medsTab) b.setBackground(bg(LIGHT_BLUE, 13));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -1, 1); p.setMargins(dp(1), 0, dp(1), 0);
        tabBar.addView(b, p); b.setOnClickListener(v -> { showMeds = medsTab; render(); });
    }
    private void heading(String title, String subtitle) {
        content.addView(text(title, 23, INK, true)); space(content, 5);
        content.addView(text(subtitle, 13, MUTED, false)); space(content, 18);
    }
    private void renderTasks() {
        int done = 0;
        for (int i=0;i<tasks.length();i++) if (isDone(tasks.optJSONObject(i), currentDay)) done++;
        heading("Сегодня · " + prettyDate(currentDay), "Выполнено " + done + " из " + tasks.length() + " · каждый день галочки новые");
        Button add = button("+  Добавить дело", true); content.addView(add, lp(-1, 50));
        add.setOnClickListener(v -> editTask(-1)); space(content, 15);
        if (tasks.length()==0) { empty("Пока нет дел", "Добавьте постоянные пункты. На следующий день они останутся, а галочки обновятся."); return; }
        for (int i=0;i<tasks.length();i++) {
            final int index=i; JSONObject task=tasks.optJSONObject(i); if(task==null)continue;
            LinearLayout c=card(); LinearLayout r=row();
            CheckBox cb=new CheckBox(this); cb.setButtonTintList(android.content.res.ColorStateList.valueOf(BLUE));
            cb.setChecked(isDone(task, currentDay));
            cb.setText(task.optString("name", "Дело")); cb.setTextSize(17); cb.setTextColor(INK);
            r.addView(cb, new LinearLayout.LayoutParams(0, -2, 1));
            TextView edit=text("⋮", 27, MUTED, true); edit.setGravity(Gravity.CENTER); r.addView(edit, lp(39, 44));
            c.addView(r); content.addView(c, lp(-1, -2)); space(content, 10);
            cb.setOnCheckedChangeListener((view, checked) -> {
                try { JSONObject checks=task.optJSONObject("checks"); if(checks==null)checks=new JSONObject();
                    if(checked) checks.put(currentDay,true); else checks.remove(currentDay);
                    task.put("checks",checks); save();
                } catch(Exception ignored) {} render();
            });
            edit.setOnClickListener(v -> editTask(index));
            c.setOnLongClickListener(v -> { editTask(index); return true; });
        }
        space(content, 5); content.addView(text("Нажмите ⋮ для изменения или удаления. Отметки прошедших дней сохраняются.", 12, MUTED, false));
    }
    private boolean isDone(JSONObject task, String day) { JSONObject checks=task==null?null:task.optJSONObject("checks"); return checks!=null && checks.optBoolean(day,false); }
    private void editTask(int index) {
        EditText input=new EditText(this); input.setSingleLine(true); input.setTextSize(17);
        input.setHint("Например, зарядка");
        if(index>=0)input.setText(tasks.optJSONObject(index).optString("name"));
        LinearLayout wrapper=column(); wrapper.setPadding(dp(22),dp(5),dp(22),0); wrapper.addView(input);
        AlertDialog.Builder dlg=new AlertDialog.Builder(this).setTitle(index<0?"Новое дело":"Изменить дело").setView(wrapper)
           .setPositiveButton("Сохранить",(d,w)->{
               String name=input.getText().toString().trim(); if(name.isEmpty()){toast("Введите название");return;}
               try { if(index>=0) tasks.optJSONObject(index).put("name",name);
                   else { JSONObject task=new JSONObject(); task.put("id",UUID.randomUUID().toString());task.put("name",name);task.put("checks",new JSONObject());tasks.put(task); }
                   save();render();
               }catch(Exception ignored){}
           }).setNegativeButton("Отмена",null);
        if(index>=0) dlg.setNeutralButton("Удалить",(d,w)-> confirmDelete(false,index));
        dlg.show();
    }
    private void renderMeds() {
        heading("Таблеточница", "Счётчик дней приёма и плановая дата окончания");
        Button add=button("+  Добавить препарат",true); content.addView(add,lp(-1,50));
        add.setOnClickListener(v -> editMed(-1)); space(content,15);
        if(meds.length()==0){empty("Список лекарств пуст", "Добавьте название, дату начала и длительность курса. Затем меняйте счётчик кнопками + и −.");return;}
        for(int i=0;i<meds.length();i++){
            final int index=i; JSONObject med=meds.optJSONObject(i); if(med==null)continue;
            String name=med.optString("name","Препарат"); int count=Math.max(0,med.optInt("count",0));
            int total=Math.max(1,med.optInt("duration",1)); String start=med.optString("start",today());
            LinearLayout c=card(); LinearLayout top=row();
            TextView title=text(name,18,INK,true); top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
            Button edit=button("Изменить",false); top.addView(edit,lp(105,39));c.addView(top);space(c,11);
            c.addView(text("Отмечено дней: " + count + " из " + total,15,MUTED,false));space(c,11);
            LinearLayout controls=row();
            Button minus=button("−",false);minus.setTextSize(23);controls.addView(minus,lp(76,48));
            TextView number=text(String.valueOf(count),25,INK,true);number.setGravity(Gravity.CENTER);
            controls.addView(number,new LinearLayout.LayoutParams(0,dp(48),1));
            Button plus=button("+",true);plus.setTextSize(23);controls.addView(plus,lp(76,48));
            c.addView(controls);space(c,12);
            TextView date=text("Последний день по плану: " + prettyDate(plannedEnd(start,total)),14,BLUE,true);
            c.addView(date);space(c,5);
            c.addView(text("Начало: " + prettyDate(start) + " · курс: " + total + " дн.",12,MUTED,false));
            if(count>=total){space(c,7);c.addView(text("✓ Отмечено дней не меньше длительности курса",12,GREEN,true));}
            content.addView(c,lp(-1,-2));space(content,12);
            plus.setOnClickListener(v -> {try{med.put("count",med.optInt("count",0)+1);save();render();}catch(Exception ignored){}});
            minus.setOnClickListener(v -> {try{med.put("count",Math.max(0,med.optInt("count",0)-1));save();render();}catch(Exception ignored){}});
            edit.setOnClickListener(v -> editMed(index));
        }
        content.addView(text("Дата окончания рассчитывается от начала курса. Кнопки + и − меняют только счётчик — не дату. При пропусках уточняйте срок у врача.",12,MUTED,false));
    }
    private void editMed(int index) {
        JSONObject old=index>=0?meds.optJSONObject(index):null;
        final String[] start={old==null?today():old.optString("start",today())};
        LinearLayout form=column();form.setPadding(dp(22),dp(4),dp(22),0);
        form.addView(text("Название препарата",13,MUTED,false));
        EditText name=new EditText(this);name.setSingleLine(true);name.setHint("Название");
        if(old!=null)name.setText(old.optString("name"));form.addView(name);space(form,13);
        form.addView(text("Длительность курса, дней",13,MUTED,false));
        EditText duration=new EditText(this);duration.setSingleLine(true);duration.setInputType(InputType.TYPE_CLASS_NUMBER);duration.setHint("Например, 30");
        if(old!=null)duration.setText(String.valueOf(old.optInt("duration",1)));form.addView(duration);space(form,12);
        form.addView(text("Дата начала курса",13,MUTED,false));space(form,5);
        Button dateButton=button(prettyDate(start[0]),false);form.addView(dateButton,lp(-1,46));
        dateButton.setOnClickListener(v -> {
            Calendar c=Calendar.getInstance();try {c.setTime(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(start[0]));}catch(Exception ignored){}
            new DatePickerDialog(this,(picker,year,month,day)->{
                start[0]=String.format(Locale.US,"%04d-%02d-%02d",year,month+1,day);
                dateButton.setText(prettyDate(start[0]));
            },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
        });
        AlertDialog.Builder dlg=new AlertDialog.Builder(this).setTitle(index<0?"Добавить препарат":"Настройки препарата").setView(form)
          .setPositiveButton("Сохранить",(d,w)->{
              String n=name.getText().toString().trim();int days=0;
              try{days=Integer.parseInt(duration.getText().toString().trim());}catch(Exception ignored){}
              if(n.isEmpty()||days<1||days>10000){toast("Введите название и длительность от 1 до 10000 дней");return;}
              try{
                  JSONObject m=old==null?new JSONObject():old;
                  m.put("name",n);m.put("duration",days);m.put("start",start[0]);
                  if(old==null){m.put("count",0);m.put("id",UUID.randomUUID().toString());meds.put(m);}
                  save();render();
              }catch(Exception ignored){}
          }).setNegativeButton("Отмена",null);
        if(index>=0)dlg.setNeutralButton("Удалить",(d,w)->confirmDelete(true,index));
        dlg.show();
    }
    private void confirmDelete(boolean medication,int index){
        new AlertDialog.Builder(this).setTitle("Удалить запись?").setMessage("Это действие нельзя отменить.")
         .setPositiveButton("Удалить",(d,w)->{JSONArray source=medication?meds:tasks;JSONArray result=new JSONArray();
             for(int i=0;i<source.length();i++)if(i!=index)result.put(source.opt(i));
             if(medication)meds=result;else tasks=result;save();render();
         }).setNegativeButton("Отмена",null).show();
    }
    private void empty(String name,String hint){
        LinearLayout c=card();space(c,9);c.addView(text(name,19,INK,true));space(c,8);
        c.addView(text(hint,15,MUTED,false));space(c,9);content.addView(c,lp(-1,-2));
    }
    private void save(){prefs.edit().putString("tasks",tasks.toString()).putString("meds",meds.toString()).apply();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}

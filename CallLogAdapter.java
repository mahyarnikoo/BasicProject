package CustomAdapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.lang.UCharacter;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.CallLog;

import com.android.apliman.dialerapp.Activities.HomeActivity;
import com.android.apliman.dialerapp.Activities.MainActivity;
import com.android.apliman.dialerapp.Constants;
import com.android.apliman.dialerapp.Languages.English;
import com.android.apliman.dialerapp.Languages.Farsi;
import com.android.apliman.dialerapp.PublicFunctions;
import com.android.apliman.dialerapp.R;
import com.android.apliman.dialerapp.SmsSendObserver;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.awt.font.NumericShaper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import HTTP.Post_async;
import Interfaces.OnTaskCompleted;
import Models.LogItem;
import Models.VN;
import enums.Pages;
import enums.PostType;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by apliman on 1/19/17.
 */

public class CallLogAdapter extends BaseAdapter {

    ArrayList<LogItem> result;
    Context context;
    Activity activity;
    private static LayoutInflater inflater = null;
    String sms_text;
    String type;

    public CallLogAdapter(ArrayList<LogItem> result, Context context, Activity activity) {
        this.result = result;
        this.context = context;
        this.activity = activity;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public final class Holder {
        TextView phoneNumber;
        ImageView callType;
        TextView callTime;
        TextView callDate;
        TextView usedVN;
        TextView callDuration;
        GridLayout comm_grid;
        ImageView vn_call;
        ImageView vn_sms;
        LinearLayout call_details_layoyt;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        SharedPreferences sharedpreferences = context.getSharedPreferences("VN_preferences"
                , context.MODE_PRIVATE);

        final int call_type = Integer.valueOf(result.get(i).getCallType());
        final String call_duration = result.get(i).getCallDuration();
        final String PN = sharedpreferences.getString("PN", "");
        final String language = sharedpreferences.getString("Language", "");
        final Holder holder = new Holder();
        final View rowView;
        rowView = inflater.inflate(R.layout.call_log_item, null);

        String name = result.get(i).getName();
        final String number = result.get(i).getPhoneNumber();

        holder.phoneNumber = (TextView) rowView.findViewById(R.id.phone_number);

        if (result.get(i).getName() == null || result.get(i).getName().equals("")) {
            if (result.get(i).getPhoneNumber().startsWith(Constants.VNShortCode)) {
                if (language.equals("Farsi"))
                    holder.phoneNumber.setText(result.get(i).getPhoneNumber()
                            .substring(4, result.get(i).getPhoneNumber().length()));
                else
                    holder.phoneNumber.setText(result.get(i).getPhoneNumber()
                            .substring(4, result.get(i).getPhoneNumber().length()));
            } else {
                if (language.equals("Farsi"))
                    holder.phoneNumber.setText(result.get(i).getPhoneNumber());
                else
                    holder.phoneNumber.setText(result.get(i).getPhoneNumber());
            }
        } else if (result.get(i).getName().startsWith("number")) {
            if (language.equals("Farsi"))
                holder.phoneNumber.setText(result.get(i).getPhoneNumber());
            else
                holder.phoneNumber.setText(result.get(i).getPhoneNumber());
        } else {
            holder.phoneNumber.setTag(result.get(i).getPhoneNumber());
            holder.phoneNumber.setText(result.get(i).getName());
        }

        holder.usedVN = (TextView) rowView.findViewById(R.id.used_vn);
        holder.callType = (ImageView) rowView.findViewById(R.id.call_type);

        switch (Integer.valueOf(result.get(i).getCallType())) {
            case CallLog.Calls.OUTGOING_TYPE:
                holder.callType.setImageResource(R.mipmap.outgoing);
                break;
            case CallLog.Calls.INCOMING_TYPE:
                holder.callType.setImageResource(R.mipmap.incoming);
                if (!result.get(i).getPhoneNumber().startsWith(Constants.VNShortCode)) {
                    holder.usedVN.setVisibility(View.INVISIBLE);
                }
                break;
            case CallLog.Calls.MISSED_TYPE:
                holder.callType.setImageResource(R.mipmap.missed);
                if (!result.get(i).getPhoneNumber().startsWith(Constants.VNShortCode)) {
                    holder.usedVN.setVisibility(View.INVISIBLE);
                }
                break;
        }

        holder.callTime = (TextView) rowView.findViewById(R.id.call_time);
        SimpleDateFormat df = new SimpleDateFormat("KK:mm aa");
        Date date = result.get(i).getCallDayTime();

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        holder.callTime.setText(df.format(date));

        holder.callDate = (TextView) rowView.findViewById(R.id.call_date);
        df = new SimpleDateFormat("yyyy/MM/dd");
        holder.callDate.setText(df.format(date));

        if (DateUtils.isToday(date.getTime())) {
            holder.callDate.setText("Today");
            //DateUtils.getRelativeTimeSpanString(date.getTime());
        } else if (DateUtils.isToday(date.getTime() + DateUtils.DAY_IN_MILLIS)) {
            holder.callDate.setText("Yesterday");
        }

        String dialedNumber = result.get(i).getPhoneNumber();
        String vn = "";
        String from = "";

        if (dialedNumber.startsWith(Constants.VNShortCode)) {
            if (language.equals("Farsi")) {
                holder.usedVN.setText(Farsi.from + ": " + Farsi.virtual_number + " " + PublicFunctions.convertNumberToFarsi(dialedNumber.charAt(3)));
            } else {
                holder.usedVN.setText(English.from + ": " + English.virtual_number + " " + dialedNumber.charAt(3));
            }

        } else {
            if (language.equals("Farsi")) {
                holder.usedVN.setText(Farsi.from + ": " + Farsi.real_number);
            } else {
                holder.usedVN.setText(English.from + ":  Real Number");
            }

        }

        holder.comm_grid = (GridLayout) rowView.findViewById(R.id.comm_grid);
        holder.call_details_layoyt = (LinearLayout) rowView.findViewById(R.id.call_details);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mahyar

                PublicFunctions.setVN_preferencesItem(context.getApplicationContext()
                        , "activePage", Pages.CallDetail.toString());

                Intent intent = new Intent(context.getApplicationContext()
                        , HomeActivity.class);
               /* intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                        |Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
                intent.putExtra("status", "CallDetail");
                if (holder.phoneNumber.getTag() == null){
                    intent.putExtra("phoneNumber", holder.phoneNumber.getText().toString());
                }
                else{
                    intent.putExtra("phoneNumber", holder.phoneNumber.getTag().toString());
                }

                /*intent.putExtra("usedVN", holder.usedVN.getText());
                intent.putExtra("callTime", holder.callTime.getText());
                intent.putExtra("callType", call_type);
                intent.putExtra("callDuration", call_duration);*/
                context.startActivity(intent);

                /*if (holder.call_details_layoyt.getVisibility() == View.VISIBLE) {
                    holder.call_details_layoyt.setVisibility(View.GONE);
                    holder.callDuration.setVisibility(View.GONE);
                } else {
                    holder.call_details_layoyt.setVisibility(View.VISIBLE);
                    holder.callDuration.setVisibility(View.VISIBLE);
                }*/
            }
        });
        holder.vn_call = (ImageView) rowView.findViewById(R.id.vn_call);
        holder.vn_sms = (ImageView) rowView.findViewById(R.id.sms_vn);

        holder.vn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                //PopupMenu popup = new PopupMenu(context, holder.vn_call);
                @SuppressLint("RestrictedApi") ContextWrapper wrapper =
                        new ContextThemeWrapper(activity, R.style.popupMenuStyle);
                PopupMenu popup = new PopupMenu(wrapper, holder.vn_call);
                //Inflating the Popup using xml file
                String vn;
                String real;
                if (language.equals("Farsi")) {
                    vn = Farsi.vn;
                    real = Farsi.real;
                } else {
                    vn = English.vn;
                    real = English.real;
                }
                popup.getMenu().add(0, -1, 0, real + ": " + PN);
                SpannableString s = new SpannableString(popup.getMenu().getItem(0).getTitle());
                s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
                popup.getMenu().getItem(0).setTitle(s);
                for (int i = 0; i < MainActivity.VNs.size(); i++) {
                    if (MainActivity.VNs.get(i).getStatus().equals("Enabled Allowed")) {
                        popup.getMenu().add(0, i, 0, vn + MainActivity.VNs.get(i).getID() + ": " + MainActivity.VNs.get(i).getVN());
                        int count = 0;
                        for (int j = 0; j <= i; j++) {
                            if (!MainActivity.VNs.get(j).getStatus().equals("Enabled Allowed"))
                                count += 1;
                        }
                        s = new SpannableString(popup.getMenu().getItem(i + 1 - count).getTitle());
                        s.setSpan(new ForegroundColorSpan(Color.parseColor("#993c4043")), 0, s.length(), 0);
                        popup.getMenu().getItem(i + 1 - count).setTitle(s);
                    }
                }
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        String numberToCall = number;
                        String virtualNumber = "";
                        int id = item.getItemId();
                        if (number.startsWith(Constants.VNShortCode)) {
                            numberToCall = number.substring(Constants.VNShortCode.length() + 1);
                            String usedVnId = number.substring(Constants.VNShortCode.length(), Constants.VNShortCode.length() + 1);
                            for (VN v : MainActivity.VNs) {
                                if (v.getID().equals(usedVnId)) {
                                    virtualNumber = v.getVN();
                                }
                            }
                        }
                        numberToCall = PublicFunctions.fixNumber(numberToCall);
                        if (id == -1) {
//                                if (number.startsWith(Constants.VNShortCode)){}
//                                    //numberToCall = Constants.sho number.substring(Constants.VNShortCode.length()+1).toString();
//                                else
//                                    numberToCall = number.toString();
                        } else {
                            //if (number.startsWith(Constants.VNShortCode))
                            numberToCall = Constants.VNShortCode + MainActivity.VNs.get(id).getID() + numberToCall;
//                                else
//                                    numberToCall = Constants.VNShortCode+item.getTitle().subSequence(2,3)+number;

                        }

                        //
                        SharedPreferences sharedpreferences = activity.getSharedPreferences("VN_preferences", Context.MODE_PRIVATE);
                        String PN = sharedpreferences.getString("PN", "");
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("Source", "mobileapp");
                            jsonBody.put("PN", PN);
                            jsonBody.put("VN", virtualNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Post_async makeCall = new Post_async(Constants.Web_Service_URL + Constants.makeCall, jsonBody.toString(), PostType.MakeCall, activity, activity, "keypad", new OnTaskCompleted() {
                            @Override
                            public void onTaskCompleted(String response) {

                            }
                        });
                        try {
                            makeCall.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            //ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        } else {
                            //send make call api request before making the call
                            context.startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse(String.format("tel:%s", Uri.encode(numberToCall)))));
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });

        holder.vn_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                //PopupMenu popup = new PopupMenu(context, holder.vn_sms);
                @SuppressLint("RestrictedApi") ContextWrapper wrapper =
                        new ContextThemeWrapper(activity, R.style.popupMenuStyle);
                PopupMenu popup = new PopupMenu(wrapper, holder.vn_sms);
                //Inflating the Popup using xml file
                SharedPreferences sharedpreferences = context.getSharedPreferences("VN_preferences"
                        , context.MODE_PRIVATE);
                String PN = sharedpreferences.getString("PN", "");
                String language = sharedpreferences.getString("Language", "");
                String vn;
                String real;
                if (language.equals("Farsi")) {
                    vn = Farsi.vn;
                    real = Farsi.real;
                } else {
                    vn = English.vn;
                    real = English.real;
                }
                popup.getMenu().add(0, -1, 0, real + ": " + PN);
                SpannableString s = new SpannableString(popup.getMenu().getItem(0).getTitle());
                s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
                popup.getMenu().getItem(0).setTitle(s);
                for (int i = 0; i < MainActivity.VNs.size(); i++) {
                    if (MainActivity.VNs.get(i).getStatus().equals("Enabled Allowed")) {
                        popup.getMenu().add(0, i, 0, vn + MainActivity.VNs.get(i).getID() + ": " + MainActivity.VNs.get(i).getVN());
                        s = new SpannableString(popup.getMenu().getItem(i + 1).getTitle());
                        s.setSpan(new ForegroundColorSpan(Color.parseColor("#993c4043")), 0, s.length(), 0);
                        popup.getMenu().getItem(i + 1).setTitle(s);
                    }
                }
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        String numberToUse = number;
                        String virtualNumber = "";
                        int id = item.getItemId();
                        if (number.startsWith(Constants.VNShortCode)) {
                            numberToUse = number.substring(Constants.VNShortCode.length() + 1);
                            String usedVnId = number.substring(Constants.VNShortCode.length(), Constants.VNShortCode.length() + 1);
                            for (VN v : MainActivity.VNs) {
                                if (v.getID().equals(usedVnId)) {
                                    virtualNumber = v.getVN();
                                }
                            }
                        }
                        if (id == -1) {
                            //numberToUse = number;
                        } else {
                            numberToUse = Constants.VNShortCode + MainActivity.VNs.get(id).getID() + numberToUse;
                        }
                        MainActivity.smsSenderVN = virtualNumber;
                        new SmsSendObserver(activity, numberToUse, 1500000).start();
                        String uri = "smsto:" + numberToUse;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.putExtra("compose_mode", true);
                        intent.putExtra("exit_on_sent", true);
                        //intent.putExtra("sms_body", outCipherText);
                        context.startActivity(intent);
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });

        holder.callDuration = (TextView) rowView.findViewById(R.id.call_duration);
        long durationInt = Long.valueOf(result.get(i).getCallDuration());
        long hours = TimeUnit.SECONDS.toHours(durationInt);
        durationInt -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(durationInt);
        durationInt -= TimeUnit.MINUTES.toSeconds(minutes);
        long seconds = TimeUnit.SECONDS.toSeconds(durationInt);

        if (language.equals("Farsi")) {
            if (hours > 0) {
                holder.callDuration.setText(Farsi.call_duration + ": " + PublicFunctions.convertNumberToFarsi(hours) + " " + Farsi.hour + " " + PublicFunctions.convertNumberToFarsi(minutes) + " " + Farsi.minute + " " + PublicFunctions.convertNumberToFarsi(seconds) + " " + Farsi.second);
            } else if (minutes > 0) {
                holder.callDuration.setText(Farsi.call_duration + ": " + PublicFunctions.convertNumberToFarsi(minutes) + " " + Farsi.minute + " " + PublicFunctions.convertNumberToFarsi(seconds) + " " + Farsi.second);
            } else {
                holder.callDuration.setText(Farsi.call_duration + ": " + PublicFunctions.convertNumberToFarsi(seconds) + " " + Farsi.second);
            }
        } else {
            if (hours > 0) {
                holder.callDuration.setText(English.call_duration + ": " + hours + " " + English.hour + " " + minutes + " " + English.minute + " " + seconds + " " + English.second);
            } else if (minutes > 0) {
                holder.callDuration.setText(English.call_duration + ": " + minutes + " " + English.minute + " " + seconds + " " + English.second);
            } else {
                holder.callDuration.setText(English.call_duration + ": " + seconds + " " + English.second);
            }
        }
        if (language.equals("Farsi")) {
            rowView.setTextDirection(View.TEXT_DIRECTION_RTL);
            rowView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            holder.phoneNumber.setGravity(Gravity.RIGHT);
            holder.usedVN.setGravity(Gravity.RIGHT);
        } else {
            rowView.setTextDirection(View.TEXT_DIRECTION_LTR);
            rowView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            holder.phoneNumber.setGravity(Gravity.LEFT);
            holder.usedVN.setGravity(Gravity.LEFT);
        }

        return rowView;
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            phoneNo = PublicFunctions.fixNumber(phoneNo);

            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS, phoneNo, sms_text}, 2);
            } else {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, msg, null, null);
                SharedPreferences sharedpreferences = activity.getSharedPreferences("VN_preferences", Context.MODE_PRIVATE);
                final String language = sharedpreferences.getString("Language", "");
                if (language.equals("Farsi")) {
                    Toast.makeText(activity, Farsi.message_sent, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, English.message_sent, Toast.LENGTH_LONG).show();
                }
                sms_text = "";
            }
        } catch (Exception ex) {

            ex.printStackTrace();
            sms_text = "";
        }
    }


}

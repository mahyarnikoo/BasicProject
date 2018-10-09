package CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.apliman.dialerapp.Constants;
import com.android.apliman.dialerapp.Fragments.AvailableBanks;
import com.android.apliman.dialerapp.Languages.English;
import com.android.apliman.dialerapp.Languages.Farsi;
import com.android.apliman.dialerapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.SocketHandler;

import HTTP.Post_async;
import Interfaces.OnTaskCompleted;
import Models.Bank;
import Models.LogItem;
import enums.PostType;

/**
 * Created by apliman on 4/24/17.
 */

public class AvailableBanksAdapter extends BaseAdapter {

    ArrayList<Bank> result;
    Context context;
    Activity activity;
    private static LayoutInflater inflater=null;

    public AvailableBanksAdapter(ArrayList<Bank> result, Context context, Activity activity){
        this.result = result;
        this.context = context;
        this.activity = activity;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public final class Holder{
        ImageView bankLogo;
        TextView bankName;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final AvailableBanksAdapter.Holder holder = new AvailableBanksAdapter.Holder();
        final View rowView;
        rowView = inflater.inflate(R.layout.bank_item, null);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(activity,"bank name: "+result.get(position).getBankName(), Toast.LENGTH_SHORT).show();
                //apply bank payment
                final SharedPreferences sharedpreferences = activity.getSharedPreferences("VN_preferences", Context.MODE_PRIVATE);
                final String PN = sharedpreferences.getString("PN","");
                final String language = sharedpreferences.getString("Language","");
                final String paymentFailed;
                final String serverError;
                if (language.equals("Farsi")){
                    paymentFailed = Farsi.could_not_perform_payment_please_try_again;
                    serverError = Farsi.noInternetConnection;
                }
                else {
                    paymentFailed = English.could_not_perform_payment_please_try_again;
                    serverError = Farsi.noInternetConnection;
                }

                Bundle bundle = activity.getIntent().getExtras();
                String dueAmount = "";
                String referenceId = "";
                if(bundle!=null){
                    dueAmount = bundle.getString("dueAmount");
                    referenceId = bundle.getString("referenceId");
                }
                String bankID = result.get(position).getBankID();
                String clientTransactionID = sharedpreferences.getString("clientTransactionId","");
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("Source", "mobileapp");
                    jsonBody.put("PN", PN);
                    jsonBody.put("Amount", dueAmount);
                    jsonBody.put("Bank_ID", bankID);
                    jsonBody.put("Reference_ID", referenceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Post_async applyBankPayment = new Post_async(Constants.Web_Service_URL + Constants.applyBankPayment, jsonBody.toString(), PostType.ApplyBankPayment, activity, activity, "applyBankPayment", new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(String response) throws JSONException {
                        if (response != null) {
                            //get json and check if payment was successful or not to report to user
                            JSONObject reader = new JSONObject(response);
                            int error_code = Integer.valueOf(reader.getString("error_code"));
                            if (error_code == 0) {
                                //payment successful
                                //Toast.makeText(activity,"Payment successful",Toast.LENGTH_LONG).show();
                                String url = result.get(position).getUrl();
                                if (!url.startsWith("http://") && !url.startsWith("https://"))
                                    url = "http://" + url;
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                activity.startActivity(browserIntent);
                                activity.onBackPressed();
                            }
                            else {
                                //payment failed
                                Toast.makeText(activity, paymentFailed, Toast.LENGTH_LONG).show();
                                activity.onBackPressed();
                            }
                        }
                        else {
                            Toast.makeText(activity, serverError, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                applyBankPayment.execute();

                //                Toast.makeText(activity, "Payment failed, please try again5", Toast.LENGTH_LONG).show();
//                activity.onBackPressed();
            }
        });

        String name = result.get(position).getBankName();
        String bankIconURL = result.get(position).getBankIcon();
        holder.bankName = (TextView)rowView.findViewById(R.id.bank_name);
        holder.bankLogo = (ImageView)rowView.findViewById(R.id.bank_icon);

        holder.bankName.setText(name);
        //URL url = null;
        //url = new URL(bankIconURL);
        Bitmap bm = result.get(position).getBankIconBitmap();
        if (bm != null)
            holder.bankLogo.setImageBitmap(bm);
        //Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());



        return rowView;
    }
}

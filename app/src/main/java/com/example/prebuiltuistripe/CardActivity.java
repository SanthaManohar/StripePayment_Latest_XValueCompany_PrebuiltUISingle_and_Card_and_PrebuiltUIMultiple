package com.example.prebuiltuistripe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CardActivity extends AppCompatActivity {
    // 10.0.2.2 is the Android emulator's alias to localhost
//private static final String BACKEND_URL = "http://10.0.2.2:4242/";
    private static final String BACKEND_URL = "http://103.228.153.188:4242/";
//private static final String BACKEND_URL = "https://085e75b99982.ngrok.io/";
//private static final String BACKEND_URL = "https://stripe-mobile-payment-sheet.glitch.me/checkout";





    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;
    private Stripe stripe;

    Button payButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        // Configure the SDK with your Stripe publishable key so it can make requests to Stripe
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull("pk_test_51J06XvSFfCoQJakcROUGxijpmQBbZcwDdazn7wLXltCiQxykJD5ZczvlD78wGRPF8ksmgCQuSxJFYdd3z2LTkrMV00uA72IFko")
        );
        startCheckout();
    }

    private void startCheckout() {
        // Create a PaymentIntent by calling the server's endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
//        String json = "{" + "currency:usd," + "items:[" + "{id:photo_subscription}" + "]" + "}";
//        String json = "{"
//        + ""currency":"usd","
//        + ""items":["
//        + "{"id":"photo_subscription"}"
//        + "]"
//        + "}";
//    Map<String, String> map2 = new HashMap<>();
//    map2.put("id","photo_subscription");
//
//
//    Map<String, String> map1 = new HashMap<>();
//    map1.put("currency","usd");
//    map1.put("items",map2.toString());
//
//    String json = map1.toString();

//    List<Object> paymentMethodTypes =
//            new ArrayList<>();
//    paymentMethodTypes.add("card");
//    Map<String, Object> params1 = new HashMap<>();
//    params1.put("amount", 2000);
//    params1.put("currency", "usd");
//    params1.put(
//            "payment_method_types",
//            paymentMethodTypes
//    );
//
//    Gson gson = new Gson();
//    String json = gson.toJson(params1);

//        String json = "{}";


        Map<String, Object> payMap = new HashMap<>();
        Map<String, Object> itemMap = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();
        payMap.put("currency", "usd"); //dont change currency in testing phase otherwise it won't work
//        itemMap.put("id", "photo_subscription");
        itemMap.put("amount", "20");
        itemMap.put("type","card");
        itemList.add(itemMap);
        payMap.put("items", itemList);
        String json = new Gson().toJson(payMap);

        Log.d("json1:","json1:"+json);


        RequestBody body = RequestBody.create(mediaType,json);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();

        Log.d("url:","url:"+request);

        httpClient.newCall(request).enqueue(new PayCallback(this));

        // Hook up the pay button to the card widget and stripe instance
       payButton = findViewById(R.id.payButton);
//        payButton.setOnClickListener((View view) -> {
//            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
//            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
//            if (params != null) {
//                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
//                        .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
//                stripe.confirmPayment(this, confirmParams);
//            }
//        });
    }

    private void displayAlert(@NonNull String title,
                              @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);

        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Log.d("json2:","json2:"+response.body().string());

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(Objects.requireNonNull(response.body()).string(),type);

        paymentIntentClientSecret = responseMap.get("clientSecret");

        payButton.setOnClickListener((View view) -> {
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                stripe.confirmPayment(this, confirmParams);
            }
        });
    }

    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<CardActivity> activityRef;

        PayCallback(@NonNull CardActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final CardActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            Log.d("error1:","error1:"+e.toString());


            activity.runOnUiThread(() ->
                    Toast.makeText(
                            activity, "Error1: " + e.toString(), Toast.LENGTH_LONG
                    ).show()
            );

        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final CardActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(
                                activity, "Error2: " + response.toString(), Toast.LENGTH_LONG
                        ).show()
                );
            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<CardActivity> activityRef;

        PaymentResultCallback(@NonNull CardActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final CardActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert(
                        "Payment completed",
                        gson.toJson(paymentIntent)
                );
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage()
                );
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final CardActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error3", e.toString());
        }
    }
}
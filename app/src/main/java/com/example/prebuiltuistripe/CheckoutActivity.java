package com.example.prebuiltuistripe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CheckoutActivity extends AppCompatActivity {
//    private static final String BACKEND_URL = "https://stripe-mobile-payment-sheet.glitch.me/checkout";
    private static final String BACKEND_URL = "https://stripe-mobile-payment-sheet.glitch.me/checkout";
////    private static final String BACKEND_URL = "http://103.228.153.188:4242/checkout";
//    private static final String BACKEND_URL = "http://103.228.153.188:4242/";
//    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51J06XvSFfCoQJakcROUGxijpmQBbZcwDdazn7wLXltCiQxykJD5ZczvlD78wGRPF8ksmgCQuSxJFYdd3z2LTkrMV00uA72IFko";

//    private static final String BACKEND_URL = "https://carpal-helpful-buzzard.glitch.me/checkout";
//    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51IgG50SCImLsYbQXpjmzELWOKtp5LeHXqTNHm3kskaFpwsLseewyDnze1nuey4EtVZlGLzren2A8tXU2yihXuoIt00rrYQekGY";

    private PaymentSheet paymentSheet;

    private String paymentIntentClientSecret;
    private String customerId;
    private String ephemeralKeySecret;

    String publishablekey;

    private Button buyButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // instantiate view and buyButton

        buyButton = (Button)findViewById(R.id.paybutton);



        buyButton.setEnabled(false);

//        PaymentConfiguration.init(this, STRIPE_PUBLISHABLE_KEY);

        paymentSheet = new PaymentSheet(this, result -> {
            onPaymentSheetResult(result);
        });

//        buyButton.setOnClickListener(v -> presentPaymentSheet());

        fetchInitData();


    }

    private void fetchInitData() {
        Map<String, Object> payMap = new HashMap<>();
        Map<String, Object> itemMap = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();
        payMap.put("currency", "usd"); //dont change currency in testing phase otherwise it won't work
//        itemMap.put("id", "photo_subscription");
        itemMap.put("amount", "20");
        itemMap.put("type","card");
        itemList.add(itemMap);
        payMap.put("items", itemList);
        String requestJson = new Gson().toJson(payMap);

        Log.d("json1:","json1:"+requestJson);
//        final String requestJson = "{}";
        final RequestBody requestBody = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                requestJson
                );

//        final Request request = new Request.Builder()
//                .url(BACKEND_URL + "payment-sheet")
//                .post(requestBody)
//                .build();



        final Request request = new Request.Builder()
                .url(BACKEND_URL)
                .post(requestBody)
                .build();

        Log.d("url:","url:"+request);

        new OkHttpClient()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        // Handle failure
                    }

                    @Override
                    public void onResponse(
                            @NotNull Call call,
                            @NotNull Response response
                    ) throws IOException {
                        if (!response.isSuccessful()) {
                            // Handle failure
                        } else {
                            final JSONObject responseJson = parseResponse(response.body());

                            publishablekey = responseJson.optString("publishableKey");
                            paymentIntentClientSecret = responseJson.optString("paymentIntent");
                            customerId = responseJson.optString("customer");
                            ephemeralKeySecret = responseJson.optString("ephemeralKey");

                            Log.d("payment1:","publishablekey : "+publishablekey);
                            Log.d("payment1:","paymentIntentClientSecret : "+paymentIntentClientSecret);
                            Log.d("payment1:","customerId : "+customerId);
                            Log.d("payment1:","ephemeralKeySecret : "+ephemeralKeySecret);

                            PaymentConfiguration.init(CheckoutActivity.this, publishablekey);

                            runOnUiThread(() -> buyButton.setEnabled(true));

                            buyButton.setOnClickListener(v -> presentPaymentSheet(paymentIntentClientSecret,customerId,ephemeralKeySecret));
                        }
                    }
                });
    }

    private JSONObject parseResponse(ResponseBody responseBody) {
        if (responseBody != null) {
            try {
                return new JSONObject(responseBody.string());
            } catch (IOException | JSONException e) {
                Log.e("App", "Error parsing response", e);
            }
        }
        return new JSONObject();
    }

    private void presentPaymentSheet(String paymentIntentClientSecretone,String customerIdone,String ephemeralKeySecretone) {

        Log.d("payment11:","payment:"+paymentIntentClientSecretone);
        Log.d("payment11:","payment:"+customerIdone);
        Log.d("payment11:","payment:"+ephemeralKeySecretone);


        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecretone,
                new PaymentSheet.Configuration(
                        "Example, Inc.",
                        new PaymentSheet.CustomerConfiguration(
                                customerIdone,
                                ephemeralKeySecretone
                        )
                )
        );
    }

    private void onPaymentSheetResult(
            final PaymentSheetResult paymentSheetResult
    ) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(
                    this,
                    "Payment Canceled",
                    Toast.LENGTH_LONG
            ).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(
                    this,
                    "Payment Failed. See logcat for details.",
                    Toast.LENGTH_LONG
            ).show();

            Log.e("App", "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(
                    this,
                    "Payment Complete",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
package com.example.prebuiltuistripe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentOptionCallback;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.paymentsheet.PaymentSheetResultCallback;
import com.stripe.android.paymentsheet.model.PaymentOption;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PrebuiltMulti extends AppCompatActivity {
    private static final String BACKEND_URL = "https://stripe-mobile-payment-sheet.glitch.me/checkout";
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51J06XvSFfCoQJakcROUGxijpmQBbZcwDdazn7wLXltCiQxykJD5ZczvlD78wGRPF8ksmgCQuSxJFYdd3z2LTkrMV00uA72IFko";


    private PaymentSheet.FlowController flowController;
    private Button paymentMethodButton;
    private Button buyButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prebuilt_multi);

        // instantiate view and buttons

        // instantiate view and buyButton

        paymentMethodButton = (Button)findViewById(R.id.paybutton);



//        PaymentConfiguration.init(this, STRIPE_PUBLISHABLE_KEY);

        paymentMethodButton.setEnabled(false);

        final PaymentOptionCallback paymentOptionCallback = paymentOption -> {
            onPaymentOption(paymentOption);
        };

        final PaymentSheetResultCallback paymentSheetResultCallback = paymentSheetResult -> {
            onPaymentSheetResult(paymentSheetResult);
        };

        flowController = PaymentSheet.FlowController.create(
                this,
                paymentOptionCallback,
                paymentSheetResultCallback
        );

        fetchInitData();
    }

    private void fetchInitData() {
        final String requestJson = "{}";
        final RequestBody requestBody = RequestBody.create(
                requestJson,
                MediaType.get("application/json; charset=utf-8")
        );

//        final Request request = new Request.Builder()
//                .url(BACKEND_URL + "payment-sheet")
//                .post(requestBody)
//                .build();

        final Request request = new Request.Builder()
                .url(BACKEND_URL)
                .post(requestBody)
                .build();

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

                            final String publishablekey = responseJson.optString("publishableKey");
                            final String paymentIntentClientSecret = responseJson.optString("paymentIntent");
                            final String customerId = responseJson.optString("customer");
                            final String ephemeralKeySecret = responseJson.optString("ephemeralKey");

                            Log.d("payment1:","publishablekey : "+publishablekey);
                            Log.d("payment1:","paymentIntentClientSecret : "+paymentIntentClientSecret);
                            Log.d("payment1:","customerId : "+customerId);
                            Log.d("payment1:","ephemeralKeySecret : "+ephemeralKeySecret);

                            PaymentConfiguration.init(PrebuiltMulti.this, publishablekey);

                            configureFlowController(
                                    paymentIntentClientSecret,
                                    customerId,
                                    ephemeralKeySecret
                            );
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

    private void configureFlowController(
            String paymentIntentClientSecret,
            String customerId,
            String ephemeralKeySecret
    ) {
//        flowController.configure(
//                paymentIntentClientSecret,
//                new PaymentSheet.Configuration(
//                        "Example, Inc.",
//                        new PaymentSheet.CustomerConfiguration(
//                                customerId,
//                                ephemeralKeySecret
//                        )
//                ),
//                (success, error) -> {
//                    if (success) {
//                        onFlowControllerReady();
//                    } else {
//                        // handle FlowController configuration failure
//                    }
//                }
//        );

        flowController.configureWithPaymentIntent(
                paymentIntentClientSecret,
                new PaymentSheet.Configuration(
                        "Example, Inc.",
                        new PaymentSheet.CustomerConfiguration(
                                customerId,
                                ephemeralKeySecret
                        )
                ),
                (success, error) -> {
                    if (success) {
                        onFlowControllerReady();
                    } else {
                        // handle FlowController configuration failure
                    }
                }
        );
    }

    private void onFlowControllerReady() {
        paymentMethodButton.setOnClickListener(v -> flowController.presentPaymentOptions());
        buyButton.setOnClickListener(v -> onCheckout());
        paymentMethodButton.setEnabled(true);
        onPaymentOption(flowController.getPaymentOption());
    }

    private void onPaymentOption(
            @Nullable PaymentOption paymentOption
    ) {
        if (paymentOption != null) {
            paymentMethodButton.setText(paymentOption.getLabel());
            paymentMethodButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    paymentOption.getDrawableResourceId(),
                    0,
                    0,
                    0
            );
            buyButton.setEnabled(true);
        } else {
            paymentMethodButton.setText("Select");
            paymentMethodButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
            );
            buyButton.setEnabled(false);
        }
    }

    private void onCheckout() {
//        flowController.confirmPayment();
        flowController.confirm();
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
                    "Payment Failed. See logcat for details",
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
package com.example.prebuiltuistripe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class CardLatestActivity extends AppCompatActivity {

    private String paymentIntentClientSecret;
    private Stripe stripe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // ...
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_latest);
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51IgG50SCImLsYbQXpjmzELWOKtp5LeHXqTNHm3kskaFpwsLseewyDnze1nuey4EtVZlGLzren2A8tXU2yihXuoIt00rrYQekGY"
        );
        startCheckout();
    }

    private void startCheckout() {
        // Request a PaymentIntent from your server and store its client secret in paymentIntentClientSecret
        // Click Open on GitHub to see a full implementation
        // Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.payButton);
        CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
        payButton.setOnClickListener((View view) -> {
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                final Context context = getApplicationContext();
                stripe = new Stripe(
                        context,
                        PaymentConfiguration.getInstance(context).getPublishableKey()
                );
                stripe.confirmPayment(this, confirmParams);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull
        private final WeakReference<CardLatestActivity> activityRef;

        PaymentResultCallback(@NonNull CardLatestActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final CardLatestActivity activity = activityRef.get();
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
                        gson.toJson(paymentIntent),
                        true
                );
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage(),
                        false
                );
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final CardLatestActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString(), false);
        }
    }

    private void displayAlert(String error, String toString, boolean b) {
        AlertDialog alertDialog = new AlertDialog.Builder(CardLatestActivity.this).create();
        alertDialog.setTitle(error);
        alertDialog.setMessage(toString);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


}
package sinhala.novels.ebooks;

import static sinhala.novels.ebooks.MainActivity.mainActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.cyberyakku.carrierbillingsupporter.CarrierBillingSupporter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class BuyPremiumActivity extends AppCompatActivity {

    private BillingClient billingClient;
    private Button purchaseGoogle,purchaseDialog,purchaseHutch;
    private FirebaseFirestore firebaseFirestore;
    private SharedPreferences sharedPreferences;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_premium);

        sharedPreferences=getSharedPreferences("UserData",MODE_PRIVATE);
        purchaseGoogle=findViewById(R.id.purchaseGoogle);
        purchaseDialog=findViewById(R.id.purchaseDialog);
        purchaseHutch=findViewById(R.id.purchaseHutch);
        firebaseFirestore=FirebaseFirestore.getInstance();

        progressDialog=new Dialog(BuyPremiumActivity.this);
        progressDialog.setContentView(R.layout.authenticating_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView text=progressDialog.findViewById(R.id.text);
        text.setText("Please wait...");

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(
                        new PurchasesUpdatedListener() {
                            @Override
                            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                                        && list != null) {
                                    for (Purchase purchase : list) {
                                        verifySubPurchase(purchase);
                                    }
                                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                                    // Handle an error caused by a user cancelling the purchase flow.
                                    progressDialog.dismiss();
                                    setEnable(true);
                                } else {
                                    progressDialog.dismiss();
                                    setEnable(true);
                                    // Handle any other error codes.
                                }

                            }
                        }
                ).build();

        //start the connection after initializing the billing client
        establishConnection();

        CarrierBillingSupporter supporter=new CarrierBillingSupporter(BuyPremiumActivity.this);
        supporter.initialize("WpIP1g4SB9utHkDX73foOQNjcVyG2EMJrFsm60CLwb5iqTRhKx", "tuDLR7oszgw2qAYGIpMQ", new CarrierBillingSupporter.OnInitializeCompleteListener() {
            @Override
            public void onInitialized() {
           /*     purchaseDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setEnable(false);
                        supporter.subscribe();
                    }
                });

                purchaseHutch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setEnable(false);
                        supporter.subscribe();

                    }
                });
*/
            }

            @Override
            public void onSubscriptionChange(boolean subscription) {

            }

            @Override
            public void onSubscribed() {
                UpdateDatabase(2,"");
            }

            @Override
            public void onUnSubscribed() {
            }

            @Override
            public void onPaymentPending() {

            }
        });

    }

    private void setEnable(boolean b) {
        purchaseDialog.setEnabled(b);
        purchaseHutch.setEnabled(b);
        purchaseGoogle.setEnabled(b);
    }

    void establishConnection() {

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection();
            }
        });
    }

    void showProducts() {

        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(QueryProductDetailsParams.Product.newBuilder()
                .setProductId("storyflix_monthly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(
                params,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {

                        if (productDetailsList!=null){

                            int i=0;
                            while (i<productDetailsList.size()){
                                if (productDetailsList.get(i).getProductId().equals("storyflix_monthly")){

                                    int finalI = i;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            purchaseGoogle.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    setEnable(false);
                                                    progressDialog.show();
                                                    if (FirebaseAuth.getInstance().getCurrentUser()==null){
                                                        Toast.makeText(BuyPremiumActivity.this,"Please login to StoryFlix app before purchase the premium version!",Toast.LENGTH_LONG).show();
                                                        finish();
                                                    }else {
                                                        launchPurchaseFlow(productDetailsList.get(finalI));
                                                    }
                                                }
                                            });
                                        }
                                    });

                                    break;

                                }
                                i++;
                            }

                        }

                    }
                }
        );
    }

    void launchPurchaseFlow(ProductDetails productDetails) {

      /*  BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        int responseCode = billingClient.launchBillingFlow(BuyPremiumActivity.this, billingFlowParams).getResponseCode();

      */
        String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();

        ImmutableList productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(BuyPremiumActivity.this, billingFlowParams);
    }

    void verifySubPurchase(Purchase purchases) {

        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchases.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    UpdateDatabase(1,"");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void UpdateDatabase(int value,String mobileNumber){

        DocumentReference reference=firebaseFirestore.collection("Users").document(mainActivity.userID);
        HashMap<String,Object> data=new HashMap<>();
        data.put("PremiumMethod",value);
        data.put("MobileNumber",mobileNumber);
        reference.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("isPremium",true);
                editor.apply();
                Toast.makeText(BuyPremiumActivity.this, "You are a premium user now", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                mainActivity.finish();
                startActivity(new Intent(BuyPremiumActivity.this,SplashActivity.class));
                finish();
            }
        });

    }

}
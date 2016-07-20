package ict.niit.son.inappbilling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ict.niit.son.inappbilling.util.IabHelper;
import ict.niit.son.inappbilling.util.IabResult;
import ict.niit.son.inappbilling.util.Inventory;
import ict.niit.son.inappbilling.util.Purchase;

public class MainActivity extends AppCompatActivity {
    private Button btClickMe, btBuyAClick;

    //private static final String ITEM_SKU = "android.test.purchased"; //for test
    private static final String ITEM_SKU = "ict.niit.son.inappbilling.restwalk";
    private static final int REQUEST_CODE = 10001;
    private static final String TAG = "InAppBilling";
    IabHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btClickMe = (Button) findViewById(R.id.btClickMe);
        btBuyAClick = (Button) findViewById(R.id.btBuyAClick);

        setupInAppBilling();
    }

    private void setupInAppBilling() {
        String base64EncodePublicKey = getResources().getString(R.string.base64EncodeRSA);
        mHelper = new IabHelper(MainActivity.this, base64EncodePublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    Log.d(TAG, "Setup InAppBilling success");
                } else {
                    Log.d(TAG, "Setup InAppBilling failed");
                }
            }
        });
    }

    public void clickMe(View view) {
        btClickMe.setEnabled(false);
        btBuyAClick.setEnabled(true);
    }

    public void buyAClick(View view) {
        mHelper.launchPurchaseFlow(MainActivity.this, ITEM_SKU, REQUEST_CODE, purchaseFinishedListener);
    }

    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (result.isFailure()) {
                //error
                Toast.makeText(MainActivity.this, "InAppBilling failed!", Toast.LENGTH_LONG).show();
                return;
            } else if (info.getSku().equals(ITEM_SKU)) {
                consumeItem();
            }
        }
    };

    private void consumeItem() {
        mHelper.queryInventoryAsync(inventoryFinishedListener);
    }

    IabHelper.QueryInventoryFinishedListener inventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if (result.isFailure()) {
                //error
                Toast.makeText(MainActivity.this, "InAppBilling failed!", Toast.LENGTH_LONG).show();
                return;
            } else {
                mHelper.consumeAsync(inv.getPurchase(ITEM_SKU), consumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener consumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                btBuyAClick.setEnabled(true);
                Toast.makeText(MainActivity.this, "InAppBilling Successfully!", Toast.LENGTH_LONG).show();
            } else {
                // handle error
                Toast.makeText(MainActivity.this, "InAppBilling failed!", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }
}

package com.bunakari.sambalpurifashion.view;

import android.content.ComponentCallbacks2;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bunakari.sambalpurifashion.R;
import com.bunakari.sambalpurifashion.adapter.CartAdapter;
import com.bunakari.sambalpurifashion.model.BasicFunction;
import com.bunakari.sambalpurifashion.model.CartList;
import com.bunakari.sambalpurifashion.model.CartResponse;
import com.bunakari.sambalpurifashion.model.GetPrefs;
import com.bunakari.sambalpurifashion.model.ProfileResponse;
import com.bunakari.sambalpurifashion.model.SignupResponse;
import com.bunakari.sambalpurifashion.network.ApiService;
import com.bunakari.sambalpurifashion.network.RetroClass;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.ItemClickListener, View.OnClickListener {

    private RecyclerView cartRecyclerView;
    private CardView cardview;

    private CartAdapter cartAdapter;
    private CartAdapter.ItemClickListener itemClickListener;
    private TextView totalTextView,checkoutTextView,notfoundTextView,totalvalue,boookingam,balanceamt,walletamt,txttraveling;
    private ProgressBar progressBar;
    private List<CartResponse> cartResponseList;
    private SharedPreferences sharedPreferences;
    private String uidString,walletAmtString= "",wamt="";
    private int tempTotal = 0,bookingamount=0,bamt=0;
    public static CartActivity cartActivity;
    private LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Cart");

        initUi();

    }

    private void initUi() {

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalTextView = findViewById(R.id.totalTextView);
        totalvalue = findViewById(R.id.totalvalue);
        boookingam = findViewById(R.id.bookingvalue);
        checkoutTextView = findViewById(R.id.checkoutTextView);
        notfoundTextView = findViewById(R.id.notfoundTextView);
        txttraveling = findViewById(R.id.txttraveling);
        progressBar = findViewById(R.id.progressBar);
        cardview = findViewById(R.id.cartsummery);
        cartRecyclerView.setNestedScrollingEnabled(false);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        cartRecyclerView.setLayoutManager(layoutManager);
        balanceamt = findViewById(R.id.balanceamount);
        sharedPreferences = getSharedPreferences(GetPrefs.PREFS_NAME,getApplicationContext().MODE_PRIVATE);
        uidString = sharedPreferences.getString(GetPrefs.PREFS_UID,"");
        walletamt = findViewById(R.id.walletamount);
        itemClickListener = CartActivity.this;
        checkoutTextView.setOnClickListener(this);
      //  mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        GetWalletAmount();
        cartActivity = CartActivity.this;
    }

    private void GetCartData(){
        progressBar.setVisibility(View.VISIBLE);
        ApiService cartService = RetroClass.getApiService();
        Call<CartList> cartListCall = cartService.getCart(uidString);
        cartListCall.enqueue(new Callback<CartList>() {
            @Override
            public void onResponse(Call<CartList> call, Response<CartList> response) {
                progressBar.setVisibility(View.GONE);
                cartResponseList = new ArrayList<>();
                try {
                    List<CartResponse> cartResponses = null;
                    if (response.body() != null) {
                        cartResponses = response.body().getCartResponseList();
                        cartResponseList.addAll(cartResponses);

                        if (cartResponseList.size() > 0) {
                            notfoundTextView.setVisibility(View.GONE);
                            cartAdapter = new CartAdapter(itemClickListener,getApplicationContext(), cartResponseList);
                            cartRecyclerView.setAdapter(cartAdapter);
                            getTotal();
                        } else {
                            cartAdapter = new CartAdapter(itemClickListener,getApplicationContext(), cartResponseList);
                            cartRecyclerView.setAdapter(cartAdapter);
                            notfoundTextView.setVisibility(View.VISIBLE);
                            notfoundTextView.setText("Sorry, No Data Found");
                            totalTextView.setVisibility(View.GONE);
                            checkoutTextView.setVisibility(View.GONE);
                            cardview.setVisibility(View.GONE);
                        }
                    }else {
                        notfoundTextView.setVisibility(View.VISIBLE);
                        notfoundTextView.setText("Sorry, No Data Found");
                        totalTextView.setVisibility(View.GONE);
                        checkoutTextView.setVisibility(View.GONE);
                        cardview.setVisibility(View.GONE);
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                    notfoundTextView.setVisibility(View.VISIBLE);
                    notfoundTextView.setText("Sorry, No Data Found");
                    totalTextView.setVisibility(View.GONE);
                    checkoutTextView.setVisibility(View.GONE);
                    cardview.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<CartList> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                notfoundTextView.setVisibility(View.VISIBLE);
                notfoundTextView.setText("Sorry, No Data Found");
                totalTextView.setVisibility(View.GONE);
                checkoutTextView.setVisibility(View.GONE);
                cardview.setVisibility(View.GONE);
            }
        });
    }

    private void getTotal(){
        tempTotal = 0;
        int balaamt =0;
        for (int i = 0; i < cartResponseList.size(); i++) {
            if (cartResponseList.get(i).getOffer_price().length() != 0){
                tempTotal = tempTotal + (Integer.parseInt(cartResponseList.get(i).getOffer_price()) * (Integer.parseInt(cartResponseList.get(i).getQty())));
                bookingamount = tempTotal*20/100;
            }else {
                tempTotal = tempTotal + (Integer.parseInt(cartResponseList.get(i).getPrice()) * (Integer.parseInt(cartResponseList.get(i).getQty())));
                bookingamount = tempTotal*20/100;
            }
        }


        balaamt = tempTotal-bookingamount-(Integer.parseInt(walletAmtString))+200;
        totalTextView.setText("Booking Amount : " + "\u20B9 "+bookingamount);
        totalTextView.setVisibility(View.VISIBLE);
        checkoutTextView.setVisibility(View.VISIBLE);
        cardview.setVisibility(View.VISIBLE);
        totalvalue.setText("\u20B9 "+ tempTotal);
        boookingam.setText(" - " + "\u20B9 "+ bookingamount);
        totalTextView.setVisibility(View.VISIBLE);
        balanceamt.setText( "Balance amount " +"\u20B9 "+ balaamt +" to be paid at the time of visit ");
        walletamt.setText(" - " +"\u20B9 " + walletAmtString);
        txttraveling.setText( "\u20B9 " + 200);
    }

    private void deleteProduct(int position){

        ApiService deleteService = RetroClass.getApiService();
        Call<SignupResponse> deleteResponseCall = deleteService.deleteProductFromCart(cartResponseList.get(position).getCartid());
        deleteResponseCall.enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                progressBar.setVisibility(View.GONE);
                SignupResponse signupResponse = response.body();
                if (signupResponse != null) {
                    if (signupResponse.success == 1) {
                        GetCartData();
                    } else {
                        BasicFunction.showDailogBox(CartActivity.this, "Oops something went wrong.Please try again");
                    }
                }else {
                    BasicFunction.showDailogBox(CartActivity.this, "Oops something went wrong.Please try again");
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                BasicFunction.showDailogBox(CartActivity.this,"Oops something went wrong.Please try again");
            }
        });
    }

    private void UpdateCart(final int position, final int qty, final int flag){
        Log.d("plus",qty+" ");
        progressBar.setVisibility(View.VISIBLE);
        int total = 0;
        if (cartResponseList.get(position).getOffer_price().length() != 0){
            total = total + (Integer.parseInt(cartResponseList.get(position).getOffer_price()) * (qty));
            bamt = total*20/100;
        }else {
            total = total + (Integer.parseInt(cartResponseList.get(position).getPrice()) * (qty));
            bamt = total*20/100;
        }
        if (bamt<1000){

            bookingamount = 500;
        }
        else{

            bookingamount =bamt;
        }

        ApiService addCartService = RetroClass.getApiService();
        Call<SignupResponse> cartResponseCall = addCartService.addToCart(uidString,cartResponseList.get(position).getPid(),bookingamount+"",qty+ "", total + "");
        cartResponseCall.enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                progressBar.setVisibility(View.GONE);
                SignupResponse signupResponse = response.body();
                if (signupResponse != null) {
                    if (signupResponse.success == 1){
                        BasicFunction.showToast(getApplicationContext(),"Item updated to cart");
                        cartResponseList.get(position).setQty(""+qty);
                        View v = layoutManager.findViewByPosition(position);
                        TextView qtyTextView = v.findViewById(R.id.qtyTextView);
                        qtyTextView.setText(""+qty);
                        getTotal();
                    }else {
                        BasicFunction.showToast(getApplicationContext(),"Item can't added to cart");
                        View v = layoutManager.findViewByPosition(position);
                        TextView qtyTextView = v.findViewById(R.id.qtyTextView);
                        if (flag == 1){
                            qtyTextView.setText(String.valueOf(qty+1));
                        }else {
                            qtyTextView.setText(String.valueOf(qty-1));
                        }
                    }
                }else {
                    View v = layoutManager.findViewByPosition(position);
                    TextView qtyTextView = v.findViewById(R.id.qtyTextView);
                    if (flag == 1){
                        qtyTextView.setText(String.valueOf(qty+1));
                    }else {
                        qtyTextView.setText(String.valueOf(qty-1));
                    }
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                BasicFunction.showToast(getApplicationContext(),"Item can't added to cart");
                View v = layoutManager.findViewByPosition(position);
                TextView qtyTextView = v.findViewById(R.id.qtyTextView);
                if (flag == 1){
                    qtyTextView.setText(String.valueOf(qty+1));
                }else {
                    qtyTextView.setText(String.valueOf(qty-1));
                }
            }
        });
    }


    private void GetWalletAmount() {

        ApiService apiService = RetroClass.getApiService();
        Call<ProfileResponse> profileResponseCall = apiService.getWalletAmount(uidString);
        profileResponseCall.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                progressBar.setVisibility(View.GONE);
                ProfileResponse profileResponse = response.body();
                if (profileResponse != null) {
                    wamt = profileResponse.money;

                    if( Integer.parseInt(wamt)>=500){

                        walletAmtString = "500";
                    }
                    else {
                        walletAmtString = wamt;
                    }


                }else {
                    BasicFunction.showDailogBox(CartActivity.this,"Oops Something went wrong, Please try again..!!");
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                BasicFunction.showDailogBox(CartActivity.this,"Oops Something went wrong, Please try again..!!");
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (BasicFunction.isNetworkAvailable(getApplicationContext())) {
            GetCartData();
        }else {
            BasicFunction.showSnackbar(CartActivity.this,"No internet connection,Please try again..!!");
        }
        String loginSession = sharedPreferences.getString(GetPrefs.PREFS_SESSION,"");
        if (loginSession.equalsIgnoreCase("2")){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(GetPrefs.PREFS_SESSION,"1");
            editor.commit();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            //called when app goes to background
            String loginSession = sharedPreferences.getString(GetPrefs.PREFS_SESSION,"");
            if (loginSession.equalsIgnoreCase("1")){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(GetPrefs.PREFS_SESSION,"2");
                editor.commit();
            }
        }
    }

    @Override
    public void onItemClick(View view, final int position) {
        if (view.getTag().equals(1)){
            AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage("Are you sure you want to delete a product ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteProduct(position);
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        }else if (view.getTag().equals(2)){
            View v = layoutManager.findViewByPosition(position);
            TextView qtyTextView = v.findViewById(R.id.qtyTextView);
            ImageView minusImageView = v.findViewById(R.id.minusImgView);
            if (qtyTextView.getText().toString().trim().equalsIgnoreCase("1")) {
                minusImageView.setClickable(false);
            } else {
                int qty = Integer.parseInt(qtyTextView.getText().toString().trim());
                qty--;
                minusImageView.setClickable(true);
                qtyTextView.setText(""+qty);
                Log.d("minus",qty+" ");
                UpdateCart(position,qty,1);
            }

        }else if (view.getTag().equals(3)){
            View v = layoutManager.findViewByPosition(position);
            TextView qtyTextView = v.findViewById(R.id.qtyTextView);
            ImageView minusImageView = v.findViewById(R.id.minusImgView);
            int qty = Integer.parseInt(qtyTextView.getText().toString().trim());
            qty++;
            minusImageView.setClickable(true);
            qtyTextView.setText("" + qty);
            Log.d("plus",qty+" ");
            UpdateCart(position,qty,2);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.checkoutTextView){

            if( tempTotal>=3000){
            Intent intent = new Intent(getApplicationContext(),BookingActivity.class);
         //String bm =totalTextView.getText().toString();

           String ba= String.valueOf(bookingamount);
           intent.putExtra("bookingamt",ba);
            intent.putExtra("walletamount", walletAmtString);
            intent.putExtra("cId", "1");
            startActivity(intent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(GetPrefs.PREFS_AMOUNT,tempTotal+"");
            editor.putString(GetPrefs.PREFS_ITEM,cartResponseList.size()+"");

            editor.commit();
            }
            else {

                BasicFunction.showDailogBox(CartActivity.this, "Total amount should be more then 3000");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                // Intent homeIntent = new Intent(this, MainActivity.class);
                // homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // startActivity(homeIntent);
                finish();
                return true;
            default:
                return (super.onOptionsItemSelected(item));
        }
    }
}

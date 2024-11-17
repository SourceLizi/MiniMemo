package com.memo.minimemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.memo.minimemo.databinding.ActivityMainBinding;
import com.memo.minimemo.databinding.FragmentContentBinding;
import com.memo.minimemo.db.MemoData;
import com.memo.minimemo.transcribe.AssetUtils;
import com.memo.minimemo.transcribe.WhisperService;

import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController m_navController;

    private SearchView searchView;

    private MemoViewModel mViewModel;

    public final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    mViewModel.createRecorder();
                }else{
                    Snackbar.make(binding.getRoot(), "语音识别需要授予权限"
                            , Snackbar.LENGTH_LONG).show();
                }
            });

    public void setDoneVisible(boolean vis){
        Menu menu = (Menu)binding.toolbar.getMenu();
        menu.findItem(R.id.action_save).setVisible(vis);
    }

    @Override
    protected void onDestroy() {
        this.mViewModel.releaseRecorder();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        m_navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(m_navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, m_navController, appBarConfiguration);

        this.mViewModel = new ViewModelProvider(this).get(MemoViewModel.class);

        OnBackPressedCallback callback  = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if(searchView != null && !searchView.isIconified()){
                    searchView.setQuery("",false);
                    searchView.clearFocus();
                    searchView.setIconified(true);
                }else{
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAnchorView(R.id.fab)
//                        .setAction("Action", null).show();


            }
        });
        if (ActivityCompat.checkSelfPermission(
                getApplication(), Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            this.mViewModel.createRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.RECORD_AUDIO)) {
            Snackbar.make(binding.getRoot(), "语音识别需要授予权限", Snackbar.LENGTH_LONG)
                    .setAction("授予权限", view -> requestPermissionLauncher.launch(
                            Manifest.permission.RECORD_AUDIO)).show();
        } else {
            requestPermissionLauncher.launch(
                    Manifest.permission.RECORD_AUDIO);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        this.searchView = (SearchView) searchViewItem.getActionView();


        m_navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination,
                                             @Nullable Bundle arguments) {
                int destination_id = destination.getId();
                Menu menu = (Menu)binding.toolbar.getMenu();
                if(destination_id == R.id.ContentFragment){
                    menu.findItem(R.id.action_new).setVisible(false);
                    menu.findItem(R.id.action_search).setVisible(false);
                }else if(destination_id == R.id.FragmentList){
                    SearchView searchView = (SearchView) searchViewItem.getActionView();
                    if(searchView != null) {
                        searchView.setQuery("",false);
                        searchView.clearFocus();
                        searchView.setIconified(true);
                    }
                    menu.findItem(R.id.action_new).setVisible(true);
                    menu.findItem(R.id.action_search).setVisible(true);
                    menu.findItem(R.id.action_save).setVisible(false);
                }
                //Log.i("TAG", "onDestinationChanged: id = " + destination.getId());
            }
        });

        if(searchView != null){
            searchView.setIconifiedByDefault(true);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    mViewModel.setDefaultData();
                    return false;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchView.clearFocus();
                    mViewModel.find(query);
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int menu_id = item.getItemId();
        Log.i("TAG","onOptionsItemSelected,id="+String.valueOf(menu_id));

        if(menu_id != R.id.action_search){
            if(this.searchView != null){
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(),0);

                searchView.setQuery("",false);
                searchView.clearFocus();
                searchView.setIconified(true);
            }
            if (menu_id == R.id.action_new) {
                String new_title = getResources().getString(R.string.new_title);
                MemoData new_memo = new MemoData(new_title,"");
                this.mViewModel.insert(new_memo);
                mViewModel.setDefaultData();
            }else if(menu_id == R.id.action_save){
                MemoData editingMemo = this.mViewModel.getCurrEditing();
                FragmentContentBinding binding1 = this.mViewModel.getContent_binding();
                if(editingMemo != null && binding1 != null){
                    binding1.textTitle.clearFocus();
                    binding1.textContent.clearFocus();

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding1.textContent.getWindowToken(),0);
                    imm.hideSoftInputFromWindow(binding1.textTitle.getWindowToken(),0);

                    editingMemo.title = binding1.textTitle.getText().toString();
                    editingMemo.content = binding1.textContent.getText().toString();
                    editingMemo.updateTime = System.currentTimeMillis();
                    this.mViewModel.update(editingMemo);

                    Menu menu = (Menu)binding.toolbar.getMenu();
                    menu.findItem(R.id.action_save).setVisible(false);
                    //m_navController.navigateUp();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();

    }
}
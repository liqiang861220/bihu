package cn.com.caoyue.bihu.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.jude.utils.JActivityManager;
import com.jude.utils.JUtils;

import cn.com.caoyue.bihu.BuildConfig;
import cn.com.caoyue.bihu.R;
import cn.com.caoyue.bihu.data.storage.CurrentFragment;
import cn.com.caoyue.bihu.data.storage.CurrentQuestion;
import cn.com.caoyue.bihu.data.storage.CurrentUser;
import cn.com.caoyue.bihu.ui.dialog.NewAnswerDialog;
import cn.com.caoyue.bihu.ui.dialog.NewQuestionDialog;
import cn.com.caoyue.bihu.ui.fragment.AnswerFragment;
import cn.com.caoyue.bihu.ui.fragment.HomeFragment;
import cn.com.caoyue.bihu.ui.navigation.NavManager;
import cn.com.caoyue.bihu.util.CurrentState;

public class MainActivity extends AppCompatActivity implements NewQuestionDialog.NewQuestionDialogCreater, NewAnswerDialog.NewAnswerDialogCreater {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize JUtils & JActivityManager
        JUtils.initialize(getApplication());
        JUtils.setDebug(BuildConfig.DEBUG, "inMain");
        JActivityManager.getInstance().pushActivity(MainActivity.this);
        // Restore savedInstanceState
        if (null != savedInstanceState) {
            onRestoreInstanceState(savedInstanceState);
        }
        // Initialize Layout
        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_inMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Drawer
        drawerLayout = NavManager.getInstance().init(MainActivity.this);
        // Fragment
        try {
            setFragment((Fragment) CurrentFragment.getInstance().getSavedFragmentClass().newInstance(), CurrentFragment.getInstance().getSavedFragmentClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
            setFragment(new HomeFragment(), HomeFragment.class.getName());
        }
        // Float Action Button
        ((FloatingActionButton) findViewById(R.id.fab_add)).setOnClickListener(new Listener());
    }

    public void setFragment(Fragment fragment, String tag) {
        CurrentFragment.getInstance().storage(fragment);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_container, fragment, tag).commit();
    }

    public void setFragmentWithArgs(Fragment fragment, String tag, Bundle args) {
        fragment.setArguments(args);
        CurrentFragment.getInstance().storage(fragment);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_container, fragment, tag).commit();
    }

    public class Listener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_add:
                    if (getCurrentFragmentName().equals(HomeFragment.class.getName())) {
                        new NewQuestionDialog().show(getSupportFragmentManager(), NewQuestionDialog.class.getName());
                    }
                    if (getCurrentFragmentName().equals(AnswerFragment.class.getName())) {
                        new NewAnswerDialog().show(getSupportFragmentManager(), NewAnswerDialog.class.getName() + CurrentQuestion.getInstance().id);
                    }
                    break;
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // 返回键关闭 Drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (!getCurrentFragmentName().equals(HomeFragment.class.getName())) {
            // 返回键回到主页面
            setFragment(new HomeFragment(), HomeFragment.class.getName());
        } else {
            super.onBackPressed();
        }
    }

    public String getCurrentFragmentName() {
        return CurrentFragment.getInstance().getSavedFragmentClass().getName();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CurrentState.save(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CurrentState.restore(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JActivityManager.getInstance().popActivity(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ModifyFaceActivity.REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    CurrentUser.getInstance().face = data.getExtras().getString("UserFaceUrl");
                    drawerLayout = NavManager.getInstance().refreshFace();
                    drawerLayout.closeDrawers();
                    JUtils.Log("nav face refresh called");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        ((AppCompatActivity) context).overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
        context.startActivity(intent);
    }

    @Override
    public NewAnswerDialog.NewAnswerDialogListener getNewAnswerDialogListener() {
        return new NewAnswerDialog.NewAnswerDialogListener() {
            @Override
            public void onSuccess() {
                ((CommitSuccessCallBack) getSupportFragmentManager().findFragmentByTag(AnswerFragment.class.getName() + CurrentQuestion.getInstance().id)).onCommitSuccess();
            }

            @Override
            public void onCancel() {

            }
        };
    }

    @Override
    public NewQuestionDialog.NewQuestionDialogListener getNewQuestionDialogListener() {
        return new NewQuestionDialog.NewQuestionDialogListener() {
            @Override
            public void onSuccess() {
                    ((CommitSuccessCallBack) getSupportFragmentManager().findFragmentByTag(HomeFragment.class.getName())).onCommitSuccess();
            }

            @Override
            public void onCancel() {

            }
        };
    }

    public interface CommitSuccessCallBack {
        void onCommitSuccess();
    }

}

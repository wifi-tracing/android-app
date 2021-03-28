package com.prj.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.prj.app.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 6;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager2 viewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_welcome);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager);
        pagerAdapter = new WelcomeSlidePageAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }

    public void onNextPressed(View view) {
        if (viewPager.getCurrentItem() == NUM_PAGES - 1) {
            finish();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // This calls finish() on this activity and pops the back stack    .
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private static class WelcomeSlidePageAdapter extends FragmentStateAdapter {
        public WelcomeSlidePageAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public @NotNull Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return WelcomeSlidePageFragment.newInstance("Hi!", "Let's walk you through all you have to know.", R.drawable.welcome_woman);
                case 1:
                    return WelcomeSlidePageFragment.newInstance("How does it work?", "The app collects WiFi signals around you, and uses that information to alert you of possible exposures.", R.drawable.welcome_petri_dish);
                case 2:
                    return WelcomeSlidePageFragment.newInstance("What if I am exposed?", "When an exposure is detected, you'll receive a notification. If you do, please contact your GP as soon as possible.", R.drawable.welcome_test_results);
                case 3:
                    return WelcomeSlidePageFragment.newInstance("What happens next?", "An operator will send you a QR code. After scanning it, you'll have the option to upload all the WiFi scans. This will help others know if they've been exposed.", R.drawable.welcome_computer);
                case 4:
                    return WelcomeSlidePageFragment.newInstance("What about my data?", "All collected data is completely anonymous, and stored for only 14 days. Also, you can choose to store location data, to help identify outbreak clusters.", R.drawable.welcome_document);
                case 5:
                    return WelcomeSlidePageFragment.newInstance("What if I change my mind?", "Simply go to the settings page, and we will stop gathering location data.", R.drawable.welcome_magnifier);
                default:
                    return WelcomeSlidePageFragment.newInstance("Oh-Oh", "There seems to be an error.", R.drawable.ic_coronavirus);
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

}
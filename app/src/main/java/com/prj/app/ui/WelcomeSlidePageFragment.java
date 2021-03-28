package com.prj.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.prj.app.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WelcomeSlidePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeSlidePageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    private static final String TITLE_PARAM = "titleParam";
    private static final String CONTENT_PARAM = "contentParam";
    private static final String IMAGE_ID_PARAM = "imageIdParam";


    // TODO: Rename and change types of parameters
    private String title;
    private String content;
    private Integer imageId;


    public WelcomeSlidePageFragment() {


    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title   Parameter 1.
     * @param content Parameter 2.
     * @return A new instance of fragment WelcomeSlidePageFragment.
     */
    public static WelcomeSlidePageFragment newInstance(String title, String content, Integer imageId) {
        WelcomeSlidePageFragment fragment = new WelcomeSlidePageFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_PARAM, title);
        args.putString(CONTENT_PARAM, content);
        args.putInt(IMAGE_ID_PARAM, imageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_welcome_slide_page, container, false);

        if (getArguments() != null) {
            title = getArguments().getString(TITLE_PARAM);
            content = getArguments().getString(CONTENT_PARAM);
            imageId = getArguments().getInt(IMAGE_ID_PARAM);

            TextView titleText = view.getRootView().findViewById(R.id.titleText);
            TextView contentText = view.getRootView().findViewById(R.id.contentText);
            ImageView imageView = view.findViewById(R.id.imageView);

            titleText.setText(title);
            contentText.setText(content);
            imageView.setImageResource(imageId);

        }


        return view;

    }

}
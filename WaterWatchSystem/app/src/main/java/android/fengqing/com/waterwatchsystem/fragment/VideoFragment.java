package android.fengqing.com.waterwatchsystem.fragment;

import android.app.Fragment;
import android.fengqing.com.waterwatchsystem.R;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;


public class VideoFragment extends Fragment {

    VideoView mVideoView;
    MediaController mediaController;
    private static class SingletonHolder {
        private static final VideoFragment INSTANCE = new VideoFragment();
    }
    public static VideoFragment instance() {
        return SingletonHolder.INSTANCE;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_fragment,container,false);
        initVideoView(view);
        return view;
    }
    private void initVideoView(View view){
        mVideoView = (VideoView)view.findViewById(R.id.video_view);
        Uri uri = Uri.parse("android.resource://android.fengqing.com.waterwatchsystem/" + R.raw.sample);
        mVideoView.setVideoURI(uri);
        mediaController = new MediaController(getContext());
        mVideoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(mVideoView);
        mediaController.setPadding(0,200,220,54);
    }
}

package android.fise.com.fiseassitant;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import uk.co.senab.photoview.*;
import com.bumptech.glide.*;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
public class PictureSlideFragment extends Fragment {
    private String url;
    private PhotoViewAttacher mAttacher;
    private ImageView imageView;

    public static PictureSlideFragment newInstance(String url) {
        PictureSlideFragment f = new PictureSlideFragment();

        Bundle args = new Bundle();
        args.putString("url", url);
        f.setArguments(args);

        return f;//获得一个包含图片url的PictureSlideFragmen的实例
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments() != null ? getArguments().getString("url") : "http://www.zhagame.com/wp-content/uploads/2016/01/JarvanIV_6.jpg";

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.picture_fragment,container,false);

        imageView= (ImageView) v.findViewById(R.id.iv_main_pic);
        mAttacher = new PhotoViewAttacher(imageView);//使用PhotoViewAttacher为图片添加支持缩放、平移的属性

        Glide.with(getActivity()).load(url).crossFade().into(new GlideDrawableImageViewTarget(imageView) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation animation) {
                super.onResourceReady(resource, animation);
                mAttacher.update();//调用PhotoViewAttacher的update()方法，使图片重新适配布局
            }
        });
        return v;
    }
}
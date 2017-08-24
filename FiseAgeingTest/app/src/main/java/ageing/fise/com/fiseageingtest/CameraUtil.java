package ageing.fise.com.fiseageingtest;

import java.util.List;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class CameraUtil {
    private static final String TAG = "CameraUtil";
	
	public static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }
	
	public static void Assert(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

	public static void fitPreviewSize(Activity activity,
			Parameters cameraParams, View cameraPreview) {
        Size previewSize = cameraParams.getPreviewSize();
        
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        LayoutParams params = cameraPreview.getLayoutParams();
        params.width = previewSize.width * dm.heightPixels / previewSize.height;
        cameraPreview.setLayoutParams(params);
	}

    public static final String PICTURE_RATIO_16_9 = "1.7778";
    public static final String PICTURE_RATIO_5_3 = "1.6667";
    public static final String PICTURE_RATIO_3_2 = "1.5";
    public static final String PICTURE_RATIO_4_3 = "1.3333";

    public static final double ASPECT_TOLERANCE = 0.001;

    public static Size getOptimalPreviewSize(Activity activity,
            List<Size> sizes, double targetRatio, boolean findMinalRatio, boolean needStandardPreview) {
        // Use a very small tolerance because we want an exact match.
        //final double EXACTLY_EQUAL = 0.001;
        if (sizes == null) {
            return null;
        }

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        double minDiffWidth = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of preview surface. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size.
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(point);

        int targetHeight = Math.min(point.x, point.y);
        int targetWidth = Math.max(point.x, point.y);
        if (findMinalRatio) {
            //Find minimal aspect ratio for that: special video size maybe not have the mapping preview size.
            double minAspectio = Double.MAX_VALUE;
            for (Size size : sizes) {
                double aspectRatio = (double)size.width / size.height;
                if (Math.abs(aspectRatio - targetRatio)
                        <= Math.abs(minAspectio - targetRatio)) {
                    minAspectio = aspectRatio;
                }
            }
            Log.d(TAG, "getOptimalPreviewSize(" + targetRatio + ") minAspectio=" + minAspectio);
            targetRatio = minAspectio;
        }

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
                minDiffWidth = Math.abs(size.width - targetWidth);
            } else if ((Math.abs(size.height - targetHeight) == minDiff)
                    && Math.abs(size.width - targetWidth) < minDiffWidth) {
                optimalSize = size;
                minDiffWidth = Math.abs(size.width - targetWidth);
            }
        }

        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        /// M: This will happen when native return video size and wallpaper want to get specified ratio.
        if (optimalSize == null && needStandardPreview) {
            Log.w(TAG, "No preview size match the aspect ratio" + targetRatio + "," +
            		"then use the standard(4:3) preview size");
            minDiff = Double.MAX_VALUE;
            targetRatio = Double.parseDouble(PICTURE_RATIO_4_3);
            for (Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                    continue;
                }
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static void setCameraPreviewRatio_4_3(CameraManager cameraManager, Activity activity) {
        Parameters parameters = cameraManager.getParameters();
        {
	        Size optimalSize = getOptimalPreviewSize(
                    activity,
                    parameters.getSupportedPreviewSizes(),
                    4/3d,
                    false, true);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        }
        cameraManager.setParameters(parameters);
    }

}

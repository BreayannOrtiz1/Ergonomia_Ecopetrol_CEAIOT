package com.vivalnk.sdk.demo.vital.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.google.android.material.R.attr;
import com.google.android.material.navigation.NavigationView;
import android.util.AttributeSet;
import com.vivalnk.sdk.BuildConfig;
import com.vivalnk.sdk.VitalClient;
import com.vivalnk.sdk.common.utils.DensityUtils;
import com.vivalnk.sdk.demo.vital.R;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VersionNavigationView extends NavigationView {

  protected Paint markTextPaint;
  protected int markTextColor;

  private String sdkVersion;
  private String demoVersion;

  private int width;
  private int height;

  public VersionNavigationView(Context context) {
    this(context, (AttributeSet) null);
  }

  public VersionNavigationView(Context context, AttributeSet attrs) {
    this(context, attrs, attr.navigationViewStyle);
  }

  public VersionNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    markTextColor = getContext().getResources().getColor(R.color.black);
    //text paint
    markTextPaint = new Paint();
    markTextPaint.setColor(markTextColor);
    markTextPaint.setTextSize(DensityUtils.dip2px(getContext(), 12F));

    demoVersion = com.vivalnk.sdk.demo.vital.BuildConfig.VERSION_NAME + "(" + com.vivalnk.sdk.demo.vital.BuildConfig.VERSION_CODE + ")";
    sdkVersion = VitalClient.getInstance().getVersion() + "(" + VitalClient.getInstance().getVersionCode() + ")";
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    width = w;
    height = h;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawMarkText(canvas);
  }

  public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private void drawMarkText(Canvas canvas) {
    Paint.FontMetrics fm = markTextPaint.getFontMetrics();
    float textHeight = Math.abs(fm.leading + fm.ascent);
    canvas.drawText("Demo: v" + demoVersion, 20, height - 9 * (textHeight + 10) - 40, markTextPaint);
    canvas.drawText("BuildType: " + com.vivalnk.sdk.demo.vital.BuildConfig.BUILD_TYPE, 20, height - 8 * (textHeight + 10) - 40, markTextPaint);
    canvas.drawText("BuildTime: " + sdf.format(new Date(com.vivalnk.sdk.demo.vital.BuildConfig.buildTime)), 20, height - 7 * (textHeight + 10) - 40, markTextPaint);

    canvas.drawText("SDK: v" + sdkVersion, 20, height - 6 * (textHeight + 10) - 20, markTextPaint);
    canvas.drawText("BuildType: " + com.vivalnk.sdk.BuildConfig.BUILD_TYPE, 20, height - 5 * (textHeight + 10) - 20, markTextPaint);
    canvas.drawText("BuildTime: " + sdf.format(new Date(com.vivalnk.sdk.BuildConfig.buildTime)), 20, height - 4 * (textHeight + 10) - 20, markTextPaint);

    canvas.drawText("Common: v" + com.vivalnk.sdk.common.BuildConfig.VERSION_NAME + "(" + com.vivalnk.sdk.common.BuildConfig.VERSION_CODE + ")", 20, height - 3 * (textHeight + 10), markTextPaint);
    canvas.drawText("BuildType: " + com.vivalnk.sdk.common.BuildConfig.BUILD_TYPE, 20, height - 2 * (textHeight + 10), markTextPaint);
    canvas.drawText("BuildTime: " + sdf.format(new Date(com.vivalnk.sdk.common.BuildConfig.buildTime)), 20, height - (textHeight + 10), markTextPaint);
  }

  public void setDemoVersion(String demoVersion) {
    this.demoVersion = demoVersion;
    postInvalidate();
  }

  public void setSDKVersion(String sdkVersion) {
    this.sdkVersion = sdkVersion;
    postInvalidate();
  }
}

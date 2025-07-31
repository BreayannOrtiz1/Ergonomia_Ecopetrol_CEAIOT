package com.vivalnk.sdk.demo.vital.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.vivalnk.sdk.demo.base.app.Layout;
import com.vivalnk.sdk.demo.base.widget.LiveEcgScreen;
import com.vivalnk.sdk.demo.base.widget.RTSEcgView;
import com.vivalnk.sdk.common.eventbus.Subscribe;
import com.vivalnk.sdk.demo.vital.R;
import com.vivalnk.sdk.model.Device;
import com.vivalnk.sdk.model.SampleData;
import com.vivalnk.sdk.open.BaseLineRemover;
import java.util.List;

public class EcgFragment extends ConnectedFragment {

    @BindView(R.id.ecgView)
    RTSEcgView ecgView;

    @BindView(R.id.btnSwitchGain)
    Button btnSwitchGain;

    @BindView(R.id.btnRevert)
    Button btnRevert;

    @BindView(R.id.btnNoisy)
    Button btnNoisy;

    @BindView(R.id.tvHR)
    TextView tvHR;

    @BindView(R.id.tvRR)
    TextView tvRR;

    LiveEcgScreen mLiveEcgScreen;

    private BaseLineRemover remover;

    private volatile boolean denoisy;
    private volatile boolean revert;

    @Override
    protected Layout getLayout() {
        return Layout.createLayoutByID(R.layout.fragment_ecg_graphic);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {

        remover = new BaseLineRemover(mDevice, new BaseLineRemover.Listener() {
            @Override
            public void onDataPop(Device device, SampleData ecgData) {
                mLiveEcgScreen.update(ecgData);

                if (ecgData.getHR() != null && ecgData.getHR() > 0) {
                    tvHR.setText("HR: " + ecgData.getHR());
                }

                if (ecgData.getRR() != null && ecgData.getRR() > 0) {
                    tvRR.setText("RR: " + ecgData.getRR());
                }
            }

            @Override
            public void onDataDiscontinous(Device device, List<SampleData> dataList) {

            }

            @Override
            public void onError(Device device, int code, String msg) {

            }
        });

        mLiveEcgScreen = new LiveEcgScreen(getContext(), mDevice, ecgView);
        mLiveEcgScreen.setDrawDirection(RTSEcgView.LEFT_IN_RIGHT_OUT);
        mLiveEcgScreen.showMarkPoint(true);

        btnNoisy.setText(denoisy ? R.string.tv_denoising_close : R.string.tv_denoising_open);
        btnRevert.setText(revert ? R.string.tv_de_revert : R.string.tv_revert);
    }

    @Subscribe
    public void onEcgDataEvent(SampleData ecgData) {
        if (!ecgData.getDeviceID().equals(mDevice.getId())) {
            return;
        }
        if (!ecgData.isFlash()) {
            remover.handle(ecgData);
        }
    }

    @OnClick(R.id.btnSwitchGain)
    protected void clickSwitchGain() {
        mLiveEcgScreen.switchGain();
    }

    @OnClick(R.id.btnRevert)
    protected void clickRevert() {
        revert = !revert;
        mLiveEcgScreen.revert(revert);
        btnRevert.setText(revert ? R.string.tv_de_revert : R.string.tv_revert);
    }

    @OnClick(R.id.btnNoisy)
    protected void clickDenoisy() {
        denoisy = !denoisy;
        mLiveEcgScreen.denoisy(denoisy);
        btnNoisy.setText(denoisy ? R.string.tv_denoising_close : R.string.tv_denoising_open);
    }

    @Override
    public void onDestroyView() {
        mLiveEcgScreen.destroy();
        super.onDestroyView();
    }
}

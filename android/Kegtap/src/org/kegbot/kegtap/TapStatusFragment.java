package org.kegbot.kegtap;

import java.text.ParseException;

import javax.measure.units.NonSI;
import javax.measure.units.SI;

import org.jscience.physics.measures.Measure;
import org.kegbot.kegtap.util.image.ImageDownloader;
import org.kegbot.proto.Api.TapDetail;
import org.kegbot.proto.Models.Image;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;

public class TapStatusFragment extends ListFragment {

  private final String TAG = TapStatusFragment.class.getSimpleName();

  private TapDetail mTapDetail;

  private final ImageDownloader mImageDownloader = ImageDownloader.getSingletonInstance();

  private final OnClickListener mOnBeerMeClickedListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      final String tapName = mTapDetail.getTap().getMeterName();
      final Intent intent = DrinkerSelectActivity.getStartIntentForTap(getActivity(), tapName);
      startActivity(intent);
    }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.tap_status_fragment_layout, container, false);
    if (mTapDetail != null) {
      buildTapView(view, mTapDetail);
    }
    ((Button) view.findViewById(R.id.beerMeButton)).setOnClickListener(mOnBeerMeClickedListener);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  public View buildTapView(View view, TapDetail tap) {
    final TextView title = (TextView) view.findViewById(R.id.tapTitle);
    if (tap == null) {
      Log.w(TAG, "Called with empty tap detail.");
      return view;
    }
    if (title == null) {
      Log.wtf(TAG, "Null title");
      return view;
    }
    title.setText(tap.getBeerType().getName());

    final String tapName = tap.getTap().getName();
    if (!Strings.isNullOrEmpty(tapName)) {
      TextView subtitle = (TextView) view.findViewById(R.id.tapSubtitle);
      subtitle.setText(tapName);
    }

    CharSequence relTime;
    try {
      long tapDate = Utils.dateFromIso8601String(tap.getKeg().getStartedTime());
      relTime = DateUtils.getRelativeTimeSpanString(tapDate);
    } catch (ParseException e) {
      relTime = null;
    }

    TextView date = (TextView) view.findViewById(R.id.tapDateTapped);
    if (relTime != null) {
      date.setText("Tapped " + relTime);
    } else {
      date.setVisibility(View.GONE);
    }

    final ImageView tapImage = (ImageView) view.findViewById(R.id.tapImage);
    if (tapImage != null) {
      tapImage.setBackgroundResource(R.drawable.kegbot_unknown_square_2);
      if (tap.getBeerType().hasImage()) {
        final Image image = tap.getBeerType().getImage();
        final String imageUrl = image.getUrl();
        mImageDownloader.download(imageUrl, tapImage);
      }
    }

    float percentFull = tap.getKeg().getPercentFull();
    TextView kegStatusText = (TextView) view.findViewById(R.id.tapKeg);
    final String statusString;
    if (percentFull > 0) {
      statusString = String.format("%.1f%% full", Float.valueOf(percentFull));
    } else {
      statusString = "Empty!";
    }
    kegStatusText.setText(statusString);

    if (tap.getTap().hasLastTemperature()) {
      float lastTemperature = tap.getTap().getLastTemperature().getTemperatureC();
      TextView tapTemperature = (TextView) view.findViewById(R.id.tapTemperature);
      double lastTempF = Measure.valueOf(lastTemperature, SI.CELSIUS).doubleValue(NonSI.FAHRENHEIT);
      tapTemperature.setText(String.format("%.2f�C / %.2f�F", Float.valueOf(lastTemperature),
          Double.valueOf(lastTempF)));
    }

    view.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_rounded_rect));

    final String description = tap.getKeg().getDescription();
    final TextView descView = (TextView) view.findViewById(R.id.tapDescription);
    if (!Strings.isNullOrEmpty(description)) {
      descView.setVisibility(View.VISIBLE);
      descView.setText(description);
    } else {
      descView.setVisibility(View.GONE);
    }

    return view;
  }

  public void setTapDetail(TapDetail tapDetail) {
    mTapDetail = tapDetail;
  }

  public TapDetail getTapDetail() {
    return mTapDetail;
  }

}
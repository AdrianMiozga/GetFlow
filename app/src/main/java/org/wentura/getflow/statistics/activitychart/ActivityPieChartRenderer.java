/*
 * Copyright (C) 2020 Adrian Miozga <AdrianMiozga@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.wentura.getflow.statistics.activitychart;

import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import org.wentura.getflow.Constants;
import org.wentura.getflow.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final public class ActivityPieChartRenderer extends PieChartRenderer {

    private static final float CIRCLE_RADIUS = 3f;
    private static final float ACTIVITY_SPACING = Utils.convertDpToPixel(50f);
    private final TextPaint entryLabelsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ActivityPieChartRenderer(PieChart chart) {
        super(chart, chart.getAnimator(), chart.getViewPortHandler());

        entryLabelsPaint.setTextAlign(Paint.Align.CENTER);

        entryLabelsPaint.setTextSize(Utils.convertDpToPixel(14f));
    }

    protected void drawEntryLabel(Canvas canvas, String label, float x, float y, int color, int width) {
        entryLabelsPaint.setColor(color);

        StaticLayout staticLayout;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            staticLayout = StaticLayout.Builder
                    .obtain(label, 0, label.length(), entryLabelsPaint, width)
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .setMaxLines(2)
                    .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL)
                    .build();
        } else {
            staticLayout = StaticLayoutWithMaxLines.create(label, 0, label.length(), entryLabelsPaint,
                    width,
                    Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true, TextUtils.TruncateAt.END, width, 2);
        }

        canvas.save();
        canvas.translate(x, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    public void drawValues(Canvas canvas) {
        MPPointF center = mChart.getCenterCircleBox();

        // get whole the radius
        float radius = mChart.getRadius();
        float rotationAngle = mChart.getRotationAngle();
        float[] drawAngles = mChart.getDrawAngles();
        float[] absoluteAngles = mChart.getAbsoluteAngles();

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        final float roundedRadius = (radius - (radius * mChart.getHoleRadius() / 100f)) / 2f;
        final float holeRadiusPercent = mChart.getHoleRadius() / 100f;
        float labelRadiusOffset = (radius - (radius * holeRadiusPercent)) / 2f;

        if (mChart.isDrawHoleEnabled() &&
                !mChart.isDrawSlicesUnderHoleEnabled() &&
                mChart.isDrawRoundedSlicesEnabled()) {
            // Add curved circle slice and spacing to rotation angle, so that it sits nicely inside
            rotationAngle += roundedRadius * 360 / (Math.PI * 2 * radius);
        }

        final float labelRadius = radius - labelRadiusOffset;

        PieData data = mChart.getData();
        List<IPieDataSet> dataSets = data.getDataSets();

        float yValueSum = data.getYValueSum();

        boolean drawEntryLabels = mChart.isDrawEntryLabelsEnabled();

        canvas.save();

        float offset = Utils.convertDpToPixel(5f);

        for (int i = 0; i < dataSets.size(); i++) {
            IPieDataSet dataSet = dataSets.get(i);

            final boolean drawValues = dataSet.isDrawValuesEnabled();

            if (!drawValues && !drawEntryLabels) {
                continue;
            }

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet);

            ValueFormatter formatter = dataSet.getValueFormatter();

            int entryCount = dataSet.getEntryCount();

            mValueLinePaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getValueLineWidth()));

            final float sliceSpace = getSliceSpace(dataSet);

            List<PieChartHelperElement> firstQuarter = new ArrayList<>();
            List<PieChartHelperElement> secondQuarter = new ArrayList<>();
            List<PieChartHelperElement> thirdQuarter = new ArrayList<>();
            List<PieChartHelperElement> fourthQuarter = new ArrayList<>();

            float angle;
            for (int j = 0; j < entryCount; j++) {
                if (j == 0) {
                    angle = 0f;
                } else {
                    angle = absoluteAngles[j - 1] * phaseX;
                }

                final float sliceAngle = drawAngles[j];
                final float sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius);

                final float angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f;

                angle = angle + angleOffset;

                final float transformedAngle;

                if (entryCount == 1) {
                    transformedAngle = rotationAngle + 45 * phaseY;
                } else {
                    transformedAngle = rotationAngle + angle * phaseY;
                }

                if (transformedAngle % 360 >= 0 && transformedAngle % 360 < 90) {
                    firstQuarter.add(new PieChartHelperElement(j));
                } else if (transformedAngle % 360 >= 90 && transformedAngle % 360 <= 180) {
                    secondQuarter.add(new PieChartHelperElement(j));
                } else if (transformedAngle % 360 > 180 && transformedAngle % 360 <= 270) {
                    thirdQuarter.add(new PieChartHelperElement(j));
                } else {
                    fourthQuarter.add(new PieChartHelperElement(j));
                }
            }

            Collections.reverse(secondQuarter);
            Collections.reverse(fourthQuarter);

            List<PieChartHelperElement> all = new ArrayList<>();
            all.addAll(firstQuarter);
            all.addAll(secondQuarter);
            all.addAll(thirdQuarter);
            all.addAll(fourthQuarter);

            for (int j = 0; j < all.size(); j++) {
                PieEntry entry = dataSet.getEntryForIndex(all.get(j).getId());

                if (all.get(j).getId() == 0) {
                    angle = 0f;
                } else {
                    angle = absoluteAngles[all.get(j).getId() - 1] * phaseX;
                }

                final float sliceAngle = drawAngles[all.get(j).getId()];
                final float sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius);

                // offset needed to center the drawn text in the slice
                final float angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2.f) / 2.f;

                angle = angle + angleOffset;

                final float transformedAngle;
                if (entryCount == 1) {
                    transformedAngle = rotationAngle + 45 * phaseY;
                } else {
                    transformedAngle = rotationAngle + angle * phaseY;
                }

                float value = mChart.isUsePercentValuesEnabled() ? entry.getY() / yValueSum * 100f : entry.getY();
                String activityPercent = formatter.getPieLabel(value, entry);
                String entryLabel = entry.getLabel();

                final float sliceXBase = (float) Math.cos(transformedAngle * Utils.FDEG2RAD);
                final float sliceYBase = (float) Math.sin(transformedAngle * Utils.FDEG2RAD);

                final float valueLinePart1OffsetPercentage = dataSet.getValueLinePart1OffsetPercentage() / 100f;

                float line1Radius = (radius - (radius * holeRadiusPercent))
                        * valueLinePart1OffsetPercentage
                        + (radius * holeRadiusPercent);

                final float pt0x = line1Radius * sliceXBase + center.x;
                final float pt0y = line1Radius * sliceYBase + center.y;

                float valueLineLength1 = 1.0f;

                float pt1x = labelRadius * (1 + valueLineLength1) * sliceXBase + center.x;
                float pt1y = labelRadius * (1 + valueLineLength1) * sliceYBase + center.y;

                float baseLineLength = (float) Math.sqrt(Math.pow(pt1x - pt0x, 2) + Math.pow(pt1y - pt0y, 2));

                // TODO: 18.09.2020 Clean up this mess
                for (int k = 0; k < firstQuarter.size(); k++) {
                    if (firstQuarter.get(k).equals(all.get(j)) && k == 0) {
                        firstQuarter.get(k).setY(pt1y);
                    } else if (firstQuarter.get(k).equals(all.get(j))) {
                        float previousY = firstQuarter.get(k - 1).getY();

                        float newPt1y = previousY + ACTIVITY_SPACING;

                        float newLineLength = (float) Math.sqrt(Math.pow(pt1x - pt0x, 2) + Math.pow(newPt1y - pt0y, 2));

                        if (baseLineLength < newLineLength) {
                            pt1y = previousY + ACTIVITY_SPACING;
                        }

                        firstQuarter.get(k).setY(pt1y);
                    }
                }

                for (int k = 0; k < secondQuarter.size(); k++) {
                    if (secondQuarter.get(k).equals(all.get(j)) && k == 0) {
                        secondQuarter.get(k).setY(pt1y);
                    } else if (secondQuarter.get(k).equals(all.get(j))) {
                        float previousY = secondQuarter.get(k - 1).getY();

                        float newPt1y = previousY + ACTIVITY_SPACING;

                        float newLineLength = (float) Math.sqrt(Math.pow(pt1x - pt0x, 2) + Math.pow(newPt1y - pt0y, 2));

                        if (baseLineLength < newLineLength) {
                            pt1y = previousY + ACTIVITY_SPACING;
                        }

                        secondQuarter.get(k).setY(pt1y);
                    }
                }

                for (int k = 0; k < thirdQuarter.size(); k++) {
                    if (thirdQuarter.get(k).equals(all.get(j)) && k == 0) {
                        if (secondQuarter.size() == 0) {
                            thirdQuarter.get(k).setY(pt1y);
                            continue;
                        }

                        float previousY = secondQuarter.get(0).getY();

                        float newPt1y = previousY - ACTIVITY_SPACING;

                        float newLineLength = (float) Math.sqrt(Math.pow(pt1x - pt0x, 2) + Math.pow(newPt1y - pt0y, 2));

                        if (baseLineLength < newLineLength && newPt1y < pt1y) {
                            pt1y = previousY - ACTIVITY_SPACING;
                        }

                        thirdQuarter.get(k).setY(pt1y);
                    } else if (thirdQuarter.get(k).equals(all.get(j))) {
                        float previousY = thirdQuarter.get(k - 1).getY();

                        float newPt1y = previousY - ACTIVITY_SPACING;

                        float newLineLength = (float) Math.sqrt(Math.pow(pt1x - pt0x, 2) + Math.pow(newPt1y - pt0y, 2));

                        if (baseLineLength < newLineLength) {
                            pt1y = previousY - ACTIVITY_SPACING;
                        }

                        thirdQuarter.get(k).setY(pt1y);
                    }
                }

                for (int k = 0; k < fourthQuarter.size(); k++) {
                    if (fourthQuarter.get(k).equals(all.get(j)) && k == 0) {
                        if (firstQuarter.size() == 0) {
                            fourthQuarter.get(k).setY(pt1y);
                            continue;
                        }

                        float previousY = firstQuarter.get(0).getY();

                        float newPt1y = previousY - ACTIVITY_SPACING;

                        float newLineLength = (float) Math.sqrt(Math.pow(pt1x - pt0x, 2) + Math.pow(newPt1y - pt0y, 2));

                        if (baseLineLength < newLineLength && newPt1y < pt1y) {
                            pt1y = previousY - ACTIVITY_SPACING;
                        }

                        fourthQuarter.get(k).setY(pt1y);
                    } else if (fourthQuarter.get(k).equals(all.get(j))) {
                        float previousY = fourthQuarter.get(k - 1).getY();

                        float newPt1y = previousY - ACTIVITY_SPACING;

                        float newLineLength = (float) Math.sqrt(Math.pow(pt1x - pt0x, 2) + Math.pow(newPt1y - pt0y, 2));

                        if (baseLineLength < newLineLength) {
                            pt1y = previousY - ACTIVITY_SPACING;
                        }

                        fourthQuarter.get(k).setY(pt1y);
                    }
                }

                // TODO: 09.09.2020 Use some better indicator than empty label = Others activity
                if (entryLabel.isEmpty()) {
                    entryLabel = mChart.getContext().getResources().getString(R.string.others_activity_name);
                    entryLabelsPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
                }

                float pt2x;
                float pt2y;
                float labelPtx;
                float labelPty;

                if (transformedAngle % 360 >= 90 && transformedAngle % 360 <= 270) {
                    if (mChart.getContext().getResources().getConfiguration().orientation ==
                            Configuration.ORIENTATION_LANDSCAPE) {
                        pt2x = pt1x - Utils.convertDpToPixel(180f);

                        if (pt2x < mChart.getLeft()) {
                            pt2x = mChart.getLeft();
                        }
                    } else {
                        pt2x = mChart.getLeft();
                    }

                    pt2y = pt1y;

                    mValuePaint.setTextAlign(Paint.Align.LEFT);
                    entryLabelsPaint.setTextAlign(Paint.Align.LEFT);

                    labelPtx = pt2x + offset;
                    labelPty = pt2y;

                    drawEntryLabel(canvas, entryLabel, labelPtx, labelPty,
                            dataSet.getValueTextColor(all.get(j).getId()), (int) Math.abs(labelPtx - pt1x));

                    circlePaint.setColor(dataSet.getValueTextColor(all.get(j).getId()));
                    canvas.drawCircle(pt2x, pt2y, Utils.convertDpToPixel(CIRCLE_RADIUS), circlePaint);
                } else {
                    if (mChart.getContext().getResources().getConfiguration().orientation ==
                            Configuration.ORIENTATION_LANDSCAPE) {
                        pt2x = pt1x + Utils.convertDpToPixel(180f);

                        if (pt2x > mChart.getRight() - Utils.convertDpToPixel(10f)) {
                            pt2x = mChart.getRight() - Utils.convertDpToPixel(10f);
                        }
                    } else {
                        pt2x = mChart.getRight() - Utils.convertDpToPixel(10f);
                    }

                    pt2y = pt1y;

                    mValuePaint.setTextAlign(Paint.Align.RIGHT);
                    entryLabelsPaint.setTextAlign(Paint.Align.RIGHT);

                    labelPtx = pt2x - offset;
                    labelPty = pt2y;

                    drawEntryLabel(canvas, entryLabel, labelPtx, labelPty,
                            dataSet.getValueTextColor(all.get(j).getId()), (int) (pt2x - pt1x - offset));

                    circlePaint.setColor(dataSet.getValueTextColor(all.get(j).getId()));
                    canvas.drawCircle(pt2x - Utils.convertDpToPixel(2f), pt2y, Utils.convertDpToPixel(CIRCLE_RADIUS),
                            circlePaint);
                }

                entryLabelsPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

                mValueLinePaint.setColor(dataSet.getColor(all.get(j).getId()));

                canvas.drawLine(pt0x, pt0y, pt1x, pt1y, mValueLinePaint);
                canvas.drawLine(pt1x, pt1y, pt2x, pt2y, mValueLinePaint);

                // Display hours above the line
                if (entry.getData() != null) {
                    float previousSize = mValueLinePaint.getTextSize();
                    mValuePaint.setTextSize(Utils.convertDpToPixel(14f));

                    drawValue(canvas, entry.getData().toString(), labelPtx, labelPty - Utils.convertDpToPixel(5),
                            dataSet.getValueTextColor(all.get(j).getId()));

                    mValuePaint.setTextSize(Utils.convertDpToPixel(previousSize));
                }

                if (value < Constants.DISPLAY_PERCENTAGES_FROM) {
                    continue;
                }

                float x = labelRadius * sliceXBase + center.x;
                float y = labelRadius * sliceYBase + center.y;

                mValuePaint.setTextAlign(Paint.Align.CENTER);

                if (entryCount == 1) {
                    drawValue(canvas, activityPercent, center.x, y - Utils.convertDpToPixel(6f), Color.WHITE);
                } else {
                    drawValue(canvas, activityPercent, x, y + Utils.convertDpToPixel(4f), Color.WHITE);
                }
            }
        }
        MPPointF.recycleInstance(center);
        canvas.restore();
    }
}

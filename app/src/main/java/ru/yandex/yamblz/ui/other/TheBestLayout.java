package ru.yandex.yamblz.ui.other;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.getMode;
import static android.view.View.MeasureSpec.getSize;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class TheBestLayout extends ViewGroup {

    public TheBestLayout(Context context) {
        this(context, null, 0);
    }

    public TheBestLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TheBestLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        View childWhichIsMatchParent = null;

        int layoutWidthSize = getSize(widthMeasureSpec); // Если пихнуть этот ViewGroup в HorizontalScrollView, то тут будет ноль, но ничего не сломается
        int layoutWidthMode = getMode(widthMeasureSpec); // А тут будет UNSPECIFIED

        int layoutHeightSize = getSize(heightMeasureSpec); // Если же пихнуть этот ViewGroup в просто ScrollView, то тут будет ноль и всё сломается

        int childWidthMode; // Как дети будут себя мерить по ширине
        if (layoutWidthMode == UNSPECIFIED) {
            childWidthMode = UNSPECIFIED; // Раз нас не ограничивают, то и детей нет смысла ограничивать
        } else {
            childWidthMode = AT_MOST;
        }

        int currentChildWidth, currentChildHeight;
        int totalWidth = 0, totalHeight = 0; // Ширина и высота всех детей
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            LayoutParams childLayoutParams = child.getLayoutParams();

            if (childLayoutParams.width == MATCH_PARENT && childWidthMode != UNSPECIFIED) {
                childWhichIsMatchParent = child; // Потом отдадим ему всё оставшееся место
                continue;
            } else if (childLayoutParams.width != WRAP_CONTENT && childLayoutParams.width != MATCH_PARENT) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(childLayoutParams.width, MeasureSpec.EXACTLY), // android:width = "100dp"
                        MeasureSpec.makeMeasureSpec(layoutHeightSize, AT_MOST)
                );
            } else {
                child.measure(
                        MeasureSpec.makeMeasureSpec(layoutWidthSize, childWidthMode),
                        MeasureSpec.makeMeasureSpec(layoutHeightSize, AT_MOST)
                );
            }

            currentChildWidth = child.getMeasuredWidth();
            currentChildHeight = child.getMeasuredHeight();

            totalWidth += currentChildWidth;
            if (currentChildHeight > totalHeight) { // Так как мы располагаем детей в горизонтальную линию, то высота всей ViewGroup = максимальной высоте ребёнка
                totalHeight = currentChildHeight;
            }
        }

        if (childWhichIsMatchParent != null) {
            if (totalWidth >= layoutWidthSize) { // Значит места для match_parent элемента не осталось, пусть будет каким захочет, сместив всех, кто правее него, в никуда
                childWhichIsMatchParent.measure(
                        MeasureSpec.makeMeasureSpec(layoutWidthSize, UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(layoutHeightSize, AT_MOST)
                );

                currentChildWidth = childWhichIsMatchParent.getMeasuredWidth();
                currentChildHeight = childWhichIsMatchParent.getMeasuredHeight();

                totalWidth += currentChildWidth;
                if (currentChildHeight > totalHeight) {
                    totalHeight = currentChildHeight;
                }

                setMeasuredDimension(totalWidth, totalHeight);
            } else {
                childWhichIsMatchParent.measure(
                        MeasureSpec.makeMeasureSpec(layoutWidthSize - totalWidth, MeasureSpec.EXACTLY), // Растягиваем
                        MeasureSpec.makeMeasureSpec(layoutHeightSize, AT_MOST)
                );

                currentChildHeight = childWhichIsMatchParent.getMeasuredHeight();
                if (currentChildHeight > totalHeight) {
                    totalHeight = currentChildHeight;
                }

                setMeasuredDimension(layoutWidthSize, totalHeight);
            }
        } else {
            setMeasuredDimension(totalWidth, totalHeight); // Если нет match_parent элемента
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int prevChildRight = 0;
        int prevChildBottom = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.layout(prevChildRight, prevChildBottom,
                    prevChildRight + child.getMeasuredWidth(),
                    prevChildBottom + child.getMeasuredHeight());
            prevChildRight += child.getMeasuredWidth();
        }
    }
}
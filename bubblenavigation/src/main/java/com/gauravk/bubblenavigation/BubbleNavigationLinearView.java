/*
        Copyright 2019 Gaurav Kumar

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
        Unless required by applicable com.law.booking or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.gauravk.bubblenavigation;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * BubbleNavigationLinearView
 *
 * @author Gaurav Kumar
 */

@SuppressWarnings("unused")
public class BubbleNavigationLinearView extends LinearLayout implements View.OnClickListener, IBubbleNavigation {

    //constants
    private static final String TAG = "BNLView";
    private static final int MIN_ITEMS = 2;
    private static final int MAX_ITEMS = 5;

    private ArrayList<BubbleToggleView> bubbleNavItems;
    private BubbleNavigationChangeListener navigationChangeListener;

    private int currentActiveItemPosition = 0;
    private boolean loadPreviousState;

    private Typeface currentTypeface;

    private SparseArray<String> pendingBadgeUpdate;

    /**
     * Constructors
     */
    public BubbleNavigationLinearView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public BubbleNavigationLinearView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BubbleNavigationLinearView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("current_item", currentActiveItemPosition);
        bundle.putBoolean("load_prev_state", true);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            currentActiveItemPosition = bundle.getInt("current_item");
            loadPreviousState = bundle.getBoolean("load_prev_state");
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    /////////////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////////////

    /**
     * Initialize
     *
     * @param context current context
     * @param attrs   custom attributes
     */
    private void init(Context context, AttributeSet attrs) {

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        post(new Runnable() {
            @Override
            public void run() {
                updateChildNavItems();
            }
        });
    }

    /**
     * Finds Child Elements of type {@link BubbleToggleView} and adds them to {@link #bubbleNavItems}
     */
    private void updateChildNavItems() {
        bubbleNavItems = new ArrayList<>();
        for (int index = 0; index < getChildCount(); ++index) {
            View view = getChildAt(index);
            if (view instanceof BubbleToggleView)
                bubbleNavItems.add((BubbleToggleView) view);
            else {
                Log.w(TAG, "Cannot have child bubbleNavItems other than BubbleToggleView");
                return;
            }
        }

        if (bubbleNavItems.size() < MIN_ITEMS) {
            Log.w(TAG, "The bubbleNavItems list should have at least 2 bubbleNavItems of BubbleToggleView");
        } else if (bubbleNavItems.size() > MAX_ITEMS) {
            Log.w(TAG, "The bubbleNavItems list should not have more than 5 bubbleNavItems of BubbleToggleView");
        }

        setClickListenerForItems();
        setInitialActiveState();
        updateMeasurementForItems();

        //update the typeface
        if (currentTypeface != null)
            setTypeface(currentTypeface);

        //update the badge count
        if (pendingBadgeUpdate != null && bubbleNavItems != null) {
            for (int i = 0; i < pendingBadgeUpdate.size(); i++)
                setBadgeValue(pendingBadgeUpdate.keyAt(i), pendingBadgeUpdate.valueAt(i));
            pendingBadgeUpdate.clear();
        }
    }

    /**
     * Makes sure that ONLY ONE child {@link #bubbleNavItems} is active
     */
    private void setInitialActiveState() {

        if (bubbleNavItems == null) return;

        boolean foundActiveElement = false;

        // find the initial state
        if (!loadPreviousState) {
            for (int i = 0; i < bubbleNavItems.size(); i++) {
                if (bubbleNavItems.get(i).isActive() && !foundActiveElement) {
                    foundActiveElement = true;
                    currentActiveItemPosition = i;
                } else {
                    bubbleNavItems.get(i).setInitialState(false);
                }
            }
        } else {
            for (int i = 0; i < bubbleNavItems.size(); i++) {
                bubbleNavItems.get(i).setInitialState(false);
            }
        }
        //set the active element
        if (!foundActiveElement)
            bubbleNavItems.get(currentActiveItemPosition).setInitialState(true);
    }

    /**
     * Update the measurements of the child components {@link #bubbleNavItems}
     */
    private void updateMeasurementForItems() {
        int numChildElements = bubbleNavItems.size();
        if (numChildElements > 0) {
            int calculatedEachItemWidth = (getMeasuredWidth() - (getPaddingRight() + getPaddingLeft())) / numChildElements;
            for (BubbleToggleView btv : bubbleNavItems)
                btv.updateMeasurements(calculatedEachItemWidth);
        }
    }

    /**
     * Sets {@link android.view.View.OnClickListener} for the child views
     */
    private void setClickListenerForItems() {
        for (BubbleToggleView btv : bubbleNavItems)
            btv.setOnClickListener(this);
    }

    /**
     * Gets the Position of the Child from {@link #bubbleNavItems} from its id
     *
     * @param id of view to be searched
     * @return position of the Item
     */
    private int getItemPositionById(int id) {
        for (int i = 0; i < bubbleNavItems.size(); i++)
            if (id == bubbleNavItems.get(i).getId())
                return i;
        return -1;
    }

    ///////////////////////////////////////////
    // PUBLIC METHODS
    ///////////////////////////////////////////

    /**
     * Set the navigation change listener {@link BubbleNavigationChangeListener}
     *
     * @param navigationChangeListener sets the passed parameters as listener
     */
    @Override
    public void setNavigationChangeListener(BubbleNavigationChangeListener navigationChangeListener) {
        this.navigationChangeListener = navigationChangeListener;
    }

    /**
     * Set the {@link Typeface} for the Text Elements of the View
     *
     * @param typeface to be used
     */
    @Override
    public void setTypeface(Typeface typeface) {
        if (bubbleNavItems != null) {
            for (BubbleToggleView btv : bubbleNavItems)
                btv.setTitleTypeface(typeface);
        } else {
            currentTypeface = typeface;
        }
    }

    /**
     * Gets the current active position
     *
     * @return active item position
     */
    @Override
    public int getCurrentActiveItemPosition() {
        return currentActiveItemPosition;
    }

    /**
     * Sets the current active item
     *
     * @param position current position change
     */
    @Override
    public void setCurrentActiveItem(int position) {

        if (bubbleNavItems == null) {
            currentActiveItemPosition = position;
            return;
        }

        if (currentActiveItemPosition == position) return;

        if (position < 0 || position >= bubbleNavItems.size())
            return;

        BubbleToggleView btv = bubbleNavItems.get(position);
        btv.performClick();
    }

    /**
     * Sets the badge value
     *
     * @param position current position change
     * @param value    value to be set in the badge
     */
    @Override
    public void setBadgeValue(int position, String value) {
        if (bubbleNavItems != null) {
            BubbleToggleView btv = bubbleNavItems.get(position);
            if (btv != null)
                btv.setBadgeText(value);
        } else {
            if (pendingBadgeUpdate == null)
                pendingBadgeUpdate = new SparseArray<>();
            pendingBadgeUpdate.put(position, value);
        }
    }

    @Override
    public void onClick(View v) {
        int changedPosition = getItemPositionById(v.getId());
        if (changedPosition >= 0) {
            if (changedPosition == currentActiveItemPosition) {
                return;
            }
            BubbleToggleView currentActiveToggleView = bubbleNavItems.get(currentActiveItemPosition);
            BubbleToggleView newActiveToggleView = bubbleNavItems.get(changedPosition);
            if (currentActiveToggleView != null)
                currentActiveToggleView.toggle();
            if (newActiveToggleView != null)
                newActiveToggleView.toggle();

            //changed the current active position
            currentActiveItemPosition = changedPosition;

            if (navigationChangeListener != null)
                navigationChangeListener.onNavigationChanged(v, currentActiveItemPosition);
        } else {
            Log.w(TAG, "Selected id not found! Cannot toggle");
        }
    }
}

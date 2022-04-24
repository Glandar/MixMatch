package com.example.myapplication;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopUpFragment {

    public void showPopupWindow(final View view, int[][] goalStack, boolean sepStacks, int[] stackSize, int goals, int base, int background, boolean cipher, char[] cipher_map) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = View.inflate(view.getContext(), R.layout.fragment_popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);

        LinearLayout ll1 = popupView.findViewById(R.id.popup_ll1);
        ll1.setBackgroundResource(background);

        if (sepStacks || goals == 1) {
            ll1.setOrientation(LinearLayout.VERTICAL);
            for (int i = 0; i < goalStack[0].length; i++) {
                TextView goal = (TextView) inflater.inflate(R.layout.item_textview, ll1, false);
                goal.setText(cipher(goalStack[0][i], base, cipher, cipher_map));
                goal.setTextSize(100);
                if (i >= goalStack[0].length - goals) {
                    if (goalStack[1][i] == i) {
                        goal.setTextColor(0xFFFFFF00);
                    } else {
                        goal.setTextColor(0xFF00FF00);
                    }
                } else if (goalStack[1][i] == 0) {
                    goal.setTextColor(0xFFFF0000);
                } else if (goalStack[1][i] == -1) {
                    goal.setTextColor(0xFFFFFF00);
                } else {
                    goal.setTextColor(0xFF00FF00);
                }
                ll1.addView(goal);
            }
        } else {
            for (int i = 0; i < goals; i++) {
                LinearLayout ll2 = new LinearLayout(view.getContext());
                ll2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                ll2.setOrientation(LinearLayout.VERTICAL);
                ll1.addView(ll2);
                for (int j = 0; j < goalStack[i].length; j++) {
                    TextView goal = (TextView) inflater.inflate(R.layout.item_textview, ll2, false);
                    goal.setText(cipher(goalStack[i][j], base, cipher, cipher_map));
                    goal.setTextSize(40);
                    goal.setGravity(Gravity.END);
                    if (j > stackSize[i]) {
                        goal.setTextColor(0xFF00FF00);
                    } else if (j == stackSize[i]) {
                        goal.setTextColor(0xFFFFFF00);
                    } else {
                        goal.setTextColor(0xFFFF0000);
                    }
                    ll2.addView(goal);
                }
            }
        }

        popupView.setOnTouchListener((v, event) -> {
            v.performClick();
            popupWindow.dismiss();
            return true;
        });
    }

    private String cipher(int number, int base, boolean cipher, char[] cipher_map) {
        String text = Integer.toString(number, base);
        if (!cipher) {
            return text;
        }
        char[] digits = text.toCharArray();
        for (int i = 0; i < digits.length; i++) {
            digits[i] = cipher_map[Integer.parseInt(Character.toString(digits[i]), base)];
        }
        return String.valueOf(digits);
    }
}
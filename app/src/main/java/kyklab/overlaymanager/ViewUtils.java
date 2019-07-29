package kyklab.overlaymanager;

import android.view.View;

class ViewUtils {
    public static void animateShowInOrder(final View[] views, float transitionX, float transitionY, long duration, long startDelay) {
        // Animate from top to bottom, but give upper views more start delay
        for (int i = 0, j = views.length; i < j; ++i) {
            final int finalI = i;
            final int showOrder = j - i - 1;
            views[finalI].clearAnimation();
            views[finalI].animate()
                    .alpha(1f)
                    .translationX(transitionX)
                    .translationY(transitionY)
                    .setDuration(duration)
                    .setStartDelay(startDelay * showOrder)
                    .withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            views[finalI].setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    public static void animateHideInOrder(final View[] views, float transitionX, float transitionY, long duration, long startDelay) {
        // Animate from top to bottom
        for (int i = 0, j = views.length; i < j; ++i) {
            final int finalI = i;
            views[finalI].clearAnimation();
            views[finalI].animate()
                    .alpha(0f)
                    .translationX(transitionX)
                    .translationY(transitionY)
                    .setDuration(duration)
                    .setStartDelay(startDelay * i)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            views[finalI].setVisibility(View.GONE);
                        }
                    });
        }
    }
}

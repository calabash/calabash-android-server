package sh.calaba.instrumentationbackend.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WindowManagerWrapperTest {
    @Test
    public void usesSystemServiceIfAvailable() {
        MockContextAfterOnCreate mockContext = new MockContextAfterOnCreate();
        WindowManagerWrapper.fromContext(mockContext);

        assertEquals("Expected context to receive getSystemService(Context.WINDOW_SERVICE)", true, mockContext.hasRequestedSystemService());
    }

    /*
     * See: https://github.com/calabash/calabash-android/issues/766
     */
    @Test
    public void defaultsIfSystemServiceIsNotAvailable() {
        MockContextBeforeOnCreate mockContext = new MockContextBeforeOnCreate();
        WindowManagerWrapper.fromContext(mockContext);
    }

    private static class MockContextBeforeOnCreate extends Activity {
        /*
         * This is how Android behaves before onCreate has been called
         */
        @Override
        public Object getSystemService(String name) {
            throw new IllegalStateException("System services not available to Activities before onCreate()");
        }
    }

    private static class MockContextAfterOnCreate extends Activity {
        private boolean hasRequestedSystemService = false;

        @Override
        public Object getSystemService(String name) {
            if (name.equals(Context.WINDOW_SERVICE)) {
                hasRequestedSystemService = true;
                return new MockWindowService();
            }

            throw new IllegalArgumentException("Expected name to be Context.WINDOW_SERVICE, not '" + name + "'");
        }

        public boolean hasRequestedSystemService() {
            return hasRequestedSystemService;
        }
    }

    private static class MockWindowService implements WindowManager {
        @Override
        public Display getDefaultDisplay() {
            return null;
        }

        @Override
        public void removeViewImmediate(View view) {

        }

        @Override
        public void addView(View view, ViewGroup.LayoutParams params) {

        }

        @Override
        public void updateViewLayout(View view, ViewGroup.LayoutParams params) {

        }

        @Override
        public void removeView(View view) {

        }
    }
}

package sh.calaba.instrumentationbackend;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class FakePickerActivity extends Activity {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        Intent result = new Intent();
        result.setData(Uri.parse("file:///sdcard/DCIM/100MEDIA/IMAG0001.jpg"));
        result.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}

package sh.calaba.instrumentationbackend.actions.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.instrumentationbackend.Result;
import sh.calaba.instrumentationbackend.actions.Action;

public class Version implements Action {
	private static final String VersionPath="version";

	@Override
    public Result execute(String... args) {
        String version;

        try {
            InputStream is = InstrumentationBackend.instrumentation.getContext().getResources().getAssets().open(VersionPath);
            BufferedReader input =  new BufferedReader(new InputStreamReader(is));
            version = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }

	    return new Result(true, version);
    }

    @Override
    public String key() {
        return "version";
    }

}

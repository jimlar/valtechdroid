package se.jimlar;

public class Logger {
    private String tag;

    public Logger(Class clazz) {
        this.tag = clazz.getSimpleName();
    }

    public void debug(String msg) {
        android.util.Log.d(tag, msg);
    }

    public void info(String msg) {
        android.util.Log.i(tag, msg);
    }

    public void warn(String msg, Throwable throwable) {
        android.util.Log.w(tag, msg, throwable);
    }

    public void error(String msg, Throwable throwable) {
        android.util.Log.e(tag, msg, throwable);
    }
}

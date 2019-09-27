package kyklab.overlaymanager.utils;

import android.os.AsyncTask;

public class Utils {
    public static boolean isTaskRunning(AsyncTask task) {
        return task != null && task.getStatus() == AsyncTask.Status.RUNNING;
    }

    public static boolean taskNeedsResume(AsyncTask task) {
        return task != null && task.isCancelled();
    }

    public static boolean isTaskExecutable(AsyncTask task) {
        return task == null ||
                (task.getStatus() != AsyncTask.Status.RUNNING
                        && task.getStatus() != AsyncTask.Status.PENDING);
    }
}

package co.blustor.gatekeeperdemo.dialogs;

import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;

import co.blustor.gatekeeperdemo.R;

public abstract class RequestBluetoothDialogFragment extends OkCancelDialogFragment {
    public static final String TAG = RequestBluetoothDialogFragment.class.getSimpleName();

    @Override
    protected void onBuildDialog(android.support.v7.app.AlertDialog.Builder builder) {
        setTitle(R.string.gkcard_bluetooth_requested_title);
        setMessage(R.string.gkcard_bluetooth_requested_message);
        setPositiveLabel(R.string.enable);
        super.onBuildDialog(builder);
    }

    protected abstract void onBluetoothEnabled();

    @Override
    protected void onOkay() {
        waitForBluetooth();
        BluetoothAdapter.getDefaultAdapter().enable();
    }

    private void waitForBluetooth() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                while (!defaultAdapter.isEnabled()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dismiss();
                onBluetoothEnabled();
            }
        }.execute();
    }
}

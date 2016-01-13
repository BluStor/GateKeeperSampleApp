package co.blustor.gatekeeperdemo.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

public abstract class OkCancelDialogFragment extends DialogFragment {
    private int mTitleResource = -1;
    private int mMessageResource = -1;
    private int mPositiveButtonResource = android.R.string.ok;
    private int mNegativeButtonResource = android.R.string.cancel;

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        setCancelable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        onBuildDialog(builder);
        return builder.create();
    }

    protected void onBuildDialog(AlertDialog.Builder builder) {
        if (isValidResource(mTitleResource)) {
            builder.setTitle(getString(mTitleResource));
        }
        if (isValidResource(mMessageResource)) {
            builder.setMessage(getString(mMessageResource));
        }
        builder.setPositiveButton(mPositiveButtonResource, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onOkay();
            }
        });
        builder.setNegativeButton(mNegativeButtonResource, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCancel();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    onCancel();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    protected void setTitle(int resourceId) {
        mTitleResource = resourceId;
    }

    protected void setMessage(int resourceId) {
        mMessageResource = resourceId;
    }

    protected void setPositiveLabel(int resourceId) {
        mPositiveButtonResource = resourceId;
    }

    protected void setNegativeLabel(int resourceId) {
        mNegativeButtonResource = resourceId;
    }

    protected abstract void onOkay();

    protected void onCancel() {
    }

    private boolean isValidResource(int resourceId) {
        try {
            String resourceName = getResources().getResourceName(resourceId);
            return resourceName.startsWith("co.blustor");
        } catch (Resources.NotFoundException e) {
            return false;
        }
    }
}

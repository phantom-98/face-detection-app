package com.facecoolalert.common;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facecoolalert.R;

public class PromptDialog {

    private TextView content;
    private Button buttonAccept;
    private Button buttonCancel;
    private Context context;
    private Dialog dialog;

    public PromptDialog(Context context)
    {
        this.context=context;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_prompt);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);



        this.buttonCancel = dialog.findViewById(R.id.cancel_btn);
        this.buttonAccept=dialog.findViewById(R.id.accept_btn);
        this.content=dialog.findViewById(R.id.alert_dialog_title);

        View.OnClickListener defaultListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        };
        buttonAccept.setOnClickListener(defaultListener);
        buttonCancel.setOnClickListener(defaultListener);

    }

    public void setText(String str){
        content.setText(str);
    }

    public void show() {

        dialog.show();
    }

    public void dismiss()
    {
        dialog.dismiss();
    }

    public void setOnCancelListener(View.OnClickListener listener)
    {
        buttonCancel.setOnClickListener(listener);
    }

    public void setOnAcceptListener(View.OnClickListener listener)
    {
        buttonAccept.setOnClickListener(listener);
    }

}

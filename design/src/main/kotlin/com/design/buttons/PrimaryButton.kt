package com.design.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.design.R

class PrimaryButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val progressBar: ProgressBar
    val buttonTextView: TextView

    private var state = State.ENABLED
    private var onClickListener: OnClickListener? = null

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.button_primary, this, true)
        buttonTextView = root.findViewById(R.id.primary_button_text)
        progressBar = root.findViewById(R.id.primary_progress_indicator)
    }


    override fun setOnClickListener(l: OnClickListener?) {
        if (l != null)
            onClickListener = l
        super.setOnClickListener(l)
    }

    fun setState(newState: State) {
        state = newState
        when (state) {
            State.ENABLED -> {
                buttonTextView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                setOnClickListener(onClickListener)
            }
            State.DISABLED -> {
                buttonTextView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                setOnClickListener(null)
            }
            State.LOADING -> {
                buttonTextView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                setOnClickListener(null)
            }
        }
    }

    fun setText(text: String?) {
        buttonTextView.text = text
    }

    enum class State {
        ENABLED, DISABLED, LOADING
    }

}
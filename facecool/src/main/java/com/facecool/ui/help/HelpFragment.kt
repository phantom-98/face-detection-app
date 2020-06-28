package com.facecool.ui.help

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.facecool.R
import com.facecool.databinding.FragmentHelpBinding
import com.facecool.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HelpFragment : BaseFragment<FragmentHelpBinding>(FragmentHelpBinding::inflate) {

    private val viewModel: HelpViewModel by viewModels()

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.help_title)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendHelpEmail.setOnClickListener {
            composeEmail(arrayOf(getString(R.string.help_support_email)),getString(R.string.help_support_msg))
        }

    }

    private fun composeEmail(addresses: Array<String>, subject: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")  // Specifies the "mailto" scheme
            putExtra(Intent.EXTRA_EMAIL, addresses)  // Email address of the recipient
            putExtra(Intent.EXTRA_SUBJECT, subject)  // Subject of the email
            putExtra(Intent.EXTRA_TEXT, "")  // Body of the email
        }
        requireActivity().startActivity(Intent.createChooser(emailIntent, getString(R.string.help_choose_email_provider)));
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onDestroyView() {
        super.onDestroyView()
//        activity?.let {
//            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        }
    }

}

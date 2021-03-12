package com.edipasquale.todo.view.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.edipasquale.todo.util.PrefUtils
import com.edipasquale.todo.viewmodel.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class AuthFragment : Fragment() {
    private val _viewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewModel.authenticate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel.authModel.observe(viewLifecycleOwner, { token ->
            PrefUtils.saveAccessToken(requireContext(), token)

            onAuthenticated(token)
        })

        _viewModel.errorModel.observe(viewLifecycleOwner, { message ->
            onAuthenticationError(message)
        })
    }

    abstract fun onAuthenticated(token: String)

    abstract fun onAuthenticationError(errorMessage: String)
}
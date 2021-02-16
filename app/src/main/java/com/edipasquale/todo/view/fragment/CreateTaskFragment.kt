package com.edipasquale.todo.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.edipasquale.todo.R
import com.edipasquale.todo.databinding.FragmentCreateTaskBinding
import com.edipasquale.todo.viewmodel.CreateTaskViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateTaskFragment : Fragment() {

    private lateinit var _binding: FragmentCreateTaskBinding
    private val _viewModel: CreateTaskViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTaskBinding.inflate(inflater, container, false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding.buttonCreateTask.setOnClickListener {
            _viewModel.createTask(
                _binding.nameInputView.text.toString(),
                _binding.noteInputView.text.toString()
            )
        }

        _viewModel.taskCreation.observe(viewLifecycleOwner, {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        })
    }

    override fun onDestroyView() {
        hideSoftKeyboard()
        super.onDestroyView()
    }

    private fun hideSoftKeyboard() {
        val view = activity?.currentFocus
        view?.let { v ->
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}
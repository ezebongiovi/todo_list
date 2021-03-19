package com.edipasquale.todo.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
        _binding = createBinding(inflater, container)

        return _binding.root
    }

    override fun onDestroyView() {
        hideSoftKeyboard()
        super.onDestroyView()
    }

    private fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCreateTaskBinding.inflate(inflater, container, false).apply {
        buttonCreateTask.setOnClickListener {
            val name = _binding.nameInputView.text.toString()
            val note = _binding.noteInputView.text.toString()

            createTask(name, note)
        }
    }

    private fun createTask(name: String, note: String) {
        _viewModel.createTask(name, note).invokeOnCompletion {
            val destination = CreateTaskFragmentDirections.tasksFragment()

            findNavController().navigate(destination)
        }
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
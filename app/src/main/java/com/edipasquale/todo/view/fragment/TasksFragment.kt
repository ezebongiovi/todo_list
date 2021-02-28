package com.edipasquale.todo.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edipasquale.todo.R
import com.edipasquale.todo.databinding.FragmentTasksBinding
import com.edipasquale.todo.view.adapter.TasksAdapter
import com.edipasquale.todo.view.fragment.base.AuthFragment
import com.edipasquale.todo.viewmodel.TasksViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class TasksFragment : AuthFragment() {

    private val _viewModel: TasksViewModel by viewModel()
    private lateinit var _binding: FragmentTasksBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)

        _binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        _binding.refreshLayout.setOnRefreshListener {
            _viewModel.pullToRefresh()
        }

        return _binding.root
    }

    override fun onAuthenticated(token: String) {
        render()
    }

    override fun onAuthenticationError(errorMessage: String) {
        render()
    }

    private fun render() {
        val adapter = TasksAdapter()
        _binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        _binding.recyclerView.adapter = adapter

        _viewModel.tasks.observe(viewLifecycleOwner, { tasks ->
            _binding.progress.visibility = View.GONE

            adapter.submitList(tasks)
        })

        _viewModel.error.observe(viewLifecycleOwner, { error ->
            _binding.refreshLayout.isRefreshing = false

            if (error == null)
                return@observe

            showErrorMmessage(
                error.errorDescription ?: getString(R.string.error_unknown_fetch_tasks)
            )
        })
    }

    private fun showErrorMmessage(message: String) {
        Snackbar.make(_binding.root, message, Snackbar.LENGTH_INDEFINITE).show()
    }
}
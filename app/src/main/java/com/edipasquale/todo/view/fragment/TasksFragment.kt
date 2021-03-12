package com.edipasquale.todo.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edipasquale.todo.R
import com.edipasquale.todo.databinding.FragmentTasksBinding
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.view.adapter.TasksAdapter
import com.edipasquale.todo.view.fragment.base.AuthFragment
import com.edipasquale.todo.viewmodel.TasksViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class TasksFragment : AuthFragment() {

    private val _viewModel: TasksViewModel by viewModel()
    private lateinit var _binding: FragmentTasksBinding
    private val _adapter = TasksAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = createBinding(inflater, container)

        return _binding.root
    }

    override fun onAuthenticated(token: String) {
        render()
    }

    override fun onAuthenticationError(errorMessage: String) {
        render()
    }

    private fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTasksBinding.inflate(inflater, container, false).apply {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = _adapter

        refreshLayout.setOnRefreshListener { _viewModel.pullToRefresh() }

        fab.setOnClickListener {
            val destination = TasksFragmentDirections.createTaskFragment()

            findNavController().navigate(destination)
        }
    }

    private fun render() {
        _viewModel.tasks.observe(viewLifecycleOwner, { tasks ->
            _binding.refreshLayout.isRefreshing = false

            renderTasks(tasks)
        })

        _viewModel.error.observe(viewLifecycleOwner, { error ->
            _binding.refreshLayout.isRefreshing = false

            renderError(error)
        })
    }

    private fun renderError(error: APIError?) {
        if (error == null)
            return

        showErrorMmessage(error.errorDescription ?: getString(R.string.error_unknown_fetch_tasks))
    }

    private fun renderTasks(tasks: List<TaskEntity>) {
        _binding.progress.visibility = View.GONE

        _adapter.submitList(tasks)
    }

    private fun showErrorMmessage(message: String) {
        Snackbar.make(_binding.root, message, Snackbar.LENGTH_INDEFINITE).show()
    }
}
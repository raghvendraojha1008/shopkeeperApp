──
package com.shopkeeper.ledger.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.shopkeeper.ledger.R

abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: error("Binding accessed after view destroyed")

    private var shimmerContainer: ShimmerFrameLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shimmerContainer = view.findViewById(R.id.shimmer_container)
        initViews()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun initViews()

    abstract fun observeViewModel()

    fun showLoading() {
        shimmerContainer?.apply {
            visibility = View.VISIBLE
            startShimmer()
        }
    }

    fun hideLoading() {
        shimmerContainer?.apply {
            stopShimmer()
            visibility = View.GONE
        }
    }

    fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(resources.getColor(R.color.error_color, requireContext().theme))
                .setTextColor(resources.getColor(android.R.color.white, requireContext().theme))
                .show()
        }
    }

    fun showSuccess(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(resources.getColor(R.color.success_color, requireContext().theme))
                .setTextColor(resources.getColor(android.R.color.white, requireContext().theme))
                .show()
        }
    }

    fun nav(@IdRes actionId: Int) {
        findNavController().navigate(actionId)
    }

    fun navWithArgs(@IdRes actionId: Int, args: Bundle) {
        findNavController().navigate(actionId, args)
    }
}
xml<!-- ───
──
package com.shopkeeper.ledger

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.shopkeeper.ledger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val topLevelDestinations = setOf(
        R.id.dashboardFragment,
        R.id.inventoryFragment,
        R.id.commandBottomSheet,
        R.id.partiesFragment,
        R.id.settingsFragment
    )

    private val hideNavDestinations = setOf(
        R.id.splashFragment,
        R.id.lockFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when {
                destination.id in hideNavDestinations -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }
                destination.id in topLevelDestinations -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }
            }
        }
    }

    fun lockApp() {
        navController.navigate(R.id.action_any_to_lock)
        binding.bottomNavigationView.visibility = View.GONE
    }

    fun unlockApp() {
        navController.popBackStack(R.id.lockFragment, true)
        binding.bottomNavigationView.visibility = View.VISIBLE
    }
}
kotlin// ───
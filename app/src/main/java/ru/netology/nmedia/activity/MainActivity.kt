package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    private val navController by lazy(::getNavigateController)

    fun getNavigateController() : NavController {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container_for_fragments) as NavHostFragment
        return navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_auth, menu)
        menu?.setGroupVisible(R.id.group_authorized, viewModel.authorized)
        menu?.setGroupVisible(R.id.group_unauthorized, !viewModel.authorized)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =

        when (item.itemId) {
            R.id.item_auth -> {
                navController.navigate(R.id.nav_auth_fragment)
                true
            }
            R.id.item_registration -> {
                navController.navigate(R.id.nav_auth_fragment)
                true
            }
            R.id.item_logout -> {
                AppAuth.getInstance().clearAuth()
                true
            }
            else -> {
                false
            }
        }



}
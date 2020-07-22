package com.luseen.spacelib

import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.navigation.fragment.NavHostFragment

/**
 * Навигация по space navigation view, где для каждого [SpaceItem]
 * должен быть отдельный граф со своим бэкстеком.
 */
fun SpaceNavView.setupWithNavigationController(
    fragmentManager: FragmentManager,
    @IdRes containerId: Int,
    onCenterButtonClick: () -> Unit,
    navigationGraphIdCreator: (SpaceItem) -> Int
) {
    post {
        setup(
            spaceNavView = this,
            fragmentManager = fragmentManager,
            containerId = containerId,
            onCenterButtonClick = onCenterButtonClick,
            navigationGraphIdCreator = navigationGraphIdCreator
        )
    }
}

private fun setup(
    spaceNavView: SpaceNavView,
    fragmentManager: FragmentManager,
    @IdRes containerId: Int,
    onCenterButtonClick: () -> Unit,
    navigationGraphIdCreator: (SpaceItem) -> Int
) {
    val defaultItem = spaceNavView.selectedSpaceItem
    var selected = defaultItem

    val navigationHosts: Map<SpaceItem, Lazy<NavHostFragment>> = spaceNavView.getAllItems()
        .map { item -> item to navigationGraphIdCreator(item) }
        .map { (item, graphId) ->
            item to lazy {
                findOrCreateNavigationHost(
                    graphResId = graphId,
                    tag = item.tag(),
                    fragmentManager = fragmentManager,
                    containerId = containerId
                )
            }
        }
        .toMap()


    navigationHosts.present(fragmentManager, defaultItem)

    spaceNavView.spaceOnClickListener = object : SpaceOnClickListener {
        override fun onCenterButtonClick() = onCenterButtonClick()

        override fun onSpaceItemClick(index: Int) {
            if (fragmentManager.isStateSaved.not()) {
                val item = spaceNavView.getItem(index)
                selected = item
                navigationHosts.present(fragmentManager, item)
            }
        }

        override fun onSpaceItemReselected(index: Int) {
            navigationHosts.getValue(spaceNavView.getItem(index)).value.also { host: NavHostFragment ->
                // Вариант `navController.popBackStack(navController.graph.startDestination, false)`
                // тут не подходит, т.к. если первый фрагмент `inclusive`, то навигация не будет совершена.
                repeat(host.childFragmentManager.backStackEntryCount) {
                    host.navController.popBackStack()
                }
            }
        }
    }

    spaceNavView.spaceOnLongClickListener = object : SpaceOnLongClickListener {
        override fun onCenterButtonLongClick() {
            spaceNavView.spaceOnClickListener?.onCenterButtonClick()
        }

        override fun onSpaceItemLongClick(index: Int) {
            spaceNavView.spaceOnClickListener?.onSpaceItemClick(index)
        }
    }

    fragmentManager.addOnBackStackChangedListener {
        if (selected != defaultItem && !fragmentManager.isOnBackStack(defaultItem.tag())) {
            selected = defaultItem
        }
        navigationHosts
            .getValue(selected).value
            .navController
            .also {
                if (it.currentDestination == null) {
                    it.navigate(it.graph.id)
                }
            }
    }
}

private fun SpaceItem.tag(): String {
    return "bottomNavigationHost#$name"
}

private fun findOrCreateNavigationHost(
    @NavigationRes graphResId: Int,
    tag: String,
    fragmentManager: FragmentManager,
    containerId: Int
): NavHostFragment {
    return fragmentManager.findFragmentByTag(tag) as? NavHostFragment
        ?: NavHostFragment.create(graphResId).also { fragment ->
            fragmentManager.commitNow(allowStateLoss = true) {
                add(containerId, fragment, tag)
                detach(fragment)
            }
        }
}

private fun Map<SpaceItem, Lazy<NavHostFragment>>.present(fragmentManager: FragmentManager, item: SpaceItem) {
    fragmentManager.commit(allowStateLoss = true) {
        setCustomAnimations(
            R.anim.nav_default_enter_anim,
            R.anim.nav_default_exit_anim,
            R.anim.nav_default_pop_enter_anim,
            R.anim.nav_default_pop_exit_anim
        )

        filterNot { entry -> entry.key == item }
            .map(Map.Entry<SpaceItem, Lazy<NavHostFragment>>::value)
            .filter(Lazy<NavHostFragment>::isInitialized)
            .map(Lazy<NavHostFragment>::value)
            .forEach { fragment -> detach(fragment) }

        val value = getValue(item).value
        attach(value)
        setPrimaryNavigationFragment(value)

        addToBackStack(item.tag())
        setReorderingAllowed(true)
    }
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    return (0 until backStackEntryCount).any { index -> getBackStackEntryAt(index).name == backStackName }
}

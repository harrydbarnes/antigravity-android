package com.google.antigravity.mobile.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.antigravity.mobile.R
import com.google.antigravity.mobile.ui.theme.AntigravityViolet

@Composable
fun AntigravityNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        tonalElevation = 6.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_terminal),
                    contentDescription = stringResource(R.string.nav_workspace)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_workspace),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AntigravityViolet.copy(alpha = 0.2f),
                selectedIconColor = AntigravityViolet,
                selectedTextColor = AntigravityViolet
            )
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_remote_control),
                    contentDescription = stringResource(R.string.nav_remote_control)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_remote_control),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AntigravityViolet.copy(alpha = 0.2f),
                selectedIconColor = AntigravityViolet,
                selectedTextColor = AntigravityViolet
            )
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cloud_projects),
                    contentDescription = stringResource(R.string.nav_cloud_projects)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_cloud_projects),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AntigravityViolet.copy(alpha = 0.2f),
                selectedIconColor = AntigravityViolet,
                selectedTextColor = AntigravityViolet
            )
        )

        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_artifacts),
                    contentDescription = stringResource(R.string.nav_artifacts)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_artifacts),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AntigravityViolet.copy(alpha = 0.2f),
                selectedIconColor = AntigravityViolet,
                selectedTextColor = AntigravityViolet
            )
        )

        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.nav_settings)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_settings),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AntigravityViolet.copy(alpha = 0.2f),
                selectedIconColor = AntigravityViolet,
                selectedTextColor = AntigravityViolet
            )
        )
    }
}

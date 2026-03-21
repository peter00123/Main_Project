package com.atezhare.ui.home

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.atezhare.R

public class HomeFragmentDirections private constructor() {
  public companion object {
    public fun actionHomeToDirectory(): NavDirections =
        ActionOnlyNavDirections(R.id.action_home_to_directory)

    public fun actionHomeToReceive(): NavDirections =
        ActionOnlyNavDirections(R.id.action_home_to_receive)
  }
}

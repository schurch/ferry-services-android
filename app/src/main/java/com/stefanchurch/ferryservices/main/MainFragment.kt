package com.stefanchurch.ferryservices.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.findNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.stefanchurch.ferryservices.ServicesRepository
import com.stefanchurch.ferryservices.R
import com.stefanchurch.ferryservices.SharedPreferences
import com.stefanchurch.ferryservices.databinding.DetailFragmentBinding
import com.stefanchurch.ferryservices.detail.ServiceDetailArgument
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.Status
import com.stefanchurch.ferryservices.models.status
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<DetailFragmentBinding>(
            inflater,
            R.layout.detail_fragment,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.detailScreen.setContent {
            MainScreen()
        }

//        model.showError = { error ->
//            val builder = AlertDialog.Builder(binding.root.context)
//            builder.setMessage(error)
//                .setPositiveButton(R.string.ok) { dialog, _ ->
//                    dialog.dismiss()
//                }
//            builder.create().show()
//        }

        return binding.root
    }

    @Composable
    private fun MainScreen(
        viewModel: MainViewModel = viewModel(
            viewModelStoreOwner = this,
            key = null,
            factory = MainViewModelFactory(
                getDefaultServices(),
                ServicesRepository.getInstance(requireContext().applicationContext),
                SharedPreferences(requireContext().applicationContext),
                this
            )
        )
    ) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        DisposableEffect(lifecycle) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> viewModel.refresh()
                }
            }
            lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycle.removeObserver(lifecycleObserver)
            }
        }

        SwipeRefresh(
            state = rememberSwipeRefreshState(viewModel.isRefreshing.value),
            onRefresh = { viewModel.refresh() },
        ) {
            LazyColumn {
                items(viewModel.items.value) { row ->
                    when (row) {
                        is ServiceItem.ServiceItemHeader -> Text(
                            text = row.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 10.dp, top = 8.dp)
                        )
                        is ServiceItem.ServiceItemService ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = rememberRipple(bounded = true),
                                        onClick = {
                                            val navController = view?.findNavController()
                                            if (navController?.currentDestination?.id == R.id.mainFragment) {
                                                navController.navigate(
                                                    MainFragmentDirections.actionMainFragmentToServiceDetail(
                                                        ServiceDetailArgument(
                                                            row.service.serviceID,
                                                            row.service
                                                        )
                                                    )
                                                )
                                            }
                                        })
                                    .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 20.dp)
                            ) {
                                val color = when (row.service.status) {
                                    Status.NORMAL -> colorResource(id = R.color.colorStatusNormal)
                                    Status.DISRUPTED -> colorResource(id = R.color.colorStatusDisrupted)
                                    Status.CANCELLED -> colorResource(id = R.color.colorStatusCancelled)
                                    Status.UNKNOWN -> colorResource(id = R.color.colorStatusUnknown)
                                }
                                Canvas(modifier = Modifier.size(25.dp), onDraw = {
                                    drawCircle(color = color)
                                })
                                Spacer(modifier = Modifier.width(15.dp))
                                Column {
                                    Text(
                                        text = row.service.area,
                                        fontSize = 18.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = row.service.route,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                    }
                }
            }
        }
    }

    private fun getDefaultServices(): Array<Service> {
        val json = resources.assets.open("services.json").bufferedReader().use { it.readText() }
        val format = Json { ignoreUnknownKeys = true }
        return format.decodeFromString<Array<Service>>(json)
    }
}

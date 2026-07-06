package kalkulator.cnc.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView
import platform.UIKit.UIColor
import platform.CoreGraphics.CGRectMake

var createIosAdBannerView: (() -> UIView)? = null

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdBanner(modifier: Modifier, isPremium: Boolean) {
    if (isPremium) return

    val factory = createIosAdBannerView
    if (factory != null) {
        Box(
            modifier = modifier.fillMaxWidth().height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            UIKitView(
                modifier = Modifier.size(320.dp, 50.dp),
                factory = {
                    val container = UIView()
                    container.backgroundColor = UIColor.clearColor
                    container.setFrame(CGRectMake(0.0, 0.0, 320.0, 50.0))

                    val banner = factory()
                    banner.backgroundColor = UIColor.clearColor
                    container.addSubview(banner)
                    container
                },
                update = { container ->
                    val banner = container.subviews.firstOrNull() as? UIView
                    banner?.setFrame(container.bounds)
                }
            )
        }
    } else {
        Box(modifier = modifier.fillMaxWidth().height(50.dp))
    }
}
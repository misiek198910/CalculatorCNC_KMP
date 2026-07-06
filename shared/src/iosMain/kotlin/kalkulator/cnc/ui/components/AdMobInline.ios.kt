package kalkulator.cnc.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kalkulator.cnc.AdMobConfigBridge
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView
import platform.UIKit.UIColor
import platform.CoreGraphics.CGRectMake

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdMobInline(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        UIKitView(
            modifier = Modifier.size(300.dp, 250.dp),
            factory = {
                val container = UIView()
                container.backgroundColor = UIColor.clearColor
                container.setFrame(CGRectMake(0.0, 0.0, 300.0, 250.0))

                val banner = (AdMobConfigBridge.createInlineBanner?.invoke() as? UIView)
                if (banner != null) {
                    banner.backgroundColor = UIColor.clearColor
                    container.addSubview(banner)
                }
                container
            },
            update = { container ->
                val banner = container.subviews.firstOrNull() as? UIView
                banner?.setFrame(container.bounds)
            }
        )
    }
}
package kalkulator.cnc.data.params

import androidx.compose.runtime.Composable
import calculatorcnc.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

data class MaterialParam(
    val name: String,
    val isoGroup: String,
    val vcMilling: Double,
    val fzMilling: Double,
    val vcTurning: Double,
    val fnTurning: Double,
    val vcThreading: Double,
    val vcTapping: Double,
    val vcParting: Double,
    val fnParting: Double,
    val vcSawing: Double,
    val kc: Double
)

@Composable
fun getMaterialsList(): List<MaterialParam> {
    return listOf(
        MaterialParam(stringResource(Res.string.carbon_steel), stringResource(Res.string.P), 180.0, 0.12, 160.0, 0.25, 100.0, 15.0, 100.0, 0.08, 70.0, 2100.0),
        MaterialParam(stringResource(Res.string.alloy_steel), stringResource(Res.string.P), 140.0, 0.10, 130.0, 0.20, 80.0, 12.0, 80.0, 0.06, 50.0, 2400.0),
        MaterialParam(stringResource(Res.string.stainless_steel), stringResource(Res.string.M), 80.0, 0.05, 90.0, 0.15, 60.0, 8.0, 50.0, 0.04, 30.0, 2500.0),
        MaterialParam(stringResource(Res.string.cast_iron), stringResource(Res.string.K), 150.0, 0.15, 140.0, 0.25, 90.0, 10.0, 70.0, 0.10, 60.0, 1200.0),
        MaterialParam(stringResource(Res.string.aluminium), stringResource(Res.string.N), 450.0, 0.20, 400.0, 0.30, 180.0, 30.0, 220.0, 0.15, 150.0, 700.0),
        MaterialParam(stringResource(Res.string.brass), stringResource(Res.string.N), 250.0, 0.15, 220.0, 0.20, 120.0, 20.0, 150.0, 0.10, 90.0, 800.0),
        MaterialParam(stringResource(Res.string.titanium), stringResource(Res.string.S), 45.0, 0.04, 50.0, 0.12, 40.0, 5.0, 30.0, 0.03, 15.0, 2800.0),
        MaterialParam(stringResource(Res.string.hardened_steel), stringResource(Res.string.H), 50.0, 0.04, 45.0, 0.10, 30.0, 4.0, 25.0, 0.02, 10.0, 4500.0)
    )
}


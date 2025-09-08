package org.rainrental.rainrentalrfid.app

enum class NavigationRoutes(val route: String, val title: String){
    Home(
        route = "route_home",
        title = "Home"
    ),
    Commission(
        route = "route_association",
        title = "Commission Tags"
    ),
    Hunt(
        route = "route_hunt",
        title = "Hunt Tags",
    ),
    Radar(
        route = "route_radar",
        title = "Radar"
    ),
    ContinuousScanning(
        route = "route_continuous_scanning",
        title = "Continuous Scanning"
    ),
    Inventory(
        route = "route_inventory",
        title = "Inventory"
    ),
    Lookup(
        route = "route_lookup",
        title = "Tag Lookup"
    ),
    Settings(
        route = "route_settings",
        title = "Settings"
    )
}
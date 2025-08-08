package org.rainrental.rainrentalrfid.continuousScanning.data

enum class DeliveryConnectionState {
    DEAD,
    WAITING_FOR_IP,
    INIT,
    CONNECTING,
    CONNECTED,
    ERROR,
} 
package com.stefanchurch.ferryservices.main

import com.stefanchurch.ferryservices.models.Service

sealed class ServiceItem {
    data class ServiceItemHeader(val text: String) : ServiceItem()
    data class ServiceItemService(val service: Service) : ServiceItem()
}
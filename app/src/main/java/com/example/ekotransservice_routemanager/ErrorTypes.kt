package com.example.ekotransservice_routemanager

enum class ErrorTypes (val string: String) {
    ROOM_ERROR("Ошибка работы с локальной базой данных"), DOWNLOAD_ERROR("Ошибка загрузки данных"), NETWORK_ERROR ("Ошибка сети")

}
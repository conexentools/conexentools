package com.conexentools.target_app_helpers.transfermovil

sealed class BankTab(val name: String) {
  data object Sesion : BankTab("Sesión")
  data object Consultas : BankTab("Consultas")
  data object Operaciones : BankTab("Operaciones")
  data object Configuracion : BankTab("Configuración")
}
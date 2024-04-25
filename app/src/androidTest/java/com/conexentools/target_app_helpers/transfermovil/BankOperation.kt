package com.conexentools.target_app_helpers.transfermovil

sealed class BankOperation(val name: String) {
  data object TransferCash : BankOperation("Transferir Efectivo")
  data object RechargeMobile : BankOperation("Recarga Saldo Móvil")
}
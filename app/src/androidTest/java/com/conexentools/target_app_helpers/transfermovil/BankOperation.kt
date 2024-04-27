package com.conexentools.target_app_helpers.transfermovil

sealed class BankOperation(val description: String) {
  data object TransferCash : BankOperation("Transferencia del saldo de la tarjeta")
  data object RechargeMobile : BankOperation("Recargar saldo del móvil")
}
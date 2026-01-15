package com.cielo.applibros.utils


import android.content.Context
import android.widget.Toast
import com.cielo.applibros.domain.model.AppError
import com.google.android.material.snackbar.Snackbar
import android.view.View

object ErrorHandler {

    /**
     * Muestra un error como Toast
     */
    fun showErrorToast(context: Context, error: AppError) {
        Toast.makeText(context, error.userMessage, Toast.LENGTH_LONG).show()
    }

    /**
     * Muestra un error como Snackbar con opción de retry
     */
    fun showErrorSnackbar(
        view: View,
        error: AppError,
        onRetry: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(view, error.userMessage, Snackbar.LENGTH_LONG)

        if (onRetry != null) {
            snackbar.setAction("REINTENTAR") {
                onRetry()
            }
        }

        snackbar.show()
    }

    /**
     * Muestra un diálogo de error (para errores críticos)
     */
    fun showErrorDialog(
        context: Context,
        error: AppError,
        onRetry: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(error.userMessage)
            .setPositiveButton(if (onRetry != null) "Reintentar" else "Entendido") { _, _ ->
                onRetry?.invoke()
            }
            .apply {
                if (onRetry != null) {
                    setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                        onDismiss?.invoke()
                    }
                }
            }
            .setCancelable(false)
            .show()
    }
}
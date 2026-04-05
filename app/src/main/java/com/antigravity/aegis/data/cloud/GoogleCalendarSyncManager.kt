package com.antigravity.aegis.data.cloud

import android.content.Context
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.antigravity.aegis.data.local.entity.ClientEntity
import com.antigravity.aegis.data.local.entity.ExpenseEntity
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.MileageLogEntity
import com.antigravity.aegis.data.local.entity.SessionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona la sincronización de entidades de Aegis Core con Google Calendar.
 * Utiliza GoogleAppsManager para obtener el servicio de Calendar autenticado.
 */
@Singleton
class GoogleCalendarSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleAppsManager: GoogleAppsManager
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ─── Projects ────────────────────────────────────────────────────────────

    /**
     * Inserta o actualiza un proyecto en Google Calendar.
     * @return eventId del evento creado/actualizado en Google Calendar, o null si falla.
     */
    suspend fun syncProject(project: ProjectEntity): String? {
        val account = googleAppsManager.getLastSignedInAccount() ?: return null
        return try {
            val service = googleAppsManager.getCalendarService(account)
            val event = buildProjectEvent(project)
            if (project.googleCalendarEventId != null) {
                service.events().update("primary", project.googleCalendarEventId, event).execute()
                project.googleCalendarEventId
            } else {
                val created = service.events().insert("primary", event).execute()
                created.id
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildProjectEvent(project: ProjectEntity): Event {
        val startMs = project.startDate
        val endMs = project.endDate ?: (startMs + 86_400_000L) // +1 día si no hay fin
        return Event()
            .setSummary("[Aegis] ${project.name}")
            .setDescription(project.description ?: "")
            .setStart(EventDateTime().setDate(DateTime(dateFormat.format(Date(startMs)))))
            .setEnd(EventDateTime().setDate(DateTime(dateFormat.format(Date(endMs)))))
    }

    // ─── Expenses ─────────────────────────────────────────────────────────────

    /**
     * Inserta un gasto como evento de un día en Google Calendar.
     * @return eventId del evento creado, o null si falla.
     */
    suspend fun syncExpense(expense: ExpenseEntity, merchantName: String): String? {
        val account = googleAppsManager.getLastSignedInAccount() ?: return null
        return try {
            val service = googleAppsManager.getCalendarService(account)
            val event = buildExpenseEvent(expense, merchantName)
            if (expense.googleCalendarEventId != null) {
                service.events().update("primary", expense.googleCalendarEventId, event).execute()
                expense.googleCalendarEventId
            } else {
                val created = service.events().insert("primary", event).execute()
                created.id
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildExpenseEvent(expense: ExpenseEntity, merchantName: String): Event {
        val dateStr = dateFormat.format(Date(expense.date))
        return Event()
            .setSummary("[Aegis Gasto] $merchantName — ${expense.totalAmount}€")
            .setDescription("Categoría: ${expense.category}\nBase: ${expense.baseAmount}€  |  IVA: ${expense.taxAmount}€")
            .setStart(EventDateTime().setDate(DateTime(dateStr)))
            .setEnd(EventDateTime().setDate(DateTime(dateStr)))
    }

    // ─── Quotes / CRM ─────────────────────────────────────────────────────────

    /**
     * Inserta un presupuesto/aviso CRM como evento en Google Calendar.
     * @return eventId del evento creado, o null si falla.
     */
    suspend fun syncQuote(quote: QuoteEntity, clientName: String): String? {
        val account = googleAppsManager.getLastSignedInAccount() ?: return null
        return try {
            val service = googleAppsManager.getCalendarService(account)
            val event = buildQuoteEvent(quote, clientName)
            if (quote.googleCalendarEventId != null) {
                service.events().update("primary", quote.googleCalendarEventId, event).execute()
                quote.googleCalendarEventId
            } else {
                val created = service.events().insert("primary", event).execute()
                created.id
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildQuoteEvent(quote: QuoteEntity, clientName: String): Event {
        val dateStr = dateFormat.format(Date(quote.date))
        return Event()
            .setSummary("[Aegis CRM] ${quote.title} — $clientName")
            .setDescription("Estado: ${quote.status}\nTotal: ${quote.calculatedTotal / 100.0}€\n${quote.description}")
            .setStart(EventDateTime().setDate(DateTime(dateStr)))
            .setEnd(EventDateTime().setDate(DateTime(dateStr)))
    }

    // ─── Mileage ─────────────────────────────────────────────────────────────

    /**
     * Inserta un registro de kilometraje como evento en Google Calendar.
     * @return eventId del evento creado, o null si falla.
     */
    suspend fun syncMileage(log: MileageLogEntity): String? {
        val account = googleAppsManager.getLastSignedInAccount() ?: return null
        return try {
            val service = googleAppsManager.getCalendarService(account)
            val event = buildMileageEvent(log)
            if (log.googleCalendarEventId != null) {
                service.events().update("primary", log.googleCalendarEventId, event).execute()
                log.googleCalendarEventId
            } else {
                val created = service.events().insert("primary", event).execute()
                created.id
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildMileageEvent(log: MileageLogEntity): Event {
        val dateStr = dateFormat.format(Date(log.date))
        return Event()
            .setSummary("[Aegis Km] ${log.origin} -> ${log.destination} — ${log.distanceKm}km")
            .setDescription("Vehículo: ${log.vehicle}\nCoste: ${log.calculatedCost}€\nOdometer: ${log.startOdometer} -> ${log.endOdometer}")
            .setStart(EventDateTime().setDate(DateTime(dateStr)))
            .setEnd(EventDateTime().setDate(DateTime(dateStr)))
    }

    // ─── Sessions ─────────────────────────────────────────────────────────────

    /**
     * Sincroniza la próxima cita de una sesión en Google Calendar.
     * @return eventId del evento creado/actualizado, o null si falla.
     */
    suspend fun syncSessionNextAppointment(session: SessionEntity, clientName: String, projectName: String): String? {
        val account = googleAppsManager.getLastSignedInAccount() ?: return null
        val nextDate = session.nextSessionDate ?: return null
        
        return try {
            val service = googleAppsManager.getCalendarService(account)
            val event = buildSessionEvent(session, clientName, projectName)
            if (session.googleCalendarEventId != null) {
                service.events().update("primary", session.googleCalendarEventId, event).execute()
                session.googleCalendarEventId
            } else {
                val created = service.events().insert("primary", event).execute()
                created.id
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildSessionEvent(session: SessionEntity, clientName: String, projectName: String): Event {
        val dateStr = dateFormat.format(Date(session.nextSessionDate!!))
        return Event()
            .setSummary("[Aegis Cita] $clientName — $projectName")
            .setDescription("Próxima sesión programada.\nNotas previas: ${session.notes}\nEjercicios: ${session.exercises}")
            .setLocation(session.location)
            .setStart(EventDateTime().setDate(DateTime(dateStr)))
            .setEnd(EventDateTime().setDate(DateTime(dateStr)))
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    /**
     * Elimina un evento de Google Calendar por su ID.
     */
    suspend fun deleteEvent(eventId: String): Boolean {
        val account = googleAppsManager.getLastSignedInAccount() ?: return false
        return try {
            val service = googleAppsManager.getCalendarService(account)
            service.events().delete("primary", eventId).execute()
            true
        } catch (e: Exception) {
            false
        }
    }
}

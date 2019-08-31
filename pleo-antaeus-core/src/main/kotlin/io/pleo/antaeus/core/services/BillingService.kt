package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import java.time.LocalDate
import mu.KLogging
//import org.jetbrains.exposed.sql.Database
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.io.File

/*
 BillingService will schedule payment of customer invoices on the first of the month. 
 */
	// TODO - check date
	// TODO - get customers list
	// TODO - get invoice details
	// TODO - charge customer account using PaymentProvider
	// TODO - run scheduled thread 
	// TODO - write pass/fail reports
	// TODO - code cleanup
	// TODO - commit

class BillingService(
	private val dal: AntaeusDal,
	private val paymentProvider: PaymentProvider
) {
	
		fun payInvoices(dayInt: Int, genReport: Boolean) {
			try {
				logger.info("Paying invoices...")
				var paidSuccess = mutableListOf<Invoice>()
				var paidFailed= mutableListOf<Invoice>() 
				if(dayInt == 1) {
					var il = dal.fetchInvoices()
					for (i in il) {
						if(i.status == InvoiceStatus.PENDING) {
							logger.debug(i.id.toString())
							logger.debug(i.customerId.toString())
							logger.debug(i.amount.value.toString())
							logger.debug(i.amount.currency.toString())
							logger.debug(i.status.toString())
							var invoiceId = i.id
							var customerId = i.customerId
							var currency = i.amount.currency
							var value = i.amount.value
							var paid = paymentProvider.charge(i);
							if (paid) {
								var msg1 = "Customer $customerId payment of $currency $value for invoice $invoiceId: SUCCESSFUL"
								logger.info(msg1)
								i.status = InvoiceStatus.PAID
								paidSuccess.add(i)
							} else {
								var msg2 = "Customer $customerId payment of $currency $value for invoice $invoiceId: FAILED"
								logger.info(msg2)
								paidFailed.add(i)
							}
						}
					}
				}
				if (genReport)
					generateReports(paidSuccess, paidFailed)
		    } catch (ex: Exception) {
		        println(ex.message) 
			}
		}

		fun generateReports(paidSuccess: List<Invoice>, paidFailed: List<Invoice>) {
			try {
				logger.info("Generating Reports...")
				val paidSuccessFile = "paidSuccessReport.txt"
				val paidFailedFile = "paidFailedReport.txt"
				val paidSuccessString = paidSuccess.toString()
				val paidFailedString = paidFailed.toString()
				logger.debug("paidSuccessString: $paidSuccessString")
				var record = ""
				for (p in paidSuccess) {
					record = record.plus(p.toString().plus("\n"))
				}
				logger.info(record)
				File(paidSuccessFile).printWriter().use { out -> out.println(record) }
				logger.info("Written $paidSuccessFile")
				logger.debug("paidFailedString: $paidFailedString")
				record = ""
				for (p in paidFailed) {
					record = record.plus(p.toString().plus("\n"))
				}
				logger.info(record)
				File(paidFailedFile).printWriter().use { out -> out.println(record) }
				logger.info("Written $paidFailedFile")
		    } catch (ex: Exception) {
		        println(ex.message) 
			}
		}
	
	companion object: KLogging()
	init {
		logger.info("BillingService initialized...")
	}
	var today = LocalDate.parse("2018-12-01")
	//var today = LocalDate.now()
	var logDate = logger.info("Today is: $today")
	val dayInt = today.getDayOfMonth()
	var logDayOfMonth = logger.info("DayOfMonth: $dayInt")
	val delay = 86400L
	val genReport = true
	val payInvoicesTask = object : Runnable {
		override fun run() {
	        val payInvoice = payInvoices(dayInt, genReport)
		}
	}
	val singleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor()
    val payInvoiceExecutor = singleThreadScheduledExecutor.scheduleWithFixedDelay(payInvoicesTask, 1L, delay, TimeUnit.SECONDS)
}
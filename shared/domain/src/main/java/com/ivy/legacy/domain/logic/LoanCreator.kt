package com.ivy.legacy.domain.logic
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.model.legacy.Loan
import com.ivy.base.coroutines.ioThread
import com.ivy.legacy.domain.model.CreateLoanData
import com.ivy.legacy.domain.mapper.toEntity
import com.ivy.legacy.domain.pure.util.nextOrderNum
import java.util.UUID
import javax.inject.Inject

class LoanCreator @Inject constructor(
    private val dao: LoanDao,
    private val loanWriter: WriteLoanDao,
) {
    suspend fun create(
        data: CreateLoanData,
        onRefreshUI: suspend (Loan) -> Unit
    ): UUID? {
        val name = data.name
        if (name.isBlank()) return null
        if (data.amount <= 0) return null

        var loanId: UUID? = null

        try {
            val newItem = ioThread {
                val item = Loan(
                    name = name.trim(),
                    amount = data.amount,
                    type = data.type,
                    color = data.color,
                    icon = data.icon,
                    note = data.note,
                    orderNum = dao.findMaxOrderNum().nextOrderNum(),
                    accountId = data.account?.id,
                    dateTime = data.dateTime
                )
                loanId = item.id
                loanWriter.save(item.toEntity())
                item
            }

            onRefreshUI(newItem)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return loanId
    }

    suspend fun edit(
        updatedItem: Loan,
        onRefreshUI: suspend (Loan) -> Unit
    ) {
        if (updatedItem.name.isBlank()) return
        if (updatedItem.amount <= 0.0) return

        try {
            ioThread {
                loanWriter.save(updatedItem.toEntity())
            }

            onRefreshUI(updatedItem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun delete(
        item: Loan,
        onRefreshUI: suspend () -> Unit
    ) {
        try {
            ioThread {
                loanWriter.deleteById(item.id)
            }

            onRefreshUI()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

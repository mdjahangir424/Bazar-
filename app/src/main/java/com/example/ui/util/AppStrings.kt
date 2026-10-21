package com.example.ui.util

enum class AppLanguage {
    BANGLA,
    ENGLISH
}

object AppStrings {
    fun appTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "হিসাব খাতা – দৈনিক হিসাব"
        AppLanguage.ENGLISH -> "Hisab Khata – Daily Hisab"
    }

    fun dashboard(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "ড্যাশবোর্ড"
        AppLanguage.ENGLISH -> "Dashboard"
    }

    fun marketExpense(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "বাজার খরচ"
        AppLanguage.ENGLISH -> "Market Expenses"
    }

    fun outstanding(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "বাকি হিসাব"
        AppLanguage.ENGLISH -> "Outstanding"
    }

    fun reports(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "রিপোর্ট ও প্রিন্ট"
        AppLanguage.ENGLISH -> "Reports & Print"
    }

    fun settings(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "সেটিংস"
        AppLanguage.ENGLISH -> "Settings"
    }

    // Dashboard Cards
    fun totalOutstandingBalance(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "মোট বাকি ব্যালেন্স"
        AppLanguage.ENGLISH -> "Total Outstanding Balance"
    }

    fun totalMarketExpenses(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "মোট বাজার খরচ"
        AppLanguage.ENGLISH -> "Total Market Expenses"
    }

    fun totalOutstandingAdded(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "মোট বাকি এন্ট্রি"
        AppLanguage.ENGLISH -> "Total Outstanding Incurred"
    }

    fun totalPaymentsMade(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "মোট পরিশোধিত টাকা"
        AppLanguage.ENGLISH -> "Total Payments Made"
    }

    fun todaysSummary(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আজকের হিসাব সারসংক্ষেপ"
        AppLanguage.ENGLISH -> "Today's Summary"
    }

    fun todaysExpense(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আজকের বাজার"
        AppLanguage.ENGLISH -> "Today's Market"
    }

    fun thisMonthExpense(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "চলতি মাসের বাজার"
        AppLanguage.ENGLISH -> "This Month's Market"
    }

    fun todaysOutstanding(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আজকের নতুন বাকি"
        AppLanguage.ENGLISH -> "Today's New Outstanding"
    }

    fun todaysPayment(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আজকের পরিশোধ"
        AppLanguage.ENGLISH -> "Today's Payment"
    }

    fun todaysEntriesCount(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আজকের মোট এন্ট্রি"
        AppLanguage.ENGLISH -> "Today's Entries"
    }

    // Quick Actions
    fun addMarketExpenseBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আজকের বাজার খরচ যোগ করুন"
        AppLanguage.ENGLISH -> "Add Today's Market Expense"
    }

    fun addOutstandingBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আজকের বাকি যোগ করুন"
        AppLanguage.ENGLISH -> "Add Today's Outstanding"
    }

    fun addPaymentBtn(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "বাকি পরিশোধ যোগ করুন"
        AppLanguage.ENGLISH -> "Record Payment / Repayment"
    }

    // Form fields
    fun amount(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "টাকার পরিমাণ (টাকা)"
        AppLanguage.ENGLISH -> "Amount (BDT)"
    }

    fun date(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "তারিখ"
        AppLanguage.ENGLISH -> "Date"
    }

    fun note(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "বিবরণ (ঐচ্ছিক)"
        AppLanguage.ENGLISH -> "Note (Optional)"
    }

    fun save(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "সংরক্ষণ করুন"
        AppLanguage.ENGLISH -> "Save Record"
    }

    fun update(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আপডেট করুন"
        AppLanguage.ENGLISH -> "Update Record"
    }

    fun cancel(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "বাতিল"
        AppLanguage.ENGLISH -> "Cancel"
    }

    fun delete(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "মুছে ফেলুন"
        AppLanguage.ENGLISH -> "Delete"
    }

    fun edit(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "এডিট"
        AppLanguage.ENGLISH -> "Edit"
    }

    fun serial(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "ক্রমিক নং"
        AppLanguage.ENGLISH -> "Sl. No."
    }

    fun actions(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "অ্যাকশন"
        AppLanguage.ENGLISH -> "Actions"
    }

    // Filters
    fun filterAll(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "সব সময়"
        AppLanguage.ENGLISH -> "All Time"
    }

    fun filterToday(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আজ"
        AppLanguage.ENGLISH -> "Today"
    }

    fun filterThisWeek(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "এই সপ্তাহ"
        AppLanguage.ENGLISH -> "This Week"
    }

    fun filterThisMonth(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "এই মাস"
        AppLanguage.ENGLISH -> "This Month"
    }

    fun filterCustom(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "নির্দিষ্ট সময়সীমা"
        AppLanguage.ENGLISH -> "Custom Range"
    }

    fun searchPlaceholder(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "বিবরণ বা টাকা দিয়ে খুঁজুন..."
        AppLanguage.ENGLISH -> "Search by note or amount..."
    }

    fun grandTotal(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "সর্বমোট"
        AppLanguage.ENGLISH -> "Grand Total"
    }

    fun filteredTotal(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "নির্বাচিত সময়ের মোট"
        AppLanguage.ENGLISH -> "Filtered Total"
    }

    fun noRecordsFound(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "কোনো রেকর্ড পাওয়া যায়নি"
        AppLanguage.ENGLISH -> "No records found"
    }

    fun confirmDeleteTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "রেকর্ড মুছে ফেলার নিশ্চিতকরণ"
        AppLanguage.ENGLISH -> "Confirm Delete"
    }

    fun confirmDeleteMsg(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "আপনি কি নিশ্চিত যে এই রেকর্ডটি মুছে ফেলতে চান? এটি স্থায়ীভাবে মুছে যাবে।"
        AppLanguage.ENGLISH -> "Are you sure you want to delete this record? This action cannot be undone."
    }

    fun paymentExceedsBalance(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "পরিশোধের পরিমাণ বর্তমান বাকি ব্যালেন্সের চেয়ে বেশি হতে পারে না!"
        AppLanguage.ENGLISH -> "Payment amount cannot exceed current outstanding balance!"
    }

    fun expenseSavedSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "বাজার খরচ সফলভাবে সংরক্ষিত হয়েছে"
        AppLanguage.ENGLISH -> "Market expense saved successfully"
    }

    fun outstandingSavedSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "বাকি এন্ট্রি সফলভাবে সংরক্ষিত হয়েছে"
        AppLanguage.ENGLISH -> "Outstanding entry saved successfully"
    }

    fun paymentSavedSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.BANGLA -> "পরিশোধ সফলভাবে সংরক্ষিত হয়েছে"
        AppLanguage.ENGLISH -> "Payment recorded successfully"
    }
}

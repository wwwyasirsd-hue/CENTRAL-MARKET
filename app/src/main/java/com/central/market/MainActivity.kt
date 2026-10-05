package com.central.market

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class MainActivity : Activity() {

    // =========================================================
    // CENTRAL MARKET — VISUAL FOUNDATION
    // =========================================================

    private val navy = Color.rgb(12, 30, 48)
    private val navy2 = Color.rgb(18, 42, 66)
    private val blue = Color.rgb(32, 104, 170)
    private val gold = Color.rgb(205, 157, 45)
    private val green = Color.rgb(46, 150, 91)
    private val red = Color.rgb(190, 65, 65)
    private val white = Color.WHITE
    private val light = Color.rgb(235, 241, 246)
    private val muted = Color.rgb(165, 180, 193)

    private lateinit var content: LinearLayout

    // =========================================================
    // ROLES
    // =========================================================

    enum class UserRole {
        USER,
        ADMIN,
        PARTNER,
        OWNER
    }

    private var currentRole = UserRole.USER
    private var currentAccountName = "زائر"

    private var simulationLoggedIn = false

    // =========================================================
    // SESSION SECURITY
    // =========================================================

    private val handler = Handler(Looper.getMainLooper())

    private val outsideAppTimeout = 10 * 60 * 1000L
    private val sensitiveTimeout = 3 * 60 * 1000L

    private var sessionLocked = false
    private var sensitiveLocked = false

    private var lastBackgroundTime = 0L

    private val sessionLockRunnable = Runnable {
        sessionLocked = true
        showSessionLock()
    }

    private val sensitiveLockRunnable = Runnable {
        sensitiveLocked = true
        showSensitiveLock()
    }

    // =========================================================
    // ACTIVITY
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // منع تصوير وتسجيل الشاشة داخل التطبيق.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        showHome()
    }

    override fun onPause() {
        super.onPause()

        lastBackgroundTime = SystemClock.elapsedRealtime()

        handler.removeCallbacks(sessionLockRunnable)

        handler.postDelayed(
            sessionLockRunnable,
            outsideAppTimeout
        )
    }

    override fun onResume() {
        super.onResume()

        handler.removeCallbacks(sessionLockRunnable)

        if (lastBackgroundTime > 0L) {
            val elapsed =
                SystemClock.elapsedRealtime() - lastBackgroundTime

            if (elapsed >= outsideAppTimeout) {
                sessionLocked = true
                showSessionLock()
            }
        }
    }

    // =========================================================
    // MAIN CONTAINER
    // =========================================================

    private fun baseLayout(title: String) {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(navy)

        val header = LinearLayout(this)
        header.orientation = LinearLayout.VERTICAL
        header.gravity = Gravity.CENTER
        header.setPadding(22, 24, 22, 18)
        header.background = gradientBackground(
            navy2,
            blue
        )

        val brand = TextView(this)

        brand.text = "CENTRAL"
        brand.textSize = 25f
        brand.setTextColor(white)
        brand.gravity = Gravity.CENTER
        brand.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        header.addView(brand)

        val market = TextView(this)

        market.text = "MARKET"
        market.textSize = 13f
        market.setTextColor(gold)
        market.gravity = Gravity.CENTER
        market.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        header.addView(market)

        val titleView = TextView(this)

        titleView.text = title
        titleView.textSize = 17f
        titleView.setTextColor(white)
        titleView.gravity = Gravity.CENTER
        titleView.setPadding(0, 10, 0, 0)

        header.addView(titleView)

        root.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val scroll = ScrollView(this)

        scroll.setBackgroundColor(navy)

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(18, 18, 18, 24)

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)
    }

    // =========================================================
    // SECTIONS
    // =========================================================

    private fun addSection(title: String) {

        val text = TextView(this)

        text.text = title
        text.textSize = 18f
        text.setTextColor(gold)
        text.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        text.setPadding(4, 14, 4, 10)

        content.addView(
            text,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    // =========================================================
    // INFORMATION CARD
    // =========================================================

    private fun addInfo(
        title: String,
        body: String
    ) {

        val card = LinearLayout(this)

        card.orientation = LinearLayout.VERTICAL
        card.setPadding(16, 14, 16, 14)
        card.background = cardBackground()

        val heading = TextView(this)

        heading.text = title
        heading.textSize = 15f
        heading.setTextColor(gold)
        heading.typeface =
            Typeface.DEFAULT_BOLD

        card.addView(heading)

        val description = TextView(this)

        description.text = body
        description.textSize = 14f
        description.setTextColor(light)
        description.setPadding(0, 7, 0, 0)

        card.addView(description)

        val params =
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(0, 0, 0, 12)

        content.addView(card, params)
    }

    // =========================================================
    // VISUAL CARD
    // =========================================================

    private fun addCard(
        symbol: String,
        title: String,
        description: String,
        action: () -> Unit
    ) {

        val card = LinearLayout(this)

        card.orientation = LinearLayout.HORIZONTAL
        card.gravity = Gravity.CENTER_VERTICAL
        card.setPadding(14, 14, 14, 14)
        card.background = cardBackground()

        val icon = TextView(this)

        icon.text = symbol
        icon.textSize = 25f
        icon.setTextColor(gold)
        icon.gravity = Gravity.CENTER

        card.addView(
            icon,
            LinearLayout.LayoutParams(
                48,
                48
            )
        )

        val texts = LinearLayout(this)

        texts.orientation = LinearLayout.VERTICAL
        texts.setPadding(12, 0, 4, 0)

        val heading = TextView(this)

        heading.text = title
        heading.textSize = 16f
        heading.setTextColor(white)
        heading.typeface =
            Typeface.DEFAULT_BOLD

        texts.addView(heading)

        val desc = TextView(this)

        desc.text = description
        desc.textSize = 13f
        desc.setTextColor(muted)
        desc.setPadding(0, 4, 0, 0)

        texts.addView(desc)

        card.addView(
            texts,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        card.setOnClickListener {
            action()
        }

        val params =
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(0, 0, 0, 12)

        content.addView(card, params)
    }

    // =========================================================
    // STATUS
    // =========================================================

    private fun addStatus(
        title: String,
        value: String,
        valueColor: Int
    ) {

        val row = LinearLayout(this)

        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(14, 12, 14, 12)
        row.background = roundedBackground(navy2)

        val name = TextView(this)

        name.text = title
        name.textSize = 14f
        name.setTextColor(light)

        row.addView(
            name,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val state = TextView(this)

        state.text = value
        state.textSize = 13f
        state.setTextColor(valueColor)
        state.typeface =
            Typeface.DEFAULT_BOLD

        row.addView(state)

        val params =
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(0, 0, 0, 8)

        content.addView(row, params)
    }

    // =========================================================
    // NAVIGATION BUTTON
    // =========================================================

    private fun addNavButton(
        title: String,
        action: () -> Unit
    ) {

        val button = Button(this)

        button.text = title
        button.textSize = 14f
        button.setTextColor(white)
        button.background = roundedBackground(blue)

        button.setOnClickListener {
            action()
        }

        val params =
            LinearLayout.LayoutParams(
                -1,
                52
            )

        params.setMargins(0, 10, 0, 8)

        content.addView(button, params)
    }

    // =========================================================
    // BACKGROUND HELPERS
    // =========================================================

    private fun roundedBackground(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 18f
        }
    }

    private fun cardBackground(): GradientDrawable {

        return GradientDrawable().apply {
            setColor(Color.rgb(21, 47, 70))
            cornerRadius = 20f
            setStroke(1, Color.rgb(45, 82, 108))
        }
    }

    private fun gradientBackground(
        start: Int,
        end: Int
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(start, end)
        ).apply {
            cornerRadius = 0f
        }
    }

    // =========================================================
    // ROLE NAMES
    // =========================================================

    private fun roleTitle(
        role: UserRole
    ): String {

        return when (role) {

            UserRole.USER ->
                "المستخدم"

            UserRole.ADMIN ->
                "الإداري"

            UserRole.PARTNER ->
                "الشريك"

            UserRole.OWNER ->
                "المالك"
        }
    }

    private fun roleDescription(
        role: UserRole
    ): String {

        return when (role) {

            UserRole.USER ->
                "الوصول إلى الخدمات والأسواق العامة."

            UserRole.ADMIN ->
                "صلاحيات إدارية محددة دون صلاحيات المالك."

            UserRole.PARTNER ->
                "وصول مخصص لمكتب الشركاء حسب الصلاحية."

            UserRole.OWNER ->
                "صلاحيات الإدارة العليا والمراجعة والاعتماد."
        }
    }

    // =========================================================
    // NETWORK
    // =========================================================

    private fun isOnline(): Boolean {

        val manager =
            getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            manager.activeNetwork
                ?: return false

        val capabilities =
            manager.getNetworkCapabilities(network)
                ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
    }

        // =========================================================
    // HOME
    // =========================================================

    private fun showHome() {

        baseLayout("منصة واحدة .. عالم من الفرص.")

        addSection("CENTRAL MARKET")

        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )

        addInfo(
            "الهوية",
            "منصة متعددة المجالات، تبدأ من السوق والخدمات وتمتد إلى حلول ذكية وإدارية."
        )

        addCard(
            "◉",
            "الأسواق",
            "السيارات، الإلكترونيات، المطاعم، الزراعة وغيرها",
            { showProducts() }
        )

        addCard(
            "◇",
            "الخدمات",
            "الخدمات العامة والمجالات المتخصصة",
            { showServices() }
        )

        addCard(
            "◎",
            "الذكاء البشري",
            "المعرفة والتواصل والخدمات البشرية",
            { showHumanIntelligence() }
        )

        addCard(
            "△",
            "CTM AI",
            "مساعد معلوماتي رسمي داخل المنصة",
            { showCtmAi() }
        )

        addCard(
            "◆",
            "BADGER",
            "منتج مالي مستقل بتصميم وحماية خاصة",
            { showBadger() }
        )

        addCard(
            "▣",
            "الأمان والخصوصية",
            "حماية الحسابات والبيانات والخدمات الحساسة",
            { showSecurity() }
        )

        addSection("الوصول")

        addCard(
            "○",
            "تسجيل الدخول",
            "الحسابات والصلاحيات والمكاتب الخاصة",
            { showSimulationLogin() }
        )

        addCard(
            "◇",
            "وضع الزائر",
            "استكشاف الخدمات العامة دون صلاحيات خاصة",
            {
                currentRole = UserRole.USER
                currentAccountName = "زائر"
                simulationLoggedIn = false
                showGuestMode()
            }
        )

        addNavButton(
            "⌂ فحص الاتصال"
        ) {
            showConnectivity()
        }
    }

    // =========================================================
    // SIMULATION LOGIN
    // =========================================================

    private fun showSimulationLogin() {

        baseLayout("تسجيل الدخول")

        addSection("الحسابات")

        addInfo(
            "نظام الصلاحيات",
            "هذه واجهة محاكاة لمسارات الحسابات. الحسابات الحقيقية يجب أن تستخدم مصادقة آمنة خارج التطبيق."
        )

        addCard(
            "○",
            "مستخدم",
            "الدخول إلى الخدمات العامة",
            {
                loginSimulation(
                    UserRole.USER,
                    "مستخدم المحاكاة"
                )
            }
        )

        addCard(
            "▣",
            "إداري",
            "مكتب الإدارة والصلاحيات التشغيلية",
            {
                showPasswordGate(
                    UserRole.ADMIN,
                    "إداري المحاكاة"
                )
            }
        )

        addCard(
            "◇",
            "شريك",
            "مكتب الشركاء والصلاحيات المخصصة",
            {
                showPasswordGate(
                    UserRole.PARTNER,
                    "شريك المحاكاة"
                )
            }
        )

        addCard(
            "◆",
            "مالك",
            "الإدارة العليا والاعتماد",
            {
                showPasswordGate(
                    UserRole.OWNER,
                    "مالك المحاكاة"
                )
            }
        )

        addInfo(
            "الحماية",
            "إغلاق الجلسة أو انتهاء مدة الجلسة يعيد طلب المصادقة."
        )

        addNavButton(
            "↩ العودة"
        ) {
            showHome()
        }
    }

    // =========================================================
    // PASSWORD GATE
    // =========================================================

    private fun showPasswordGate(
        role: UserRole,
        accountName: String
    ) {

        baseLayout("التحقق من الهوية")

        addSection(roleTitle(role))

        addInfo(
            "الحساب",
            accountName
        )

        val password =
            android.widget.EditText(this)

        password.hint = "كلمة المرور المحاكية"
        password.setTextColor(white)
        password.setHintTextColor(muted)
        password.textSize = 15f
        password.isSingleLine = true

        password.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        password.background =
            roundedBackground(navy2)

        password.setPadding(
            18,
            12,
            18,
            12
        )

        content.addView(
            password,
            LinearLayout.LayoutParams(
                -1,
                58
            ).apply {
                setMargins(0, 0, 0, 14)
            }
        )

        addCard(
            "🔐",
            "دخول",
            "التحقق من كلمة المرور المحاكية",
            {

                val entered =
                    password.text.toString()

                if (
                    verifySimulationPassword(
                        role,
                        entered
                    )
                ) {

                    loginSimulation(
                        role,
                        accountName
                    )

                } else {

                    Toast.makeText(
                        this,
                        "كلمة المرور غير صحيحة.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        addInfo(
            "بيانات المحاكاة",
            "ADMIN-DEMO / PARTNER-DEMO / OWNER-DEMO"
        )

        addNavButton(
            "↩ اختيار حساب آخر"
        ) {
            showSimulationLogin()
        }
    }

    // =========================================================
    // PASSWORD VERIFICATION
    // =========================================================

    private fun verifySimulationPassword(
        role: UserRole,
        password: String
    ): Boolean {

        return when (role) {

            UserRole.USER ->
                password == "USER-DEMO"

            UserRole.ADMIN ->
                password == "ADMIN-DEMO"

            UserRole.PARTNER ->
                password == "PARTNER-DEMO"

            UserRole.OWNER ->
                password == "OWNER-DEMO"
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private fun loginSimulation(
        role: UserRole,
        accountName: String
    ) {

        currentRole = role
        currentAccountName = accountName
        simulationLoggedIn = true

        sessionLocked = false
        sensitiveLocked = false

        handler.removeCallbacks(
            sessionLockRunnable
        )

        handler.removeCallbacks(
            sensitiveLockRunnable
        )

        Toast.makeText(
            this,
            "تم فتح حساب ${roleTitle(role)}",
            Toast.LENGTH_SHORT
        ).show()

        showAccountHome()
    }

    // =========================================================
    // ACCOUNT HOME
    // =========================================================

    private fun showAccountHome() {

        baseLayout(
            "حساب ${roleTitle(currentRole)}"
        )

        addSection("الحساب الحالي")

        addStatus(
            "الاسم",
            currentAccountName,
            green
        )

        addStatus(
            "الدور",
            roleTitle(currentRole),
            gold
        )

        addInfo(
            "الصلاحيات",
            roleDescription(currentRole)
        )

        when (currentRole) {

            UserRole.USER ->
                showUserAccount()

            UserRole.ADMIN ->
                showAdminAccount()

            UserRole.PARTNER ->
                showPartnerAccount()

            UserRole.OWNER ->
                showOwnerAccount()
        }

        addSection("الجلسة")

        addCard(
            "🔒",
            "قفل الحساب",
            "إغلاق الصلاحيات وطلب كلمة المرور عند العودة",
            {
                lockAccount()
            }
        )

        addCard(
            "↪",
            "تسجيل الخروج",
            "إنهاء الحساب المحاكي",
            {
                logoutSimulation()
            }
        )

        addNavButton(
            "⌂ الرئيسية"
        ) {
            showHome()
        }
    }

    // =========================================================
    // USER ACCOUNT
    // =========================================================

    private fun showUserAccount() {

        addCard(
            "◉",
            "الأسواق",
            "استعراض المجالات التجارية",
            { showProducts() }
        )

        addCard(
            "◇",
            "الخدمات",
            "الخدمات العامة",
            { showServices() }
        )

        addCard(
            "◎",
            "النقاط",
            "نظام النقاط والمزايا",
            { showPoints() }
        )
    }

    // =========================================================
    // ADMIN ACCOUNT
    // =========================================================

    private fun showAdminAccount() {

        addCard(
            "▣",
            "مكتب الإدارة",
            "الأدوات التشغيلية المسموحة",
            { showManagerTools() }
        )

        addCard(
            "◇",
            "الأمن",
            "حالة الحماية والسياسات",
            { showSecurity() }
        )

        addCard(
            "△",
            "التشخيص",
            "فحص التطبيق والاتصال",
            { showDiagnostics() }
        )
    }

    // =========================================================
    // PARTNER ACCOUNT
    // =========================================================

    private fun showPartnerAccount() {

        addCard(
            "◇",
            "مكتب الشركاء",
            "المعلومات والصلاحيات المخصصة",
            { showPartnerOffice() }
        )

        addCard(
            "◎",
            "حالة النظام",
            "ملخص التشغيل والتكاملات",
            { showSystemSummary() }
        )
    }

    // =========================================================
    // OWNER ACCOUNT
    // =========================================================

    private fun showOwnerAccount() {

        addCard(
            "◆",
            "مكتب المالك",
            "الإدارة العليا",
            { showOwnerOffice() }
        )

        addCard(
            "◇",
            "إدارة الشركاء",
            "مراجعة الصلاحيات",
            { showPartnerManagement() }
        )

        addCard(
            "▣",
            "الاعتماد",
            "مراجعة التغييرات الحساسة",
            { showOwnerApproval() }
        )
    }

        // =========================================================
    // PRODUCTS / MARKETS
    // =========================================================

    private fun showProducts() {

        baseLayout("الأسواق")

        addSection("مجالات CENTRAL MARKET")

        addCard(
            "◌",
            "السيارات والشاحنات",
            "سوق المركبات والمعدات والنقل",
            { showCategory("السيارات والشاحنات") }
        )

        addCard(
            "⌁",
            "الهواتف والإلكترونيات",
            "الأجهزة والاتصالات والتقنية",
            { showCategory("الهواتف والإلكترونيات") }
        )

        addCard(
            "◇",
            "المطاعم والتوصيل",
            "المطاعم والطلبات والخدمات المرتبطة بها",
            { showCategory("المطاعم والتوصيل") }
        )

        addCard(
            "⌂",
            "الزراعة والثروة الحيوانية",
            "المنتجات الزراعية والحيوانية",
            { showCategory("الزراعة والثروة الحيوانية") }
        )

        addCard(
            "≈",
            "الأسماك",
            "الأسماك والمنتجات البحرية",
            { showCategory("الأسماك") }
        )

        addCard(
            "▱",
            "البناء والجملة",
            "مواد البناء والتجارة بالجملة",
            { showCategory("البناء والجملة") }
        )

        addCard(
            "+",
            "الصحة",
            "خدمات ومجالات صحية عامة",
            { showCategory("الصحة") }
        )

        addCard(
            "△",
            "الرياضة",
            "الرياضة والمستلزمات والخدمات",
            { showCategory("الرياضة") }
        )

        addCard(
            "⌁",
            "الكهرباء والمياه",
            "خدمات ومستلزمات البنية الأساسية",
            { showCategory("الكهرباء والمياه") }
        )

        addCard(
            "□",
            "التعليم",
            "التعليم والتدريب والمعرفة",
            { showCategory("التعليم") }
        )

        addCard(
            "◇",
            "السفر",
            "السفر والتنقل والخدمات المرتبطة",
            { showCategory("السفر") }
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    private fun showCategory(name: String) {

        baseLayout(name)

        addSection("القسم")

        addStatus(
            "الحالة",
            "متاح للاستكشاف",
            green
        )

        addInfo(
            "السوق",
            "يمكن أن يحتوي هذا القسم على عروض ومنتجات وخدمات وفق البلد والأنظمة المطبقة."
        )

        addCard(
            "⌕",
            "البحث داخل القسم",
            "العثور على المنتجات والخدمات",
            { showSearch() }
        )

        addCard(
            "☆",
            "المفضلة",
            "حفظ العناصر المهمة للمستخدم",
            { showFavorites() }
        )

        addInfo(
            "التجارة المحلية والدولية",
            "تختلف الشحنات والرسوم والجمارك والتذاكر والتنبيهات النظامية حسب المنطقة والبلد."
        )

        addNavButton("↩ الأسواق") {
            showProducts()
        }
    }

    // =========================================================
    // SERVICES
    // =========================================================

    private fun showServices() {

        baseLayout("الخدمات")

        addSection("الخدمات العامة")

        addCard(
            "◇",
            "الخدمات العامة",
            "الخدمات اليومية والمهنية",
            { showCategory("الخدمات العامة") }
        )

        addCard(
            "◎",
            "المطبخ",
            "مجال الطعام والمطبخ والخدمات المرتبطة",
            { showKitchen() }
        )

        addCard(
            "♥",
            "الصدقة والمساعدة",
            "دعم المحتاجين والأيتام وفق الضوابط القانونية",
            { showCharity() }
        )

        addCard(
            "◎",
            "الذكاء البشري",
            "المعرفة والخبرة والتواصل",
            { showHumanIntelligence() }
        )

        addCard(
            "△",
            "CTM AI",
            "المساعد المعلوماتي الرسمي",
            { showCtmAi() }
        )

        addCard(
            "◆",
            "BADGER",
            "منتج مستقل للخدمات المالية المصرح بها",
            { showBadger() }
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================================================
    // HUMAN INTELLIGENCE
    // =========================================================

    private fun showHumanIntelligence() {

        baseLayout("الذكاء البشري")

        addSection("المعرفة البشرية")

        addInfo(
            "الفكرة",
            "مساحة للاستفادة من الخبرات والمعارف البشرية داخل المنصة."
        )

        addCard(
            "◎",
            "المعرفة",
            "تنظيم المعلومات والخبرات",
            { showCategory("المعرفة والخبرات") }
        )

        addCard(
            "◇",
            "الزكاة البشرية",
            "مفهوم للمساعدة والخدمة المجتمعية ضمن الضوابط",
            { showCategory("الزكاة البشرية") }
        )

        addInfo(
            "الخصوصية",
            "المعلومات الخاصة لا تُعرض إلا وفق الصلاحيات المسموح بها."
        )

        addNavButton("↩ الخدمات") {
            showServices()
        }
    }

    // =========================================================
    // CTM AI
    // =========================================================

    private fun showCtmAi() {

        baseLayout("CTM AI")

        addSection("المساعد الذكي")

        addStatus(
            "الحالة",
            "معلوماتي",
            green
        )

        addInfo(
            "الدور",
            "مساعد رسمي للمعلومات المسموح بها داخل التطبيق."
        )

        addCard(
            "△",
            "البحث المعلوماتي",
            "الوصول إلى المعلومات المتاحة داخل المنصة",
            {
                Toast.makeText(
                    this,
                    "CTM AI: وضع المعلومات فقط.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        addCard(
            "◇",
            "القراءة الصوتية",
            "قراءة المعلومات المسموح بها عند دعم الجهاز",
            {
                Toast.makeText(
                    this,
                    "ميزة القراءة الصوتية تُفعّل حسب دعم الجهاز.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        addInfo(
            "الحدود",
            "لا ينفذ معاملات مالية ولا يمنح صلاحيات ولا يتجاوز قواعد الأمان."
        )

        addNavButton("↩ الخدمات") {
            showServices()
        }
    }

    // =========================================================
    // BADGER
    // =========================================================

    private fun showBadger() {

        baseLayout("BADGER")

        addSection("BADGER")

        addStatus(
            "النوع",
            "منتج مستقل",
            gold
        )

        addStatus(
            "القفل",
            "3 دقائق للخدمات الحساسة",
            green
        )

        addInfo(
            "الهوية البصرية",
            "علامة هندسية مختصرة مستوحاة من فكرة BADGER، وليست رسمة كاملة للحيوان."
        )

        addCard(
            "◆",
            "لوحة BADGER",
            "المؤشرات والبيانات المحاكية",
            { showBadgerDashboard() }
        )

        addCard(
            "◇",
            "الإيداع والاستثمار",
            "معلومات وتحليلات محاكاة فقط",
            { showSensitiveInvestment() }
        )

        addCard(
            "◎",
            "الشركاء والبنوك",
            "معلومات المنتج والتكاملات المستقبلية",
            { showCategory("شركاء BADGER") }
        )

        addInfo(
            "قاعدة مالية",
            "لا يوجد تنفيذ مالي حقيقي من هذه النسخة. أي تنفيذ مستقبلي يحتاج بنية آمنة واعتمادًا رسميًا."
        )

        addNavButton("↩ الخدمات") {
            showServices()
        }
    }

    // =========================================================
    // BADGER DASHBOARD
    // =========================================================

    private fun showBadgerDashboard() {

        baseLayout("BADGER • لوحة المعلومات")

        addSection("المؤشرات")

        addStatus(
            "الحساب",
            "محاكاة",
            gold
        )

        addStatus(
            "التحليلات",
            "متاحة للعرض",
            green
        )

        addStatus(
            "التنفيذ المالي",
            "غير متاح",
            red
        )

        addInfo(
            "الإيصالات",
            "هوية الإيصالات المقترحة تستخدم لغة بصرية زرقاء وذهبية وسوداء عند تطوير الواجهة الكاملة."
        )

        addInfo(
            "العمولات",
            "أي عمولة مستقبلية يجب أن تكون موثقة تعاقديًا وواضحة للأطراف."
        )

        addNavButton("↩ BADGER") {
            showBadger()
        }
    }

    // =========================================================
    // SENSITIVE INVESTMENT
    // =========================================================

    private fun showSensitiveInvestment() {

        baseLayout("الخدمة الحساسة")

        if (!simulationLoggedIn) {
            addInfo(
                "الوصول",
                "هذه الخدمة تتطلب حسابًا مصادقًا عليه."
            )

            addNavButton("↩ تسجيل الدخول") {
                showSimulationLogin()
            }

            return
        }

        sensitiveLocked = false

        handler.removeCallbacks(
            sensitiveLockRunnable
        )

        handler.postDelayed(
            sensitiveLockRunnable,
            sensitiveTimeout
        )

        addSection("المشاركة الاستثمارية")

        addStatus(
            "حماية الشاشة",
            "مفعلة",
            green
        )

        addStatus(
            "القفل التلقائي",
            "3 دقائق",
            gold
        )

        addStatus(
            "التنفيذ المالي",
            "غير متاح في المحاكاة",
            red
        )

        addInfo(
            "التحقق الرسمي",
            "عند بناء الخدمة الحقيقية يجب التحقق من الأهلية عبر الجهات الحكومية المخولة وبالتفويضات والإجراءات النظامية."
        )

        addInfo(
            "الموانع القانونية",
            "لا تُتخذ قرارات قانونية من التطبيق نفسه، بل من خلال مصدر رسمي مخول وبالإجراءات المعتمدة."
        )

        addInfo(
            "البيانات",
            "المعلومات الحساسة يجب ألا تكون مكشوفة داخل APK، بل خلف خادم آمن وصلاحيات مناسبة."
        )

        addNavButton("↩ BADGER") {
            handler.removeCallbacks(
                sensitiveLockRunnable
            )
            showBadger()
        }
    }

    // =========================================================
    // SENSITIVE LOCK
    // =========================================================

    private fun showSensitiveLock() {

        handler.removeCallbacks(
            sensitiveLockRunnable
        )

        baseLayout("الخدمة مقفلة")

        addSection("حماية الخدمة الحساسة")

        addInfo(
            "انتهت مدة الجلسة الحساسة",
            "تم إغلاق هذه الشاشة لحماية المعلومات. يلزم إعادة المصادقة قبل العودة."
        )

        addStatus(
            "الحالة",
            "مقفلة",
            red
        )

        addCard(
            "🔐",
            "إعادة المصادقة",
            "العودة إلى مسار تسجيل الدخول",
            {
                sensitiveLocked = false
                showSimulationLogin()
            }
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

        // =========================================================
    // SESSION LOCK
    // =========================================================

    private fun lockAccount() {

        simulationLoggedIn = false
        sessionLocked = true

        handler.removeCallbacks(
            sessionLockRunnable
        )

        showSessionLock()
    }

    private fun showSessionLock() {

        handler.removeCallbacks(
            sessionLockRunnable
        )

        baseLayout("الجلسة مقفلة")

        addSection("حماية الحساب")

        addStatus(
            "الحالة",
            "مقفلة",
            red
        )

        addInfo(
            "سبب القفل",
            "انتهت مدة الجلسة أو تم قفل الحساب يدويًا."
        )

        addInfo(
            "إعادة الفتح",
            "يجب إعادة المصادقة قبل استعادة صلاحيات الحساب."
        )

        addCard(
            "🔐",
            "إعادة تسجيل الدخول",
            "فتح مسار المصادقة من جديد",
            {
                sessionLocked = false
                showSimulationLogin()
            }
        )

        addNavButton("⌂ النسخة العامة") {
            sessionLocked = false
            showHome()
        }
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private fun logoutSimulation() {

        simulationLoggedIn = false
        currentRole = UserRole.USER
        currentAccountName = "زائر"

        sessionLocked = false
        sensitiveLocked = false

        handler.removeCallbacks(
            sessionLockRunnable
        )

        handler.removeCallbacks(
            sensitiveLockRunnable
        )

        Toast.makeText(
            this,
            "تم تسجيل الخروج.",
            Toast.LENGTH_SHORT
        ).show()

        showHome()
    }

    // =========================================================
    // GUEST MODE
    // =========================================================

    private fun showGuestMode() {

        baseLayout("وضع الزائر")

        addSection("استكشاف")

        addStatus(
            "الحساب",
            "زائر",
            gold
        )

        addInfo(
            "الوصول",
            "يمكن للزائر استكشاف المحتوى العام دون الوصول إلى المكاتب أو البيانات الخاصة."
        )

        addCard(
            "◉",
            "الأسواق",
            "استعراض المجالات العامة",
            { showProducts() }
        )

        addCard(
            "◇",
            "الخدمات",
            "استعراض الخدمات العامة",
            { showServices() }
        )

        addCard(
            "⌕",
            "البحث",
            "البحث داخل المحتوى العام",
            { showSearch() }
        )

        addCard(
            "○",
            "تسجيل الدخول",
            "فتح حساب بصلاحية مناسبة",
            { showSimulationLogin() }
        )

        addNavButton("⌂ الرئيسية") {
            showHome()
        }
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private fun showSearch() {

        baseLayout("البحث")

        addSection("بحث CENTRAL MARKET")

        val searchBox =
            android.widget.EditText(this)

        searchBox.hint = "اكتب ما تريد البحث عنه"
        searchBox.setTextColor(white)
        searchBox.setHintTextColor(muted)
        searchBox.textSize = 15f
        searchBox.isSingleLine = true
        searchBox.background =
            roundedBackground(navy2)

        searchBox.setPadding(
            18,
            12,
            18,
            12
        )

        content.addView(
            searchBox,
            LinearLayout.LayoutParams(
                -1,
                58
            ).apply {
                setMargins(0, 0, 0, 14)
            }
        )

        addCard(
            "⌕",
            "بحث",
            "استكشاف المحتوى العام",
            {
                val query =
                    searchBox.text.toString().trim()

                if (query.isEmpty()) {
                    Toast.makeText(
                        this,
                        "اكتب كلمة للبحث.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "البحث المحاكي: $query",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================================================
    // FAVORITES
    // =========================================================

    private fun showFavorites() {

        baseLayout("المفضلة")

        addSection("المحتوى المحفوظ")

        addInfo(
            "المفضلة",
            "هذه الواجهة تمثل مكان العناصر التي يحفظها المستخدم للرجوع إليها لاحقًا."
        )

        addStatus(
            "الحالة",
            "لا توجد عناصر محفوظة في المحاكاة",
            gold
        )

        addNavButton("↩ الأسواق") {
            showProducts()
        }
    }

    // =========================================================
    // POINTS
    // =========================================================

    private fun showPoints() {

        baseLayout("النقاط")

        addSection("نظام النقاط")

        addStatus(
            "الرصيد",
            "0 نقطة — محاكاة",
            gold
        )

        addInfo(
            "الفكرة",
            "يمكن استخدام النقاط مستقبلًا ضمن نظام واضح للشروط والمزايا، دون اعتبارها أموالًا إلا إذا صُمم النظام قانونيًا لذلك."
        )

        addNavButton("↩ الحساب") {
            showAccountHome()
        }
    }

    // =========================================================
    // KITCHEN
    // =========================================================

    private fun showKitchen() {

        baseLayout("المطبخ")

        addSection("المطبخ والخدمات الغذائية")

        addCard(
            "◇",
            "الطلبات",
            "المطاعم والتوصيل",
            { showCategory("الطلبات والمطاعم") }
        )

        addCard(
            "⌂",
            "المنتجات",
            "منتجات ومستلزمات المطبخ",
            { showCategory("منتجات المطبخ") }
        )

        addNavButton("↩ الخدمات") {
            showServices()
        }
    }

    // =========================================================
    // CHARITY
    // =========================================================

    private fun showCharity() {

        baseLayout("الصدقة والمساعدة")

        addSection("المساعدة المجتمعية")

        addInfo(
            "الهدف",
            "دعم الأيتام والمحتاجين ضمن ضوابط واضحة وشفافة."
        )

        addStatus(
            "الحالة",
            "معلومات ومحاكاة",
            gold
        )

        addInfo(
            "الضوابط",
            "أي جمع أو تحويل أموال حقيقي يحتاج إلى جهة قانونية وآلية محاسبية معتمدة."
        )

        addNavButton("↩ الخدمات") {
            showServices()
        }
    }

    // =========================================================
    // CONNECTIVITY
    // =========================================================

    private fun showConnectivity() {

        baseLayout("اختبار الاتصال")

        addSection("حالة الشبكة")

        val online = isOnline()

        addStatus(
            "الإنترنت",
            if (online) "متصل" else "غير متصل",
            if (online) green else red
        )

        addInfo(
            "ملاحظة",
            "حالة الاتصال هنا هي حالة الجهاز الحالية فقط."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================================================
    // SECURITY
    // =========================================================

    private fun showSecurity() {

        baseLayout("الأمان والخصوصية")

        addSection("الحماية")

        addStatus(
            "حماية الشاشة",
            "FLAG_SECURE",
            green
        )

        addStatus(
            "قفل الجلسة",
            "10 دقائق",
            gold
        )

        addStatus(
            "الخدمات الحساسة",
            "3 دقائق",
            gold
        )

        addInfo(
            "البيانات الحساسة",
            "لا ينبغي وضع كلمات المرور أو البيانات المالية أو الحكومية السرية مباشرة داخل APK في النظام الحقيقي."
        )

        addInfo(
            "الصلاحيات",
            "كل دور يجب أن يرى فقط ما تسمح به الصلاحية المرتبطة بحسابه."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================================================
    // PRIVACY
    // =========================================================

    private fun showPrivacy() {

        baseLayout("الخصوصية")

        addSection("حماية البيانات")

        addInfo(
            "المبدأ",
            "تقليل جمع البيانات، وحماية البيانات الضرورية، وتحديد الوصول إليها حسب الحاجة."
        )

        addInfo(
            "الحسابات",
            "المعلومات الخاصة بالحسابات الإدارية والمالك ليست جزءًا من الواجهة العامة."
        )

        addNavButton("↩ الوثائق") {
            showDocuments()
        }
    }

    // =========================================================
    // RULES
    // =========================================================

    private fun showRules() {

        baseLayout("قواعد المنصة")

        addSection("القواعد")

        addInfo(
            "التجارة",
            "يجب الالتزام بالقوانين والأنظمة الخاصة بكل بلد ومنطقة."
        )

        addInfo(
            "الخدمات الحساسة",
            "لا تُنفذ المعاملات الحساسة أو المالية دون البنية والاعتمادات المطلوبة."
        )

        addInfo(
            "الحسابات",
            "الصلاحيات تعتمد على نوع الحساب والتحقق المناسب."
        )

        addNavButton("↩ الوثائق") {
            showDocuments()
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroy() {

        handler.removeCallbacks(
            sessionLockRunnable
        )

        handler.removeCallbacks(
            sensitiveLockRunnable
        )

        super.onDestroy()
    }
}

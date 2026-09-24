package com.central.market

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun baseLayout(): LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.rgb(248, 250, 252))

        val header = TextView(this)
        header.text = "CENTRAL MARKET"
        header.textSize = 25f
        header.setTypeface(null, Typeface.BOLD)
        header.setTextColor(Color.rgb(20, 45, 70))
        header.gravity = Gravity.CENTER
        header.setPadding(16, 30, 16, 20)

        root.addView(header)

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(18, 8, 18, 20)

        val scroll = ScrollView(this)
        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val navigation = LinearLayout(this)
        navigation.orientation = LinearLayout.HORIZONTAL
        navigation.gravity = Gravity.CENTER

        addNavButton(navigation, "الرئيسية") { showHome() }
        addNavButton(navigation, "البحث") { showSearch() }
        addNavButton(navigation, "المفضلة") { showFavorites() }
        addNavButton(navigation, "الحساب") { showAccount() }

        root.addView(navigation)

        return root
    }

    private fun addNavButton(
        parent: LinearLayout,
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = title
        button.textSize = 11f
        button.setOnClickListener { action() }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )
    }

    private fun addSection(title: String) {
        val text = TextView(this)
        text.text = title
        text.textSize = 21f
        text.setTypeface(null, Typeface.BOLD)
        text.setTextColor(Color.rgb(20, 45, 70))
        text.setPadding(5, 18, 5, 10)

        content.addView(text)
    }

    private fun addCard(
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = "$title\n$description"
        button.textSize = 15f
        button.setPadding(10, 18, 10, 18)
        button.setOnClickListener { action() }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        )
    }

    private fun showHome() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("مرحبًا بك في CENTRAL MARKET")

        val intro = TextView(this)
        intro.text =
            "منصة واحدة للأسواق والخدمات والتسويق والمركبات والمزيد."
        intro.textSize = 16f
        intro.gravity = Gravity.CENTER
        intro.setPadding(5, 5, 5, 18)

        content.addView(intro)

        addSection("الوصول السريع")

        addCard("👤 وضع الزائر", "استعمال الخدمات الأساسية دون تسجيل") {
            showGuestMode()
        }

        addCard("🛡️ أمان المنطقة", "مؤشر معلومات السلامة المحلية") {
            showSafety()
        }

        addCard("📦 المنتجات والخدمات", "استعراض العروض") {
            showProducts()
        }

        addSection("الأقسام الرئيسية")

        addCard("🚛 المركبات والشاحنات", "بيع وشراء وخدمات المركبات") {
            showCategory(
                "🚛 المركبات والشاحنات",
                "مركبات، شاحنات، معدات وآليات"
            )
        }

        addCard("📱 الهواتف والإلكترونيات", "أجهزة وهواتف وإلكترونيات") {
            showCategory(
                "📱 الهواتف والإلكترونيات",
                "هواتف وأجهزة وإلكترونيات"
            )
        }

        addCard("🍽️ المطاعم والتوصيل", "مطاعم وطلبات وتوصيل") {
            showCategory(
                "🍽️ المطاعم والتوصيل",
                "مطاعم وطلبات وخدمات توصيل"
            )
        }

        addCard("📢 التسويق والإعلانات", "عرض المنتجات والخدمات") {
            showAds()
        }

        addCard("🛠️ الخدمات", "خدمات متنوعة") {
            showCategory(
                "🛠️ الخدمات",
                "خدمات للأفراد والشركات"
            )
        }

        addSection("المشاريع والمجتمع")

        addCard("💡 الذكاء البشري", "أفكار وابتكارات ومشاريع المستقبل") {
            showHumanIntelligence()
        }

        addCard(
            "🤲 صندوق دعم الأيتام والمحتاجين",
            "مبادرات الدعم المجتمعي"
        ) {
            showCharity()
        }

        addCard("🍲 مطبخ الطيبات", "وصفات وأطعمة ومعلومات غذائية") {
            showKitchen()
        }

        addSection("خدمات قادمة")

        addCard("💳 الدفع الإلكتروني", "ربط وسائل الدفع لاحقًا") {
            showMessage("الدفع الإلكتروني قيد التجهيز")
        }

        addCard("📍 الخرائط والتتبع", "الموقع والتتبع") {
            showMessage("الخرائط والتتبع قيد التجهيز")
        }
    }

    private fun showGuestMode() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("👤 وضع الزائر")

        addCard(
            "استعراض المنتجات",
            "يمكنك تصفح المنتجات والخدمات"
        ) {
            showProducts()
        }

        addCard(
            "البحث",
            "البحث عن منتج أو خدمة"
        ) {
            showSearch()
        }

        addCard(
            "الإعلانات",
            "استعراض الإعلانات"
        ) {
            showAds()
        }

        addCard(
            "🔐 تسجيل الدخول",
            "للوصول إلى الميزات الشخصية"
        ) {
            showLogin()
        }

        add

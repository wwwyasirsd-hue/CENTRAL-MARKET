package com.central.market

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
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
        header.setPadding(16, 35, 16, 25)

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(18, 10, 18, 20)

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
        navigation.setPadding(5, 8, 5, 8)

        addNavButton(navigation, "الرئيسية") { showHome() }
        addNavButton(navigation, "البحث") { showSearch() }
        addNavButton(navigation, "الإعلانات") { showAds() }
        addNavButton(navigation, "الحساب") { showAccount() }

        root.addView(
            navigation,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        return root
    }

    private fun addNavButton(
        parent: LinearLayout,
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = title
        button.textSize = 12f
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
        text.setPadding(5, 20, 5, 12)

        content.addView(text)
    }

    private fun addCard(title: String, description: String, action: () -> Unit) {
        val button = Button(this)
        button.text = "$title\n$description"
        button.textSize = 15f
        button.setPadding(10, 20, 10, 20)
        button.setOnClickListener { action() }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 6, 0, 6)
            }
        )
    }

    private fun showHome() {
        setContentView(baseLayout())
        content.removeAllViews()

        val welcome = TextView(this)
        welcome.text = "مرحبًا بك في CENTRAL MARKET"
        welcome.textSize = 23f
        welcome.setTypeface(null, Typeface.BOLD)
        welcome.gravity = Gravity.CENTER
        welcome.setTextColor(Color.rgb(20, 45, 70))
        welcome.setPadding(5, 15, 5, 20)

        content.addView(welcome)

        val info = TextView(this)
        info.text =
            "منصة واحدة للخدمات والأسواق والمركبات والإعلانات والتسويق."
        info.textSize = 16f
        info.gravity = Gravity.CENTER
        info.setPadding(10, 0, 10, 20)

        content.addView(info)

        addSection("أقسام CENTRAL MARKET")

        addCard("🚛 المركبات والشاحنات", "بيع وشراء وخدمات المركبات") {
            showMessage("قسم المركبات والشاحنات")
        }

        addCard("📱 الهواتف والإلكترونيات", "هواتف وأجهزة وإلكترونيات") {
            showMessage("قسم الهواتف والإلكترونيات")
        }

        addCard("🍽️ المطاعم والتوصيل", "مطاعم وطلبات وتوصيل") {
            showMessage("قسم المطاعم والتوصيل")
        }

        addCard("📢 التسويق والإعلانات", "عرض المنتجات والخدمات") {
            showAds()
        }

        addCard("🛠️ الخدمات", "خدمات متنوعة في مكان واحد") {
            showMessage("قسم الخدمات")
        }

        addCard("💡 الابتكار والمشاريع", "أفكار ومشاريع المستقبل") {
            showMessage("قسم الابتكار والمشاريع")
        }

        addSection("خدمات قادمة")

        addCard("💳 الدفع الإلكتروني", "سيتم ربط خدمات الدفع لاحقًا") {
            showMessage("خدمة الدفع الإلكتروني قيد التجهيز")
        }

        addCard("📍 الخرائط والتتبع", "الخدمات الجغرافية والتتبع") {
            showMessage("الخرائط والتتبع قيد التجهيز")
        }
    }

    private fun showSearch() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🔎 البحث")

        val search = EditText(this)
        search.hint = "ابحث عن منتج أو خدمة..."
        search.textSize = 17f

        content.addView(search)

        val button = Button(this)
        button.text = "بحث"
        button.setOnClickListener {
            val query = search.text.toString().trim()

            if (query.isEmpty()) {
                showMessage("اكتب شيئًا للبحث")
            } else {
                showMessage("سيتم البحث عن: $query")
            }
        }

        content.addView(button)

        addSection("بحث سريع")

        addCard("🚛 المركبات", "البحث في المركبات") {
            showMessage("بحث المركبات")
        }

        addCard("📱 الإلكترونيات", "البحث في الإلكترونيات") {
            showMessage("بحث الإلكترونيات")
        }

        addCard("🍽️ المطاعم", "البحث في المطاعم") {
            showMessage("بحث المطاعم")
        }
    }

    private fun showAds() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📢 الإعلانات")

        addCard("🥉 BRONZE", "الإعلان الأساسي") {
            showMessage("إعلان BRONZE")
        }

        addCard("🥈 SILVER", "ظهور أكبر للإعلان") {
            showMessage("إعلان SILVER")
        }

        addCard("🥇 GOLD", "ظهور مميز للإعلان") {
            showMessage("إعلان GOLD")
        }

        addSection("إضافة إعلان")

        addCard("➕ إنشاء إعلان جديد", "إضافة منتج أو خدمة") {
            showMessage("إنشاء الإعلان سيُربط بالنظام لاحقًا")
        }
    }

    private fun showAccount() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("👤 الحساب")

        addCard("تسجيل الدخول", "الدخول إلى حسابك") {
            showMessage("نظام تسجيل الدخول قيد التجهيز")
        }

        addCard("إنشاء حساب", "إنشاء حساب جديد") {
            showMessage("إنشاء الحساب قيد التجهيز")
        }

        addCard("🌐 اللغة", "العربية / English / لغات أخرى") {
            showMessage("اختيار اللغات قيد التجهيز")
        }

        addCard("⚙️ الإعدادات", "إعدادات التطبيق") {
            showMessage("الإعدادات")
        }

        addCard("🔔 الإشعارات", "إدارة التنبيهات") {
            showMessage("الإشعارات قيد التجهيز")
        }

        addCard("ℹ️ عن CENTRAL MARKET", "معلومات عن المشروع") {
            showMessage(
                "CENTRAL MARKET\nمنصة رقمية متعددة الخدمات والأسواق"
            )
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

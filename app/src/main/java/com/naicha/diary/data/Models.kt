package com.naicha.diary.data

import com.naicha.diary.R

import kotlinx.serialization.Serializable
import java.util.UUID

enum class DrinkCategory(
    val label: String,
    val badge: String,
    val accent: Long,
) {
    Tea("茶饮", "茶", 0xFFC98A5B),
    Coffee("咖啡", "咖", 0xFF8B5E3C),
    Alcohol("酒", "酒", 0xFFB5651D),
    Other("其他", "饮", 0xFF6FA8C7),
    ;

    companion object {
        fun of(label: String): DrinkCategory =
            entries.firstOrNull { it.label == label } ?: Tea
    }
}

@Serializable
data class Drink(
    val id: String = UUID.randomUUID().toString(),
    val category: String = DrinkCategory.Tea.label,
    val brand: String = "",
    val name: String = "",
    val cupSize: String = "中杯",
    val sugar: String = "半糖",
    val ice: String = "少冰",
    val toppings: List<String> = emptyList(),
    val price: Double = 0.0,
    val rating: Int = 5,
    val mood: String = "😋",
    val note: String = "",
    val photoPath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
) {
    val categoryType: DrinkCategory get() = DrinkCategory.of(category)

    val calories: Int get() = estimateCalories(categoryType, cupSize, sugar, toppings.size)

    companion object {
        private fun estimateCalories(
            category: DrinkCategory,
            cupSize: String,
            sugar: String,
            toppingCount: Int,
        ): Int {
            val base = when (category) {
                DrinkCategory.Tea -> when (cupSize) {
                    "小杯" -> 180
                    "中杯" -> 260
                    "大杯" -> 340
                    else -> 420
                }
                DrinkCategory.Coffee -> when (cupSize) {
                    "小杯" -> 90
                    "中杯" -> 150
                    "大杯" -> 210
                    else -> 260
                }
                DrinkCategory.Alcohol -> when (cupSize) {
                    "单份" -> 90
                    "小杯" -> 130
                    "中杯" -> 180
                    "大杯" -> 240
                    else -> 320
                }
                DrinkCategory.Other -> when (cupSize) {
                    "小杯" -> 70
                    "中杯" -> 110
                    "大杯" -> 150
                    else -> 190
                }
            }
            val sugarFactor = when (sugar) {
                "全糖" -> 1.30
                "七分糖" -> 1.18
                "半糖" -> 1.00
                "三分糖" -> 0.86
                "无糖" -> 0.72
                "无酒精", "低度" -> 0.85
                "中度" -> 1.10
                "高度" -> 1.35
                "微醺" -> 1.00
                else -> 1.0
            }
            val toppingWeight = when (category) {
                DrinkCategory.Coffee -> 38
                DrinkCategory.Alcohol -> 20
                else -> 45
            }
            return (base * sugarFactor).toInt() + toppingCount * toppingWeight
        }
    }
}

data class Brand(
    val name: String,
    val category: DrinkCategory,
    val color: Long,
    val badge: String,
    val logo: Int = 0,
)

object Catalog {

    private val tea = DrinkCategory.Tea
    private val coffee = DrinkCategory.Coffee
    private val alcohol = DrinkCategory.Alcohol
    private val other = DrinkCategory.Other

    val brands: List<Brand> = listOf(
        // ── 茶饮（按 2026 门店规模 / 影响力排序）───────────────
        Brand("蜜雪冰城", tea, 0xFFE23A2E, "雪", R.drawable.logo_mixue),
        Brand("古茗", tea, 0xFF2B2B2B, "古", R.drawable.logo_guming),
        Brand("沪上阿姨", tea, 0xFFC7303E, "沪", R.drawable.logo_hushangayi),
        Brand("茶百道", tea, 0xFF2E86DE, "茶", R.drawable.logo_chabaidao),
        Brand("益禾堂", tea, 0xFFD7263D, "益", R.drawable.logo_yihetang),
        Brand("霸王茶姬", tea, 0xFF8E2F3F, "姬", R.drawable.logo_bawangchaji),
        Brand("喜茶", tea, 0xFF1C1C1E, "喜", R.drawable.logo_heytea),
        Brand("奈雪的茶", tea, 0xFF7DBE3F, "奈", R.drawable.logo_naixue),
        Brand("茶颜悦色", tea, 0xFF7B4A2D, "颜", R.drawable.logo_chayanyuese),
        Brand("CoCo都可", tea, 0xFFF26522, "Co", R.drawable.logo_coco),
        Brand("1点点", tea, 0xFF3B8C4E, "1", R.drawable.logo_yidiandian),
        Brand("书亦烧仙草", tea, 0xFFE23A2E, "书", R.drawable.logo_shuyi),
        Brand("甜啦啦", tea, 0xFFFF6B9D, "甜", R.drawable.logo_tianlala),
        Brand("7分甜", tea, 0xFFF4B223, "7", R.drawable.logo_qifentian),
        Brand("乐乐茶", tea, 0xFFC8A464, "乐", R.drawable.logo_lelecha),
        Brand("阿水大杯茶", tea, 0xFF2C7BE5, "水", R.drawable.logo_ashui),
        Brand("茉莉奶白", tea, 0xFF6FAE8E, "茉", R.drawable.logo_molinaibai),
        Brand("茶话弄", tea, 0xFFB08968, "话", R.drawable.logo_chahualong),
        Brand("快乐柠檬", tea, 0xFFE8B93C, "柠", R.drawable.logo_happylemon),
        Brand("爷爷不泡茶", tea, 0xFF8D6E63, "爷", R.drawable.logo_yeyebupaocha),
        Brand("桂桂茶", tea, 0xFFB08968, "桂", R.drawable.logo_guiguicha),
        Brand("KOI Thé", tea, 0xFF2E7D5B, "K", R.drawable.logo_koi),
        Brand("茶理宜世", tea, 0xFF5D6D7E, "理", R.drawable.logo_chalishiyi),
        Brand("茶山派", tea, 0xFFC0392B, "山", R.drawable.logo_chashanpai),
        Brand("察理王子", tea, 0xFFE67E22, "察", R.drawable.logo_chaliwangzi),
        Brand("初茶花月", tea, 0xFFE8A0BF, "初", R.drawable.logo_chuchahuayue),
        Brand("丘大叔柠檬茶", tea, 0xFFF1C40F, "丘", R.drawable.logo_qiudashu),
        Brand("山茶涧", tea, 0xFF7B4A2D, "涧", R.drawable.logo_shanchajian),
        Brand("茶月山", tea, 0xFF34495E, "月", R.drawable.logo_chayueshan),
        Brand("淡马茶坊", tea, 0xFF8E44AD, "淡", R.drawable.logo_danmachafang),
        Brand("放哈 Fundosa", tea, 0xFF27AE60, "放", R.drawable.logo_fundosa),
        Brand("哈茶福", tea, 0xFFE74C3C, "哈", R.drawable.logo_hachafu),
        Brand("洪都大拇指", tea, 0xFFC0392B, "洪", R.drawable.logo_hongdu),
        Brand("TCROSS 交茶点", tea, 0xFF2980B9, "T", R.drawable.logo_tcross),
        Brand("去茶山", tea, 0xFF8D9E6E, "去", R.drawable.logo_quchashan),
        Brand("拾叁茶", tea, 0xFF7B3F00, "拾", R.drawable.logo_shisancha),
        Brand("唐沫茶兮", tea, 0xFF2C3E50, "唐", R.drawable.logo_tangmochaxi),
        Brand("仙雨林", tea, 0xFF1E8449, "仙", R.drawable.logo_xianyulin),
        Brand("阿嫲手作", tea, 0xFF9C8B7A, "嫲", R.drawable.logo_amashouzuo),
        Brand("茶满方庭", tea, 0xFF8B4513, "满", R.drawable.logo_chamanfangting),
        Brand("椿风", tea, 0xFF1B4F9C, "椿", R.drawable.logo_chunfeng),
        Brand("放牛斑", tea, 0xFFA3C644, "牛", R.drawable.logo_fangniuban),
        Brand("壶见", tea, 0xFFD35400, "壶", R.drawable.logo_hujian),
        Brand("眷之茶", tea, 0xFF6C3483, "眷", R.drawable.logo_juanzhicha),
        Brand("卡旺卡", tea, 0xFF7D3C98, "卡", R.drawable.logo_kawangka),
        Brand("李山山茶事", tea, 0xFF4A4A4A, "李", R.drawable.logo_lishanshan),
        Brand("OT 另茶", tea, 0xFF95A5A6, "OT", R.drawable.logo_otlingcha),
        Brand("馬伍旺饮料厂", tea, 0xFFE85D75, "馬", R.drawable.logo_mawuwang),
        Brand("茉沏", tea, 0xFF7F8C8D, "茉", R.drawable.logo_moqi),
        Brand("让茶", tea, 0xFF27AE60, "让", R.drawable.logo_rangcha),
        Brand("真茶屋", tea, 0xFF2ECC71, "真", R.drawable.logo_zhenchawu),
        Brand("自制 / 其他", tea, 0xFF8D6E63, "自"),

        // ── 咖啡（2026 门店规模 + 金饕奖综合）─────────────────
        Brand("瑞幸咖啡", coffee, 0xFF1B3A6B, "瑞", R.drawable.logo_luckin),
        Brand("星巴克", coffee, 0xFF00704A, "星", R.drawable.logo_starbucks),
        Brand("Manner Coffee", coffee, 0xFF8B5E3C, "M", R.drawable.logo_manner),
        Brand("幸运咖", coffee, 0xFFF58220, "幸", R.drawable.logo_xingyunka),
        Brand("库迪咖啡", coffee, 0xFFE4002B, "库", R.drawable.logo_cudi),
        Brand("Tims 天好咖啡", coffee, 0xFFC8102E, "T", R.drawable.logo_tims),
        Brand("M Stand", coffee, 0xFF1A1A1A, "MS", R.drawable.logo_mstand),
        Brand("皮爷咖啡", coffee, 0xFFA6192E, "皮", R.drawable.logo_peets),
        Brand("比星咖啡", coffee, 0xFF1E5AA8, "比", R.drawable.logo_beanstar),
        Brand("挪瓦咖啡", coffee, 0xFF2D9CDB, "挪", R.drawable.logo_nowwa),
        Brand("肯悦咖啡", coffee, 0xFFC8102E, "肯"),
        Brand("Costa", coffee, 0xFF6B2737, "C"),
        Brand("麦咖啡", coffee, 0xFFC99A2E, "麦"),
        Brand("便利店咖啡", coffee, 0xFF5B7C99, "便"),
        Brand("手冲 / 自制", coffee, 0xFF6B4F3D, "手"),

        // ── 酒 ────────────────────────────────────────────────
        Brand("精酿啤酒", alcohol, 0xFFD98E04, "啤"),
        Brand("工业啤酒", alcohol, 0xFFE8C547, "酒"),
        Brand("清酒", alcohol, 0xFFB9A88C, "清"),
        Brand("威士忌", alcohol, 0xFFB5651D, "威"),
        Brand("红酒", alcohol, 0xFF722F37, "红"),
        Brand("白酒", alcohol, 0xFF8E99A4, "白"),
        Brand("鸡尾酒", alcohol, 0xFF9B59B6, "鸡"),
        Brand("梅酒", alcohol, 0xFF7D9D5C, "梅"),
        Brand("果酒", alcohol, 0xFFE8A0BF, "果"),
        Brand("米酒", alcohol, 0xFFC9B491, "米"),
        Brand("香槟 / 起泡酒", alcohol, 0xFFD4AF37, "槟"),
        Brand("酒吧 / 自制", alcohol, 0xFF6D4C41, "吧"),

        // ── 其他 ──────────────────────────────────────────────
        Brand("可乐", other, 0xFFE61A27, "可"),
        Brand("雪碧", other, 0xFF00A651, "碧"),
        Brand("果汁", other, 0xFFFF8C42, "汁"),
        Brand("气泡水", other, 0xFF7FC8E8, "气"),
        Brand("功能饮料", other, 0xFF1E90FF, "能"),
        Brand("酸奶", other, 0xFFC9B79C, "酸"),
        Brand("豆奶", other, 0xFFD8C49A, "豆"),
        Brand("椰子水", other, 0xFF8FC7A8, "椰"),
        Brand("柠檬茶", other, 0xFFE8C33C, "柠"),
        Brand("矿泉水", other, 0xFFA8D8F0, "水"),
        Brand("自制 / 其他", other, 0xFF8D6E63, "自"),
    )

    fun brandsOf(category: DrinkCategory): List<Brand> =
        brands.filter { it.category == category }

    fun brandOf(name: String): Brand =
        brands.firstOrNull { it.name == name } ?: brands.last()

    fun colorOf(name: String): Long = brandOf(name).color

    val cupSizes = listOf("小杯", "中杯", "大杯", "超大杯")
    val alcoholSizes = listOf("单份", "小杯", "中杯", "大杯", "整瓶")

    val sugars = listOf("全糖", "七分糖", "半糖", "三分糖", "无糖")
    val alcoholLevels = listOf("无酒精", "低度", "微醺", "中度", "高度")

    val ices = listOf("正常冰", "少冰", "去冰", "常温", "热饮")
    val alcoholTemps = listOf("冰镇", "常温", "温热")

    val toppings = listOf(
        "珍珠", "波霸", "椰果", "仙草冻", "布丁",
        "芋圆", "芋泥", "麻薯", "奶盖", "芝士奶盖",
        "奥利奥", "脆啵啵", "红豆", "燕麦", "西米",
        "青稞", "寒天", "烧仙草", "冰淇淋", "多肉",
    )

    val coffeeAdds = listOf(
        "浓缩", "燕麦奶", "厚乳", "焦糖", "香草",
        "奶油", "肉桂", "糖浆", "冰博客", "椰乳",
    )

    val alcoholAdds = listOf(
        "柠檬", "冰块", "苏打水", "果汁", "姜片", "话梅", "薄荷", "盐边",
    )

    val otherAdds = listOf(
        "柠檬", "冰块", "蜂蜜", "珍珠", "椰果", "薄荷", "青柠", "盐",
    )

    fun sizesFor(category: DrinkCategory): List<String> =
        if (category == DrinkCategory.Alcohol) alcoholSizes else cupSizes

    fun sugarLabel(category: DrinkCategory): String =
        if (category == DrinkCategory.Alcohol) "酒精度" else "甜度"

    fun sugarOptions(category: DrinkCategory): List<String> =
        if (category == DrinkCategory.Alcohol) alcoholLevels else sugars

    fun iceLabel(category: DrinkCategory): String =
        if (category == DrinkCategory.Alcohol) "温度" else "冰量 / 温度"

    fun iceOptions(category: DrinkCategory): List<String> =
        if (category == DrinkCategory.Alcohol) alcoholTemps else ices

    fun extraLabel(category: DrinkCategory): String = when (category) {
        DrinkCategory.Coffee -> "加料"
        DrinkCategory.Alcohol -> "配料"
        DrinkCategory.Other -> "加料"
        DrinkCategory.Tea -> "小料"
    }

    fun extraOptions(category: DrinkCategory): List<String> = when (category) {
        DrinkCategory.Coffee -> coffeeAdds
        DrinkCategory.Alcohol -> alcoholAdds
        DrinkCategory.Other -> otherAdds
        DrinkCategory.Tea -> toppings
    }

    val moods = listOf("😋", "🤤", "🥰", "😌", "😍", "😎", "🤔", "😴", "🥹", "🫠")

    val commonNames: Map<DrinkCategory, List<String>> = mapOf(
        DrinkCategory.Tea to listOf(
            "珍珠奶茶", "烤奶", "多肉葡萄", "杨枝甘露", "芝士奶盖茶",
            "茉莉奶绿", "黑糖珍珠鲜奶", "烧仙草奶茶", "芋泥波波", "四季春",
        ),
        DrinkCategory.Coffee to listOf(
            "生椰拿铁", "美式", "拿铁", "卡布奇诺", "澳白",
            "冷萃", "燕麦拿铁", "焦糖玛奇朵", "摩卡", "手冲",
        ),
        DrinkCategory.Alcohol to listOf(
            "精酿IPA", "拉格", "威士忌高球", "莫吉托", "金汤力",
            "梅子酒", "清酒", "赤霞珠", "鸡尾酒", "黄酒",
        ),
        DrinkCategory.Other to listOf(
            "可乐", "雪碧", "鲜橙汁", "气泡水", "酸奶",
            "椰子水", "柠檬茶", "豆奶", "能量饮料", "矿泉水",
        ),
    )
}

package com.moaga.app.ui.screens.expense

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moaga.app.R
import com.moaga.app.ui.components.expense.getProfileImage
import com.moaga.app.ui.theme.font_gothic_3
import com.moaga.app.ui.theme.font_gothic_4
import com.moaga.app.ui.theme.font_gothic_5

enum class BottomSheetMode {
    DETAIL,
    CATEGORY_SELECTION
}

data class CategoryItem(
    val id: Int,
    val name: String,
    val description: String
)

private val categories = listOf(
    CategoryItem(1, "미분류", "분류되지 않은 지출"),
    CategoryItem(2, "주유", "주유소, 연료비"),
    CategoryItem(3, "대형마트", "이마트, 홈플러스, 코스트코 등"),
    CategoryItem(4, "교통", "택시, 버스, 철도, 항공, 렌트"),
    CategoryItem(5, "교육/육아", "학비, 학원, 독서실, 이러닝, 어린이집, 유치원, 돌봄"),
    CategoryItem(6, "통신", "휴대폰, 인터넷, 유선전화"),
    CategoryItem(7, "해외", "기타 해외지출"),
    CategoryItem(8, "생활", "생활 관련 일반 지출"),
    CategoryItem(9, "외식", "식당"),
    CategoryItem(10, "배달", "배달앱, 자체 배달"),
    CategoryItem(11, "카페/간식", "카페/음료, 베이커리, 디저트"),
    CategoryItem(12, "마트/편의점", "마트, 편의점, 슈퍼마켓, 매점, 시장, 식재료, 생필품"),
    CategoryItem(13, "술/주점", "주류, 주점"),
    CategoryItem(14, "쇼핑", "백화점, 면세점, 아울렛, 잡화"),
    CategoryItem(15, "온라인 쇼핑", "온라인 쇼핑, 생필품, 식재료"),
    CategoryItem(16, "미용", "미용실, 메이크업, 화장품, 네일"),
    CategoryItem(17, "취미/여가", "도서, 영화, 전시, 관람, 공연, 체험, 스포츠"),
    CategoryItem(18, "주거/통신", "관리비, 전기세, 수도세, 가스비, 월세, 통신비"),
    CategoryItem(19, "구독", "음악, OTT"),
    CategoryItem(20, "자동차", "주유, 주차, 세차, 정비"),
    CategoryItem(21, "교육", "학비, 학원, 독서실, 이러닝"),
    CategoryItem(22, "생활편의", "가구, 가전, 컴퓨터, 주방, 침구"),
    CategoryItem(23, "여행/숙박", "숙박 및 각종 여행 제비용"),
    CategoryItem(24, "용돈/생활비", "부모님, 배우자, 자녀, 가족 용돈, 생활비"),
    CategoryItem(25, "병원/건강", "병원, 약국, 보조식품, 건강식품"),
    CategoryItem(26, "대출", "대출 원금, 이자 상환"),
    CategoryItem(27, "저축/투자", "적금, 연금, 펀드, 공제"),
    CategoryItem(28, "보험", "손해보험, 생명보험, 상조"),
    CategoryItem(29, "세금", "재산세, 주민세 등 각종 세금"),
    CategoryItem(30, "기부/후원", "기부, 후원, 종교, 복지"),
    CategoryItem(31, "경조/선물", "결혼, 장례, 돌, 생일, 기념일"),
    CategoryItem(32, "모임", "회비, 곗돈, 행사, 단체"),
    CategoryItem(33, "자녀", "어린이집, 유치원, 학교, 육아용품"),
    CategoryItem(34, "반려동물", "사료/간식, 병원, 용품"),
    CategoryItem(35, "ATM", "ATM 출금"),
    CategoryItem(36, "이체", "계좌이체"),
    CategoryItem(37, "카드대금", "카드대금 상환"),
    CategoryItem(38, "기타", "기타 지출")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItemBottomSheet(
    item: ExpenseItem?,
    onDismiss: () -> Unit,
    currentUserName: String,
    onCategoryChange: (CategoryItem) -> Unit = {},
    onExcludeToggle: (Boolean) -> Unit = {},
    onSaveCategory: (transactionId: Int, categoryId: Int, categoryName: String, exclude: Boolean) -> Unit
) {
    var currentMode by remember { mutableStateOf(BottomSheetMode.DETAIL) }
    var isExcluded by remember(item) { mutableStateOf(item?.isExcluded ?: false) }
    var selectedCategory by remember(item) { mutableStateOf(item?.category ?: "") }

    if (item != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = Color.White,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 상단 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentMode == BottomSheetMode.DETAIL) "상세 지출 내역" else "카테고리 변경",
                        fontSize = 18.sp,
                        fontFamily = font_gothic_5,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    IconButton(
                        onClick = {
                            if (currentMode == BottomSheetMode.CATEGORY_SELECTION) {
                                currentMode = BottomSheetMode.DETAIL
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))


                // 내용
                when (currentMode) {
                    BottomSheetMode.DETAIL -> {
                        ExpenseDetailContent(
                            item = item.copy(category = selectedCategory),
                            isExcluded = isExcluded,
                            onExcludeToggle = { excluded ->
                                isExcluded = excluded
                                onExcludeToggle(excluded)
                            },
                            onCategoryChangeClick = { currentMode = BottomSheetMode.CATEGORY_SELECTION },
                            onDismiss = onDismiss,
                            isEditable = (item.person == currentUserName),
                            onSaveCategory = { transactionId, categoryId, categoryName, exclude ->
                                onSaveCategory(transactionId, categoryId, categoryName, exclude)
                            }
                        )
                    }
                    BottomSheetMode.CATEGORY_SELECTION -> {
                        CategorySelectionContent(
                            onCategorySelect = { category ->
                                selectedCategory = category.name
                                onCategoryChange(category)
                                currentMode = BottomSheetMode.DETAIL
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ExpenseDetailContent(
    item: ExpenseItem,
    isExcluded: Boolean,
    onExcludeToggle: (Boolean) -> Unit,
    onCategoryChangeClick: () -> Unit,
    onDismiss: () -> Unit,
    isEditable: Boolean,
    onSaveCategory: (transactionId: Int, categoryId: Int, categoryName: String, exclude: Boolean) -> Unit
) {
    Column {
        // 시간 정보
        DetailRow("시간", "${item.date} ${item.time}", textColor = Color(0xFF666666))
        Spacer(modifier = Modifier.height(16.dp))

        // 멤버 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("멤버", fontSize = 14.sp, fontFamily = font_gothic_4, color = Color(0xFF666666))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(getProfileImage(item.person)),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(20.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(item.person, fontSize = 14.sp, fontFamily = font_gothic_4,
                    fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 내용
        DetailRow("내용", item.name)
        Spacer(modifier = Modifier.height(16.dp))

        // 카테고리
        Row(
            modifier = Modifier.fillMaxWidth().then(
                if (isEditable) Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onCategoryChangeClick() } else Modifier
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("카테고리", fontSize = 14.sp, fontFamily = font_gothic_4, color = Color(0xFF666666))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.category, fontSize = 14.sp, fontFamily = font_gothic_4,
                    fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                if (isEditable) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = "카테고리 변경",
                        tint = Color(0xFF18A87E), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 금액
        DetailRow("금액", item.amount, textColor = Color.Black, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        // 지출내역 제외 토글
        if (isEditable) { // ✅ 내 거래일 때만 보이게
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("지출내역 제외", fontSize = 16.sp, fontFamily = font_gothic_4, color = Color(0xFF1A1A1A))
                Switch(
                    checked = isExcluded,
                    onCheckedChange = onExcludeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF18A87E),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE0E0E0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 카테고리 변경 버튼
        if (isEditable) {
            Button(
                onClick = {
                    val category = categories.firstOrNull { it.name == item.category }
                    if (category != null) {
                        onSaveCategory(item.id, category.id, category.name, isExcluded)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18A87E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("수정 완료", fontSize = 16.sp, fontFamily = font_gothic_4,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp))

            }
        }

        Spacer(modifier = Modifier.height(42.dp))
    }
}

@Composable
private fun CategorySelectionContent(onCategorySelect: (CategoryItem) -> Unit) {
    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
        items(categories) { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategorySelect(category) }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                val iconResId = remember(category.id) {
                    val resName = "category_${category.id}"
                    context.resources.getIdentifier(resName, "drawable", context.packageName)
                }

                if (iconResId != 0) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = category.name,
                        modifier = Modifier
                            .size(36.dp) // 아이콘 크기 조절
                            .padding(end = 12.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        category.name,
                        fontSize = 16.sp,
                        fontFamily = font_gothic_4,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        category.description,
                        fontSize = 12.sp,
                        fontFamily = font_gothic_3,
                        color = Color(0xFF666666)
                    )
                }
            }
            if (category != categories.last()) {
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = Color(0xFFE0E0E0)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    textColor: Color = Color(0xFF1A1A1A),
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontFamily = font_gothic_4, color = Color(0xFF666666))
        Text(value, fontSize = 14.sp, fontFamily = font_gothic_4,
            fontWeight = fontWeight, color = textColor)
    }
}

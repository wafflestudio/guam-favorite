package waffle.guam.service

import waffle.guam.db.DevType.TechStackDTO


class SearchEngine {
    // Unicode of Hangul
    private val begin: Int = 44032 // = 가
    private val end: Int = 55203   // = 힣
    private val textArr: CharArray = "가깋낗닣딯띻맇밓빟삫싷잏짛찧칳킿팋핗힣".toCharArray()
    private val choSung: CharArray = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅇㅈㅉㅊㅋㅌㅍㅎ".toCharArray()
    private val choSungV: CharArray = "가까나다따라마바빠사아자짜차카타파하".toCharArray()

    private fun isChoSung(c: Char): Boolean {
        return choSung.count { it == c } > 0;
    }

    private fun isHangul(c: Char): Boolean {
        return isChoSung(c) || (c.toInt() in begin..end)
    }

    private fun findChoSung(c: Char): Char {
        // 초성이면 '가, 다 ..' 부터, 아니고 '김, 송 등등..'이면 거기부터
        for (i in choSung.indices) {
            if (choSung[i] == c) return choSungV[i]
        }
        return c
    }

    private fun findJongSung(c: Char): Char{
        var res: Char = '0'
        if(isChoSung(c)) {
            for(i in choSung.indices) {
                // 초성이면 정보가 없으므로 해당 초성의 끝자락(깋, 힣)까지.
                if (choSung[i] == c) res = textArr[i+1]
            }
        }
        else {
            for(i in textArr.indices ) {

                // 한글 한글자를 28로 나눈 나머지 -> 그 글자의 종성이다.
                // 종성으로 올 수 있는 가짓수가 28이라 그럼
                if(  c >= textArr[i] && c < textArr[i+1] )
                    res = if ( (textArr[i+1].toInt() - c.toInt()) % 28 != 27 ) {
                        // 종성이 있으면 깋, 닣, 딯 등등과의 종성 차이가 27보다 작음
                        c // 종성이 있으면 해당 종성까지 범위를 좁힘
                    } else {
                        val a = textArr[i + 1] - (textArr[i + 1].minus(c)) / 28 * 28
                        a // 종성이 없을 경우 범위는 ㅎ받침까지
                    }
            }
        }
        return res
    }


    fun containsQ(word: String, keyword: String): Int {
        val wl = word.length
        val kl = keyword.length
        for (i in 0..(wl - kl)) {
            var flag = true
            for (j in 0 until kl) {
                val a = word[i + j]
                val b = keyword[j]
                if (isHangul(a) && isHangul(b)){
                    // findChoSung ~ findJongSung 사이에 있어야지만 그 글자로 인정
                    // ex) '기' 로 '길' 찾기 -> 기 ~ 깋 사이에 길 해당 됨
                    // ex) 'ㄴ'로 '너' 찾기 -> ㄴ ~ 닣 사이에 해당 됨
                    // ex) 'ㄹ'로 '랄' 찾기 -> 라 ~ 랄 사이에 해당 됨
                    // 고로 다 찾을 수 있다.
                    if ( findChoSung(b) > a || a > findJongSung(b) )
                    flag = false
                }
                else if (a.toLowerCase() != b.toLowerCase())
                    flag = false
            }
            if (flag) return i + kl
        }
        return -1
    }

    // returns total cnt of q string in dic
    fun search( dic: List<String>, q: String ): Int {
        val queries = q.split(", ")
        var cnt = 0
        for (name in dic) {
                for(q in queries) {
                    // OR 처리, AND 우선 -> cnt 로 구현 가능
                    if (containsQ(name, q) >= 0) cnt++
                }
            }
        return cnt
    }


}
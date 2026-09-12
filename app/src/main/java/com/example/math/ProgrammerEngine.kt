package com.example.math

/**
 * Programmer Calculator Engine.
 * Supports conversions across bases (Hex, Dec, Oct, Bin) and bitwise operations.
 */
object ProgrammerEngine {

    enum class WordSize(val bits: Int, val mask: ULong, val label: String) {
        QWORD(64, ULong.MAX_VALUE, "64-bit QWORD"),
        DWORD(32, 0xFFFFFFFFu, "32-bit DWORD"),
        WORD(16, 0xFFFFu, "16-bit WORD"),
        BYTE(8, 0xFFu, "8-bit BYTE")
    }

    data class ProgrammerState(
        val value: ULong,
        val wordSize: WordSize = WordSize.QWORD
    ) {
        val hexString: String get() = (value and wordSize.mask).toString(16).uppercase()
        val decString: String get() {
            // Signed representation depending on wordSize
            val masked = value and wordSize.mask
            return when (wordSize) {
                WordSize.QWORD -> masked.toLong().toString()
                WordSize.DWORD -> masked.toUInt().toInt().toString()
                WordSize.WORD -> masked.toUShort().toShort().toString()
                WordSize.BYTE -> masked.toUByte().toByte().toString()
            }
        }
        val octString: String get() = (value and wordSize.mask).toString(8)
        val binString: String get() {
            val raw = (value and wordSize.mask).toString(2)
            val padded = raw.padStart(wordSize.bits, '0')
            // Group by 4 bits for nice formatting
            return padded.chunked(4).joinToString(" ")
        }
        val activeBitsCount: Int get() = (value and wordSize.mask).countOneBits()
    }

    fun parseInput(text: String, radix: Int, wordSize: WordSize): Result<ULong> {
        return runCatching {
            val clean = text.replace(" ", "").trim()
            if (clean.isEmpty()) return@runCatching 0uL
            val parsed = clean.toULong(radix)
            parsed and wordSize.mask
        }
    }

    fun applyBitwise(a: ULong, b: ULong, op: String, wordSize: WordSize): ULong {
        val mask = wordSize.mask
        val res = when (op) {
            "AND" -> a and b
            "OR" -> a or b
            "XOR" -> a xor b
            "NAND" -> (a and b).inv()
            "NOR" -> (a or b).inv()
            "LSH", "<<" -> (a shl (b.toInt() % wordSize.bits))
            "RSH", ">>" -> (a shr (b.toInt() % wordSize.bits))
            else -> a
        }
        return res and mask
    }

    fun not(a: ULong, wordSize: WordSize): ULong {
        return (a.inv()) and wordSize.mask
    }
}

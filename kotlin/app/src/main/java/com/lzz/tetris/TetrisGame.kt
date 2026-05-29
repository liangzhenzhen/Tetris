package com.lzz.tetris

import kotlin.random.Random

/**
 * 俄罗斯方块游戏逻辑核心
 *
 * 棋盘: 10 列 x 20 行
 * board[row][col]: 0 = 空, 1-7 = 方块颜色索引
 */
class TetrisGame {

    companion object {
        const val COLS = 10
        const val ROWS = 20

        // 7 种标准方块的形状定义 (使用 4x4 矩阵，或更小)
        // I, O, T, S, Z, J, L
        val SHAPES = arrayOf(
            // I (cyan)
            arrayOf(
                intArrayOf(0, 0, 0, 0),
                intArrayOf(1, 1, 1, 1),
                intArrayOf(0, 0, 0, 0),
                intArrayOf(0, 0, 0, 0)
            ),
            // O (yellow)
            arrayOf(
                intArrayOf(1, 1),
                intArrayOf(1, 1)
            ),
            // T (purple)
            arrayOf(
                intArrayOf(0, 1, 0),
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 0)
            ),
            // S (green)
            arrayOf(
                intArrayOf(0, 1, 1),
                intArrayOf(1, 1, 0),
                intArrayOf(0, 0, 0)
            ),
            // Z (red)
            arrayOf(
                intArrayOf(1, 1, 0),
                intArrayOf(0, 1, 1),
                intArrayOf(0, 0, 0)
            ),
            // J (blue)
            arrayOf(
                intArrayOf(1, 0, 0),
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 0)
            ),
            // L (orange)
            arrayOf(
                intArrayOf(0, 0, 1),
                intArrayOf(1, 1, 1),
                intArrayOf(0, 0, 0)
            )
        )
    }

    // 棋盘: 0 = 空, 1-7 = 对应方块颜色
    val board = Array(ROWS) { IntArray(COLS) { 0 } }

    // 当前方块
    var currentShape: Array<IntArray> = arrayOf()
    var currentType = 0       // 0-6 对应 SHAPES 索引
    var currentX = 0          // 方块左上角列坐标
    var currentY = 0          // 方块左上角行坐标

    // 下一个方块
    var nextType = Random.nextInt(7)

    var score = 0
    var isGameOver = false
    var isPaused = false

    // 消行动画状态
    val clearingRows = mutableListOf<Int>()
    var clearAnimProgress = 0f
    private var clearAnimStarted = false

    init {
        spawnPiece()
    }

    /** 生成新方块 */
    fun spawnPiece() {
        currentType = nextType
        nextType = Random.nextInt(7)
        currentShape = SHAPES[currentType].map { it.clone() }.toTypedArray()
        currentX = COLS / 2 - currentShape[0].size / 2
        currentY = 0

        if (!isValidPosition(currentShape, currentX, currentY)) {
            isGameOver = true
        }
    }

    /** 获取下一个方块的形状（用于预览） */
    fun getNextShape(): Array<IntArray> {
        return SHAPES[nextType].map { it.clone() }.toTypedArray()
    }


    /** 检查方块在指定位置是否有效 */
    fun isValidPosition(shape: Array<IntArray>, x: Int, y: Int): Boolean {
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val boardX = x + c
                    val boardY = y + r
                    if (boardX < 0 || boardX >= COLS || boardY >= ROWS) return false
                    if (boardY < 0) continue
                    if (board[boardY][boardX] != 0) return false
                }
            }
        }
        return true
    }

    /** 左移 */
    fun moveLeft(): Boolean {
        if (isGameOver || isPaused) return false
        if (isValidPosition(currentShape, currentX - 1, currentY)) {
            currentX--
            return true
        }
        return false
    }

    /** 右移 */
    fun moveRight(): Boolean {
        if (isGameOver || isPaused) return false
        if (isValidPosition(currentShape, currentX + 1, currentY)) {
            currentX++
            return true
        }
        return false
    }

    /** 下移一格 */
    fun moveDown(): Boolean {
        if (isGameOver || isPaused) return false
        if (isValidPosition(currentShape, currentX, currentY + 1)) {
            currentY++
            return true
        }
        // 无法下移，锁定方块
        lockPiece()
        return false
    }

    /** 硬降 (直接落到底) */
    fun hardDrop() {
        if (isGameOver || isPaused) return
        while (isValidPosition(currentShape, currentX, currentY + 1)) {
            currentY++
        }
        lockPiece()
    }

    /** 旋转 (顺时针) */
    fun rotate(): Boolean {
        if (isGameOver || isPaused) return false
        val rotated = rotateShape(currentShape)
        // 尝试基本旋转
        if (isValidPosition(rotated, currentX, currentY)) {
            currentShape = rotated
            return true
        }
        // Wall kick: 尝试左移/右移
        val kicks = intArrayOf(-1, 1, -2, 2, 0)
        for (dx in kicks) {
            if (isValidPosition(rotated, currentX + dx, currentY)) {
                currentShape = rotated
                currentX += dx
                return true
            }
        }
        // 也尝试上移 (对于 I 方块在底部)
        for (dy in intArrayOf(-1, -2)) {
            if (isValidPosition(rotated, currentX, currentY + dy)) {
                currentShape = rotated
                currentY += dy
                return true
            }
        }
        return false
    }

    /** 顺时针旋转形状矩阵 */
    private fun rotateShape(shape: Array<IntArray>): Array<IntArray> {
        val rows = shape.size
        val cols = shape[0].size
        val rotated = Array(cols) { IntArray(rows) { 0 } }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                rotated[c][rows - 1 - r] = shape[r][c]
            }
        }
        return rotated
    }

    /** 锁定当前方块到棋盘 */
    private fun lockPiece() {
        for (r in currentShape.indices) {
            for (c in currentShape[r].indices) {
                if (currentShape[r][c] != 0) {
                    val boardY = currentY + r
                    val boardX = currentX + c
                    if (boardY in 0 until ROWS && boardX in 0 until COLS) {
                        board[boardY][boardX] = currentType + 1
                    }
                }
            }
        }
        // 检测并标记要消除的行，启动闪光动画
        val fullRows = mutableListOf<Int>()
        for (row in 0 until ROWS) {
            if (board[row].all { it != 0 }) {
                fullRows.add(row)
            }
        }
        if (fullRows.isNotEmpty()) {
            clearingRows.clear()
            clearingRows.addAll(fullRows)
            clearAnimProgress = 0f
            clearAnimStarted = true
        } else {
            spawnPiece()
        }
    }

    /** 更新消行动画，返回 true 表示动画结束需要清除行 */
    fun updateClearAnim(deltaMs: Long): Boolean {
        if (!clearAnimStarted || clearingRows.isEmpty()) return false
        clearAnimProgress += deltaMs / 450f  // 碎裂动画时长，亮暗分明  // 300ms 动画时长
        if (clearAnimProgress >= 1f) {
            clearAnimProgress = 1f
            doClearLines()
            clearingRows.clear()
            clearAnimStarted = false
            spawnPiece()
            return true
        }
        return false
    }

    /** 实际消除行 */
    private fun doClearLines() {
        val linesCleared = clearingRows.size
        // 从下到上消除，避免索引偏移问题
        val rowsToClear = clearingRows.toSet()
        var writeRow = ROWS - 1
        for (readRow in ROWS - 1 downTo 0) {
            if (readRow !in rowsToClear) {
                board[writeRow] = board[readRow]
                writeRow--
            }
        }
        while (writeRow >= 0) {
            board[writeRow] = IntArray(COLS) { 0 }
            writeRow--
        }
        score += when (linesCleared) {
            1 -> 100
            2 -> 300
            3 -> 500
            4 -> 800
            else -> 0
        }
    }

    /** 获取当前方块的绝对坐标列表（用于绘制） */
    fun getCurrentPieceCells(): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        for (r in currentShape.indices) {
            for (c in currentShape[r].indices) {
                if (currentShape[r][c] != 0) {
                    cells.add(Pair(currentY + r, currentX + c))
                }
            }
        }
        return cells
    }

    /** 重置游戏 */
    fun reset() {
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                board[r][c] = 0
            }
        }
        score = 0
        isGameOver = false
        isPaused = false
        nextType = Random.nextInt(7)
        spawnPiece()
    }
}

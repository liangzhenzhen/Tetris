package com.lzz.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * 俄罗斯方块自定义视图
 *
 * 支持两种模式:
 * - GAME: 绘制完整游戏棋盘 (10x20)
 * - PREVIEW: 绘制下一个方块预览
 */
class TetrisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val MODE_GAME = 0
        const val MODE_PREVIEW = 1

        // 统一金砖颜色
        const val GOLD_LIGHT = -0x174FC0    // 亮金 #E8B040
        const val GOLD_MID   = -0x255AE0    // 中金 #DAA520
        const val GOLD_DARK  = -0x4779F5    // 暗金 #B8860B
    }

    var mode = MODE_GAME
    var game: TetrisGame? = null

    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val piecePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pieceHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ghostPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        gridPaint.color = 0xFF333333.toInt()
        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth = 1f

        ghostPaint.color = 0x40FFFFFF.toInt()
        ghostPaint.style = Paint.Style.FILL

        textPaint.color = Color.WHITE
        textPaint.textSize = 48f
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (mode) {
            MODE_GAME -> drawGame(canvas)
            MODE_PREVIEW -> drawPreview(canvas)
        }
    }

    private fun drawGame(canvas: Canvas) {
        val g = game ?: return
        val cellW = width.toFloat() / TetrisGame.COLS
        val cellH = height.toFloat() / TetrisGame.ROWS

        // 棋盘背景
        boardPaint.color = 0xFF1A1A2E.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boardPaint)

        // 绘制已锁定的方块
        for (row in 0 until TetrisGame.ROWS) {
            for (col in 0 until TetrisGame.COLS) {
                val value = g.board[row][col]
                if (value > 0) {
                    drawCell(canvas, col * cellW, row * cellH, cellW, cellH, value - 1)
                }
            }
        }

        // 绘制当前方块
        drawPieceCells(
            canvas, g.currentShape, g.currentX, g.currentY,
            cellW, cellH, g.currentType, false
        )

        // 绘制网格线
        for (row in 0..TetrisGame.ROWS) {
            canvas.drawLine(0f, row * cellH, width.toFloat(), row * cellH, gridPaint)
        }
        for (col in 0..TetrisGame.COLS) {
            canvas.drawLine(col * cellW, 0f, col * cellW, height.toFloat(), gridPaint)
        }

        // 消行动画: 金砖闪烁
        if (g.clearingRows.isNotEmpty()) {
            drawClearFlash(canvas, cellW, cellH, g)
        }

        // 游戏结束遮罩
        if (g.isGameOver) {
            boardPaint.color = 0x80000000.toInt()
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boardPaint)

            textPaint.textSize = height / 10f
            canvas.drawText(
                "游戏结束",
                width / 2f,
                height / 2f,
                textPaint
            )
            textPaint.textSize = height / 20f
            canvas.drawText(
                "得分: ${g.score}",
                width / 2f,
                height / 2f + height / 10f,
                textPaint
            )
        }
    }

    private fun drawPreview(canvas: Canvas) {
        val g = game
        if (g == null) {
            boardPaint.color = 0xFF1A1A2E.toInt()
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boardPaint)
            return
        }

        val shape = g.getNextShape()
        val shapeRows = shape.size
        val shapeCols = shape[0].size

        val margin = 4f
        val availW = width - 2 * margin
        val availH = height - 2 * margin

        val cellSize = minOf(availW / shapeCols, availH / shapeRows)

        val offsetX = (width - cellSize * shapeCols) / 2f
        val offsetY = (height - cellSize * shapeRows) / 2f

        boardPaint.color = 0xFF1A1A2E.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boardPaint)

        val pieceType = g.nextType

        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val left = offsetX + c * cellSize + 1
                    val top = offsetY + r * cellSize + 1
                    val right = left + cellSize - 2
                    val bottom = top + cellSize - 2

                    piecePaint.color = GOLD_LIGHT
                    canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, piecePaint)

                    // 高光
                    pieceHighlightPaint.color = 0x40FFFFFF.toInt()
                    canvas.drawRect(left + 2, top + 2, left + cellSize / 2f, top + cellSize / 2f, pieceHighlightPaint)
                }
            }
        }
    }

    /** 获取 Ghost 投影 Y 坐标 */
    private fun getGhostY(game: TetrisGame): Int {
        var ghostY = game.currentY
        while (game.isValidPosition(game.currentShape, game.currentX, ghostY + 1)) {
            ghostY++
        }
        return ghostY
    }

    /** 绘制方块的所有小格 */
    private fun drawPieceCells(
        canvas: Canvas,
        shape: Array<IntArray>,
        offsetX: Int,
        offsetY: Int,
        cellW: Float,
        cellH: Float,
        pieceType: Int,
        isGhost: Boolean
    ) {
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val boardY = offsetY + r
                    val boardX = offsetX + c
                    if (boardY >= 0) {
                        if (isGhost) {
                            val left = boardX * cellW + 2
                            val top = boardY * cellH + 2
                            val right = left + cellW - 4
                            val bottom = top + cellH - 4
                            canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, ghostPaint)
                        } else {
                            drawCell(canvas, boardX * cellW, boardY * cellH, cellW, cellH, pieceType)
                        }
                    }
                }
            }
        }
    }

    /** 绘制金砖质感的单个格子 (无底部阴影，均匀金色) */
    private fun drawCell(canvas: Canvas, left: Float, top: Float, w: Float, h: Float, pieceType: Int) {
        val inset = 1.5f
        val l = left + inset
        val t = top + inset
        val r = left + w - inset
        val b = top + h - inset
        val radius = 3f

        // 砖底: 用中调金色，不用暗色
        piecePaint.shader = null
        piecePaint.color = GOLD_MID
        canvas.drawRoundRect(l, t, r, b, radius, radius, piecePaint)

        // 渐变主色: 顶部到中部渐亮，底部保留中调
        val gradient = LinearGradient(
            l, t, l, b,
            intArrayOf(GOLD_LIGHT, GOLD_LIGHT, GOLD_MID),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        piecePaint.shader = gradient
        val innerMargin = 1.2f
        canvas.drawRoundRect(
            l + innerMargin, t + innerMargin,
            r - innerMargin, b - innerMargin,
            radius * 0.7f, radius * 0.7f, piecePaint
        )
        piecePaint.shader = null

        // 四边高光线 (亮金边框)
        piecePaint.color = 0x55FFFFFF.toInt()
        piecePaint.style = Paint.Style.STROKE
        piecePaint.strokeWidth = 1.0f
        canvas.drawLine(l + 3, t + 2f, r - 6, t + 2f, piecePaint)
        canvas.drawLine(l + 2f, t + 3, l + 2f, b - 6, piecePaint)
        canvas.drawLine(l + 3, b - 2f, r - 6, b - 2f, piecePaint)
        canvas.drawLine(r - 2f, t + 3, r - 2f, b - 6, piecePaint)
        piecePaint.style = Paint.Style.FILL

        // 顶部金属反光
        val highlightGrad = LinearGradient(
            l + w * 0.15f, t + 2,
            l + w * 0.6f, t + h * 0.35f,
            intArrayOf(0x55FFFFFF.toInt(), 0x00FFFFFF.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        pieceHighlightPaint.shader = highlightGrad
        canvas.drawRoundRect(
            l + innerMargin + 1, t + innerMargin + 1,
            r - innerMargin - 1, t + h * 0.4f,
            radius * 0.5f, radius * 0.5f, pieceHighlightPaint
        )
        pieceHighlightPaint.shader = null

        // 砖面纹理横线
        piecePaint.color = 0x15FFFFFF.toInt()
        piecePaint.strokeWidth = 0.6f
        val cy = t + h * 0.45f
        canvas.drawLine(l + 4, cy, r - 4, cy, piecePaint)
        canvas.drawLine(l + 3, cy + h * 0.22f, r - 5, cy + h * 0.22f, piecePaint)

        // 外边框
        piecePaint.color = 0x50FFD700.toInt()
        piecePaint.strokeWidth = 0.8f
        canvas.drawRoundRect(l, t, r, b, radius, radius, piecePaint)
        piecePaint.style = Paint.Style.FILL
    }

    /** 绘制消行动画: 两闪 + 砖块碎裂飞散 */
    private fun drawClearFlash(canvas: Canvas, cellW: Float, cellH: Float, g: TetrisGame) {
        val progress = g.clearAnimProgress
        // 三段: ⅓亮 → ⅓暗 → ⅓碎裂(不闪)
        val intensity = when {
            progress < 0.333f -> 1f
            else              -> 0f
        }
        val flashAlpha = if (intensity > 0f) 180 else 0
        val flashColor = (flashAlpha shl 24) or 0x00FFD700.toInt()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (row in g.clearingRows) {
            val y = row * cellH

            // 整行金色覆盖
            paint.color = flashColor
            canvas.drawRect(0f, y, width.toFloat(), y + cellH, paint)

            // 光晕
            val glowGrad = LinearGradient(
                0f, y - cellH * 2f, 0f, y + cellH * 3f,
                intArrayOf(0, flashColor, 0),
                floatArrayOf(0f, 0.22f, 1f), Shader.TileMode.CLAMP
            )
            paint.shader = glowGrad
            canvas.drawRect(0f, y - cellH * 2f, width.toFloat(), y + cellH * 3f, paint)
            paint.shader = null

            // === 砖块碎裂效果 (仅第二闪时裂开) ===
            if (progress > 0.666f) {  // 最后⅓ 碎裂
                drawShatteredBricks(canvas, row, cellW, cellH, progress, intensity)
            }
        }
    }

    /** 砖块碎裂: 每格拆成碎片向外飞散 */
    private fun drawShatteredBricks(canvas: Canvas, row: Int, cellW: Float, cellH: Float,
                                     progress: Float, intensity: Float) {
        val fragmentsPerCell = 3
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (col in 0 until TetrisGame.COLS) {
            val cx = col * cellW + cellW / 2f
            val cy = row * cellH + cellH / 2f

            for (i in 0 until fragmentsPerCell) {
                val seed = col * 7919 + row * 6271 + i * 2063
                val r1 = pseudoRandom(seed)
                val r2 = pseudoRandom(seed + 1)
                val r3 = pseudoRandom(seed + 2)
                val r4 = pseudoRandom(seed + 3)

                // 碎片飞出方向和速度
                val angle = r1 * Math.PI.toFloat() * 2f
                val crackP = ((progress - 0.666f) / 0.334f).coerceIn(0f, 1f)
                val splitDist = crackP * cellW * 0.25f  // 裂缝随进度扩大
                val px = cx + Math.cos(angle.toDouble()).toFloat() * splitDist
                val py = cy + Math.sin(angle.toDouble()).toFloat() * splitDist

                // 碎片大小: 加大，更明显
                val baseSize = cellW * (0.16f + r3 * 0.10f)
                val size = baseSize

                // 旋转
                val rotation = progress * (r4 - 0.5f) * 540f

                // 透明度: 亮段显示，不透明到底
                val fragAlpha = (intensity * 255).toInt().coerceIn(0, 255)
                val fragColor = (fragAlpha shl 24) or (0x00FFD700.toInt() and 0x00FFFFFF)

                if (size > 0.5f && fragAlpha > 20) {
                    canvas.save()
                    canvas.rotate(rotation, px, py)
                    paint.color = fragColor
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(px - size, py - size, px + size, py + size, size * 0.3f, size * 0.3f, paint)
                    // 加一条亮边模拟碎片边缘反光
                    paint.color = (fragAlpha shl 24) or 0x00FFFFFF.toInt()
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 0.6f
                    canvas.drawRoundRect(px - size, py - size, px + size, py + size, size * 0.3f, size * 0.3f, paint)
                    canvas.restore()
                }
            }
        }
    }

    /** 简单的确定性伪随机 */
    private fun pseudoRandom(seed: Int): Float {
        var x = seed
        x = x xor (x shl 13)
        x = x xor (x ushr 17)
        x = x xor (x shl 5)
        return (x.toLong() and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat()
    }
}

package com.lzz.tetris

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var game: TetrisGame
    private lateinit var tetrisView: TetrisView
    private lateinit var nextPieceView: TetrisView
    private lateinit var tvScore: TextView
    private lateinit var btnPause: Button
    private lateinit var btnRestart: Button
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button
    private lateinit var btnRotate: Button
    private lateinit var btnDown: Button

    private val handler = Handler(Looper.getMainLooper())
    private var gameSpeed = 500L  // 毫秒
    private var gameLoopRunning = false

    private var lastFrameTime = System.currentTimeMillis()
    private var wasClearing = false

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (!game.isGameOver) {
                // 消行动画期间: 高帧率驱动动画，暂停游戏 tick
                if (game.clearingRows.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    if (!wasClearing) {
                        lastFrameTime = now  // 刚进入，防跳帧
                        wasClearing = true
                    }
                    val delta = (now - lastFrameTime).coerceAtMost(33L)
                    lastFrameTime = now
                    game.updateClearAnim(delta)
                    updateUI()
                    if (game.clearingRows.isNotEmpty()) {
                        handler.postDelayed(this, 16)  // 60fps
                        return
                    }
                    wasClearing = false
                }

                if (!game.isPaused) {
                    game.moveDown()
                    updateUI()
                }
            }

            if (!game.isGameOver) {
                handler.postDelayed(this, gameSpeed)
            } else {
                gameLoopRunning = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        game = TetrisGame()

        tetrisView = findViewById(R.id.tetris_view)
        tetrisView.mode = TetrisView.MODE_GAME
        tetrisView.game = game

        nextPieceView = findViewById(R.id.next_piece_view)
        nextPieceView.mode = TetrisView.MODE_PREVIEW
        nextPieceView.game = game

        tvScore = findViewById(R.id.tv_score)

        btnPause = findViewById(R.id.btn_pause)
        btnRestart = findViewById(R.id.btn_restart)
        btnLeft = findViewById(R.id.btn_left)
        btnRight = findViewById(R.id.btn_right)
        btnRotate = findViewById(R.id.btn_rotate)
        btnDown = findViewById(R.id.btn_down)

        setupButtons()
        startGameLoop()
        updateUI()
    }

    private fun setupButtons() {
        btnLeft.setOnClickListener {
            game.moveLeft()
            updateUI()
        }
        btnRight.setOnClickListener {
            game.moveRight()
            updateUI()
        }
        btnRotate.setOnClickListener {
            game.rotate()
            updateUI()
        }
        btnDown.setOnClickListener {
            game.moveDown()
            updateUI()
        }
        btnPause.setOnClickListener {
            if (game.isGameOver) return@setOnClickListener
            game.isPaused = !game.isPaused
            if (game.isPaused) {
                btnPause.text = getString(R.string.btn_resume)
                stopGameLoop()
            } else {
                btnPause.text = getString(R.string.btn_pause)
                startGameLoop()
            }
        }
        btnRestart.setOnClickListener {
            game.reset()
            btnPause.text = getString(R.string.btn_pause)
            updateUI()
            startGameLoop()
        }

        // 长按快速下落
        btnDown.setOnLongClickListener {
            game.hardDrop()
            updateUI()
            true
        }
    }

    private fun startGameLoop() {
        if (!gameLoopRunning && !game.isGameOver && !game.isPaused) {
            gameLoopRunning = true
            lastFrameTime = System.currentTimeMillis()
            handler.postDelayed(gameRunnable, gameSpeed)
        }
    }

    private fun stopGameLoop() {
        gameLoopRunning = false
        handler.removeCallbacks(gameRunnable)
    }

    private fun updateUI() {
        tvScore.text = game.score.toString()
        tetrisView.invalidate()
        nextPieceView.invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGameLoop()
    }
}

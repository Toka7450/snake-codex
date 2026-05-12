package com.example.snake

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class Cell(val x: Int, val y: Int)

class SnakeGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val gridSize = 20
    private val gameTickMs = 180L

    private val snakePaint = Paint().apply { color = Color.parseColor("#2E7D32") }
    private val headPaint = Paint().apply { color = Color.parseColor("#1B5E20") }
    private val foodPaint = Paint().apply { color = Color.parseColor("#D32F2F") }
    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val gameOverPaint = Paint().apply {
        color = Color.BLACK
        textSize = 60f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var snake = mutableListOf(Cell(10, 10), Cell(9, 10), Cell(8, 10))
    private var direction = Direction.RIGHT
    private var pendingDirection = Direction.RIGHT
    private var food = Cell(15, 15)
    private var gameOver = false
    private var score = 0

    var onScoreChanged: ((Int) -> Unit)? = null
    var onGameOverChanged: ((Boolean) -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val gameLoop = object : Runnable {
        override fun run() {
            if (!gameOver) {
                step()
                invalidate()
                handler.postDelayed(this, gameTickMs)
            }
        }
    }

    private var touchStartX = 0f
    private var touchStartY = 0f

    init {
        spawnFood()
        startGameLoop()
    }

    fun restartGame() {
        snake = mutableListOf(Cell(10, 10), Cell(9, 10), Cell(8, 10))
        direction = Direction.RIGHT
        pendingDirection = Direction.RIGHT
        score = 0
        gameOver = false
        onGameOverChanged?.invoke(false)
        onScoreChanged?.invoke(score)
        spawnFood()
        handler.removeCallbacks(gameLoop)
        startGameLoop()
        invalidate()
    }

    private fun startGameLoop() {
        handler.postDelayed(gameLoop, gameTickMs)
    }

    private fun step() {
        if (isOpposite(direction, pendingDirection).not()) {
            direction = pendingDirection
        }

        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> Cell(head.x, head.y - 1)
            Direction.DOWN -> Cell(head.x, head.y + 1)
            Direction.LEFT -> Cell(head.x - 1, head.y)
            Direction.RIGHT -> Cell(head.x + 1, head.y)
        }

        if (newHead.x !in 0 until gridSize || newHead.y !in 0 until gridSize || snake.contains(newHead)) {
            gameOver = true
            onGameOverChanged?.invoke(true)
            return
        }

        snake.add(0, newHead)

        if (newHead == food) {
            score += 1
            onScoreChanged?.invoke(score)
            spawnFood()
        } else {
            snake.removeAt(snake.size - 1)
        }
    }

    private fun spawnFood() {
        var candidate: Cell
        do {
            candidate = Cell(Random.nextInt(gridSize), Random.nextInt(gridSize))
        } while (snake.contains(candidate))
        food = candidate
    }

    private fun isOpposite(current: Direction, next: Direction): Boolean {
        return (current == Direction.UP && next == Direction.DOWN) ||
            (current == Direction.DOWN && next == Direction.UP) ||
            (current == Direction.LEFT && next == Direction.RIGHT) ||
            (current == Direction.RIGHT && next == Direction.LEFT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)

        val cellSize = minOf(width, height).toFloat() / gridSize
        val xOffset = (width - cellSize * gridSize) / 2f
        val yOffset = (height - cellSize * gridSize) / 2f

        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val left = xOffset + x * cellSize
                val top = yOffset + y * cellSize
                canvas.drawRect(left, top, left + cellSize, top + cellSize, gridPaint)
            }
        }

        snake.forEachIndexed { index, cell ->
            val left = xOffset + cell.x * cellSize
            val top = yOffset + cell.y * cellSize
            val paint = if (index == 0) headPaint else snakePaint
            canvas.drawRect(left + 2, top + 2, left + cellSize - 2, top + cellSize - 2, paint)
        }

        val foodLeft = xOffset + food.x * cellSize
        val foodTop = yOffset + food.y * cellSize
        canvas.drawCircle(foodLeft + cellSize / 2, foodTop + cellSize / 2, cellSize * 0.35f, foodPaint)

        if (gameOver) {
            canvas.drawText("Game Over", width / 2f, height / 2f, gameOverPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY
                val minSwipeDistance = 24f

                if (abs(dx) < minSwipeDistance && abs(dy) < minSwipeDistance) {
                    return true
                }

                if (abs(dx) > abs(dy)) {
                    pendingDirection = if (dx > 0) Direction.RIGHT else Direction.LEFT
                } else {
                    pendingDirection = if (dy > 0) Direction.DOWN else Direction.UP
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(gameLoop)
    }
}

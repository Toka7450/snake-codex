package com.example.snake

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scoreText = findViewById<TextView>(R.id.scoreText)
        val gameView = findViewById<SnakeGameView>(R.id.gameView)
        val restartButton = findViewById<Button>(R.id.restartButton)

        gameView.onScoreChanged = { score ->
            scoreText.text = "Score: $score"
        }
        gameView.onGameOverChanged = { isGameOver ->
            if (isGameOver) {
                scoreText.text = "${scoreText.text} • Game Over"
            }
        }

        restartButton.setOnClickListener {
            gameView.restartGame()
        }

        gameView.restartGame()
    }
}

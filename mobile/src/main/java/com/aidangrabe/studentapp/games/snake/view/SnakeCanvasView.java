package com.aidangrabe.studentapp.games.snake.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aidangrabe.studentapp.games.snake.Food;
import com.aidangrabe.studentapp.games.snake.Snake;
import com.aidangrabe.studentapp.games.snake.SnakeController;

/**
 * Created by aidan on 13/01/15.
 *
 */
public class SnakeCanvasView extends View {

    private static final int COLOR_FOOD = Color.RED;

    private Rect mSnakeRect;
    private Paint mSnakePaint, mTextPaint;
    private int mWidth, mHeight, mNumPlayers;
    private boolean mSizedAndReady;
    private boolean mGameStarted;
    private SnakeController mGame;

    public SnakeCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSnakeRect = new Rect(0, 0, 16, 16);
        mSnakePaint = new Paint();
        mSnakePaint.setColor(Color.BLACK);
        mSizedAndReady = false;
        mGameStarted = false;
        mNumPlayers = 0;

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(16);
        mTextPaint.setAntiAlias(true);

    }

    public void setGameController(SnakeController controller) {
        mGame = controller;
    }

    public void setGameStarted(boolean gameStarted) {
        mGameStarted = gameStarted;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // clear the canvas
        canvas.drawColor(Color.WHITE);

        if (mGameStarted) {
            // draw the snakes
            for (Snake snake : mGame.getSnakes()) {

                drawPart(canvas, snake.getPosition(), snake.getColor());
                for (Snake.BodyPart part : snake.getBodyParts()) {
                    drawPart(canvas, part.position, snake.getColor());
                }

            }

            drawPart(canvas, mGame.getFood().position, COLOR_FOOD);
        }
        // show waiting for players message
        else {
            canvas.drawText("Waiting for players...", mWidth / 2, mHeight / 2, mTextPaint);
            canvas.drawText(String.format("Num Players: %d", mNumPlayers), mWidth / 2, mHeight / 2 + 30, mTextPaint);
            if (mNumPlayers > 0) {
                canvas.drawText("TOUCH TO START", mWidth / 2, mHeight / 2 + 70, mTextPaint);
            }
        }

    }

    // draw a part of the snake's body at the given Point
    public void drawPart(Canvas canvas, Point point) {

        drawPart(canvas, point, mSnakePaint.getColor());

    }

    // draw a part of the snake's body at the given Point
    public void drawPart(Canvas canvas, Point point, int color) {

        int oldColor = mSnakePaint.getColor();
        mSnakePaint.setColor(color);

        mSnakeRect.offsetTo(point.x, point.y);
        canvas.drawRect(mSnakeRect, mSnakePaint);

        // reset the paint color
        mSnakePaint.setColor(oldColor);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        if (!mSizedAndReady) {
            mSizedAndReady = true;
            mGame.getFood().jumpRandomly(w, h);
        }

        mGame.setSize(mWidth, mHeight);

    }

    public Snake[] getSnakes() {
        return mGame.getSnakes();
    }

    public void setNumPlayers(int numPlayers) {
        mNumPlayers = numPlayers;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public SnakeController getSnakeController() {
        return mGame;
    }

}

package activitytest.example.com.ballgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

/**
 * Created by haha on 2017-08-08.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable, ContactListener {
    private SurfaceHolder sfh;
    private Thread th;
    private boolean flag;
    private Canvas canvas;
    private Paint paint;
    private int screenH, screenW;
    private int x = 0, y = 0;       //触屏手指放下的位置
    private int upx = 0, upy = 0;     //移动后的手势位置

    //------------添加物理世界
    World world;
    Vec2 gravity;
    AABB aabb;

    //屏幕映射与现实比例
    float rate = 30;
    //物理世界模拟频率
    float timeStep = 1f / 60f;
    //迭代值
    final int itetation = 10;

    //声明小球的body， 便于后续对小球进行操作
    private Body bodyBall;

    //声明胜利与失败的body，用于判定游戏的胜负
    private Body lostBody1, lostBody2, winBody;

    // 为了游戏暂停时，失败，胜利能继续可能到游戏中的状态，所以并没有将其写成一个状态
    private boolean gameIsPause, gameIsLost, gameIsWin;


    //定义所有图片资源
    Bitmap bmp_ball, bmp_game_bg, bmp_gamelost, bmp_gamewin, bmp_h, bmp_helpbg, bmp_icon, bmp_lostbody,
            bmp_menu_back, bmp_menu_bg, bmp_menu_exit, bmp_menu_help, bmp_menu_play, bmp_menu_replay,
            bmp_menu_resume, bmp_s, bmp_sh, bmp_smallbg, bmp_ss, bmp_winbody, bmp_menu_menu;

    //创建按钮
    private HButton hbHelp, hbPlay, hbExit, hbResume, hbReplay, hbBack, hbMenu;


    //游戏状态
    //主菜单界面
    private final int game_menu = 0;
    //游戏帮助界面
    private final int game_help = 1;
    //游戏中
    private final int game_ing = 2;
    //默认状态
    private int gameState = game_menu;


    public MySurfaceView(Context context) {
        super(context);
        sfh = this.getHolder();
        sfh.addCallback(this);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        //设置画笔无锯齿
        paint.setAntiAlias(true);
        //设置按键焦点
        setFocusable(true);
        //设置触屏焦点
        setFocusableInTouchMode(true);

        // ---------添加物理世界
        aabb = new AABB();
        gravity = new Vec2(0, 10);
        aabb.lowerBound.set(-100f, -100f);
        aabb.upperBound.set(100f, 100f);
        world = new World(aabb, gravity, true);


        bmp_game_bg = BitmapFactory.decodeResource(getResources(), R.drawable.game_bg);
        bmp_gamelost = BitmapFactory.decodeResource(getResources(), R.drawable.gamelost);
        bmp_gamewin = BitmapFactory.decodeResource(getResources(), R.drawable.gamewin);
        bmp_helpbg = BitmapFactory.decodeResource(getResources(), R.drawable.helpbg);
        bmp_icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        bmp_lostbody = BitmapFactory.decodeResource(getResources(), R.drawable.lostbody);
        bmp_menu_back = BitmapFactory.decodeResource(getResources(), R.drawable.menu_back);
        bmp_menu_bg = BitmapFactory.decodeResource(getResources(), R.drawable.menu_bg);
        bmp_menu_exit = BitmapFactory.decodeResource(getResources(), R.drawable.menu_exit);
        bmp_menu_help = BitmapFactory.decodeResource(getResources(), R.drawable.menu_help);
        bmp_menu_menu = BitmapFactory.decodeResource(getResources(), R.drawable.menu_menu);
        bmp_menu_play = BitmapFactory.decodeResource(getResources(), R.drawable.menu_play);
        bmp_menu_replay = BitmapFactory.decodeResource(getResources(), R.drawable.menu_replay);
        bmp_menu_resume = BitmapFactory.decodeResource(getResources(), R.drawable.menu_resume);
        bmp_smallbg = BitmapFactory.decodeResource(getResources(), R.drawable.smallbg);
        bmp_winbody = BitmapFactory.decodeResource(getResources(), R.drawable.winbody);
        //Body实例按钮
        bmp_s = BitmapFactory.decodeResource(getResources(), R.drawable.s);
        bmp_sh = BitmapFactory.decodeResource(getResources(), R.drawable.sh);
        bmp_ss = BitmapFactory.decodeResource(getResources(), R.drawable.ss);
        bmp_h = BitmapFactory.decodeResource(getResources(), R.drawable.h);
        bmp_ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        screenH = this.getHeight();
        screenW = this.getWidth();
        //防止home键导致游戏重置
        if (gameState == game_menu) {
            //实例化按钮
            hbPlay = new HButton(bmp_menu_play, screenW / 2 - bmp_menu_help.getWidth() / 2, 600);
            hbHelp = new HButton(bmp_menu_help, hbPlay.getX(), hbPlay.getY() + 200);
            hbExit = new HButton(bmp_menu_exit, hbPlay.getX(), hbHelp.getY() + 200);
            hbBack = new HButton(bmp_menu_back, hbPlay.getX(), screenH - 2 * bmp_menu_back.getHeight());
            hbResume = new HButton(bmp_menu_resume, hbPlay.getX(), 500);
            hbReplay = new HButton(bmp_menu_replay, hbPlay.getX(), 700);
            hbMenu = new HButton(bmp_menu_menu, hbPlay.getX(), 900);

            // 创建主角小球
            bodyBall = createCircle(bmp_ball, bmp_h.getHeight(), bmp_h.getHeight(), bmp_ball.getWidth() / 2, 5);
            //创建胜负Body
            lostBody1 = createCircle(bmp_lostbody, screenW - bmp_h.getHeight() - bmp_lostbody.getWidth(), bmp_h.getHeight(), bmp_lostbody.getWidth() / 2, 0);
            lostBody2 = createCircle(bmp_lostbody, bmp_h.getHeight(), screenH - bmp_h.getHeight() - bmp_lostbody.getHeight(), bmp_lostbody.getWidth() / 2, 0);
            winBody = createCircle(bmp_winbody, screenW - bmp_h.getHeight() - bmp_winbody.getWidth(), screenH - bmp_h.getHeight() - bmp_winbody.getHeight(),
                    bmp_winbody.getWidth() / 2, 0);
            //设置传感器，发生碰撞，但无碰撞效果
            lostBody1.getShapeList().m_isSensor = true;
            lostBody2.getShapeList().m_isSensor = true;
            winBody.getShapeList().m_isSensor = true;
            // 创建边界
            createRect(bmp_h, 0, 0, bmp_h.getWidth(), bmp_h.getHeight(), 0);// 上
            createRect(bmp_h, 0, screenH - bmp_h.getHeight(), bmp_h.getWidth(), bmp_sh.getHeight(), 0);// 下
            createRect(bmp_s, 0, 0, bmp_s.getWidth(), bmp_s.getHeight(), 0);// 左
            createRect(bmp_s, screenW - bmp_s.getWidth(), 0, bmp_s.getWidth(), bmp_s.getHeight(), 0);// 右
            // -----创建障碍物
            createRect(bmp_sh, 0, 400, bmp_sh.getWidth(), bmp_sh.getHeight(), 0);
            createRect(bmp_sh, 450, 800, bmp_sh.getWidth(), bmp_sh.getHeight(), 0);
            createRect(bmp_ss, 450, 800, bmp_ss.getWidth(), bmp_ss.getHeight(), 0);
            createRect(bmp_ss, 750, screenH - bmp_ss.getHeight(), bmp_ss.getWidth(), bmp_ss.getHeight(), 0);
            //绑定监听器
            world.setContactListener(this);


        }


        flag = true;
        th = new Thread(this);
        th.start();
    }


    //在物理世界中添加矩形Body
    public Body createRect(Bitmap bmp, float x, float y, float w, float h, float density) {
        PolygonDef pd = new PolygonDef();
        pd.density = density;
        pd.friction = 0.8f;
        pd.restitution = 0.3f;
        pd.setAsBox(w / 2 / rate, h / 2 / rate);
        BodyDef bd = new BodyDef();
        bd.position.set((x + w / 2) / rate, (y + h / 2) / rate);
        Body body = world.createBody(bd);
        body.m_userData = new MyRect(bmp, x, y);
        body.createShape(pd);
        body.setMassFromShapes();
        return body;
    }


    public Body createCircle(Bitmap bmp, float x, float y, float r, float density) {
        CircleDef cd = new CircleDef();
        cd.density = density;
        cd.friction = 0.8f;
        cd.restitution = 0.3f;
        cd.radius = r / rate;
        //创建刚体
        BodyDef bd = new BodyDef();
        bd.position.set((x + r) / rate, (y + r) / rate);
        //创建物体
        Body body = world.createBody(bd);
        body.m_userData = new MyCircle(bmp, x, y, r);
        body.createShape(cd);
        body.setMassFromShapes();
        body.allowSleeping(false);
        return body;
    }


    public void myDraw() {
        try {
            canvas = sfh.lockCanvas();
            canvas.drawColor(Color.BLACK);

            switch (gameState) {
                case game_menu:
                    //绘制主菜单背景
                    canvas.save();
                    canvas.scale((float) 1.15, (float) 1.3, 0, 0);
//                        canvas.scale((float)(screenW / bmp_menu_bg.getWidth()), (float)(screenH / bmp_menu_bg.getHeight()), 0, 0);
                    canvas.drawBitmap(bmp_menu_bg, 0, 0, paint);
                    canvas.restore();
                    //绘制play按钮
                    hbPlay.draw(canvas, paint);
                    //绘制help按钮
                    hbHelp.draw(canvas, paint);
                    //绘制exit按钮
                    hbExit.draw(canvas, paint);
                    break;
                case game_help:
                    canvas.save();
                    canvas.scale((float) 1.15, (float) 1.3, 0, 0);
                    canvas.drawBitmap(bmp_helpbg, 0, 0, paint);
                    canvas.restore();
                    //绘制返回按钮
                    hbBack.draw(canvas, paint);
                    break;
                case game_ing:
                    canvas.save();
                    canvas.scale((float) 1.15, (float) 1.3, 0, 0);
                    canvas.drawBitmap(bmp_game_bg, 0, 0, paint);
                    canvas.restore();
                    //遍历物理世界中所有的body
                    Body body = world.getBodyList();
                    for (int i = 1; i < world.getBodyCount(); i++) {
                        if ((body.m_userData) instanceof MyRect) {           // instanof用来判断前者是否是后者的实例
                            MyRect rect = (MyRect) (body.m_userData);
                            rect.drawRect(canvas, paint);
                        } else if ((body.m_userData) instanceof MyCircle) {
                            MyCircle mcc = (MyCircle) (body.m_userData);
                            mcc.drawArc(canvas, paint);
                        }
                        body = body.m_next;
                    }
                    //当游戏暂停，失败，成功时
                    if (gameIsPause || gameIsLost || gameIsWin) {
                        //// 当游戏暂停或失败或成功时画一个半透明黑色矩形，突出界面
                        Paint paintB = new Paint();
                        paintB.setAlpha(0x77);
                        canvas.drawRect(0, 0, screenW, screenH, paint);
                    }
                    //游戏暂停时
                    if (gameIsPause) {
                        canvas.drawBitmap(bmp_smallbg, screenW / 2 - bmp_smallbg.getWidth() / 2, 300, paint);
                        //绘制resume按钮
                        hbResume.draw(canvas, paint);
                        //绘制replay按钮
                        hbReplay.draw(canvas, paint);
                        //绘制menu按钮
                        hbMenu.draw(canvas, paint);

                        //游戏失败
                    } else if (gameIsLost) {
                        canvas.drawBitmap(bmp_gamelost, screenW / 2 - bmp_smallbg.getWidth() / 2, 300, paint);
                        //绘制replay按钮
                        hbReplay.draw(canvas, paint);
                        //绘制menu按钮
                        hbMenu.draw(canvas, paint);

                        //游戏胜利
                    } else if (gameIsWin) {
                        canvas.drawBitmap(bmp_gamewin, screenW / 2 - bmp_smallbg.getWidth() / 2, 300, paint);
                        //绘制replay按钮
                        hbReplay.draw(canvas, paint);
                        //绘制menu按钮
                        hbMenu.draw(canvas, paint);
                    }


                    break;
            }


        } catch (Exception e) {

        } finally {
            if (canvas != null)
                sfh.unlockCanvasAndPost(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (gameState) {
            case game_menu:
                //判断play按钮是否被点击
                if (hbPlay.isPressed(event))
                    gameState = game_ing;
                //判断help按钮是否被点击
                if (hbHelp.isPressed(event))
                    gameState = game_help;
                //判断exit按钮是否被点击
                if (hbExit.isPressed(event))
                    MainActivity.exit();

                break;
            case game_ing:
                if (gameIsPause || gameIsWin || gameIsLost) {
                    if (hbResume.isPressed(event)) {
                        gameIsPause = false;
                    } else if (hbReplay.isPressed(event)) {
                        //因为在重置前小球可能拥有力，所以重置游戏要先使用putToSleep()方法
                        //让其Body进入睡眠，并让Body停止模拟，速度置为0
                        bodyBall.putToSleep();
                        //然后对小球的坐标进行重置
                        bodyBall.setXForm(new Vec2((bmp_h.getHeight() + bmp_ball.getWidth() / 2 + 2) / rate, (bmp_h.getHeight() + bmp_ball.getWidth() / 2 + 2) / rate), 0);
                        //设置默认重力方向向下
                        world.setGravity(new Vec2(0, 10));
                        //唤醒小球
                        bodyBall.wakeUp();
                        //游戏暂停。胜利， 失败条件还原默认false
                        gameIsPause = false;
                        gameIsLost = false;
                        gameIsWin = false;

                    } else if (hbMenu.isPressed(event)) {
                        bodyBall.putToSleep();
                        //小球坐标重置
                        bodyBall.setXForm(new Vec2((bmp_h.getHeight() + bmp_ball.getWidth() / 2 + 2) / rate,
                                (bmp_h.getHeight() + bmp_ball.getWidth() / 2 + 2) / rate), 0);
                        //设置默认重力方向向下
                        world.setGravity(new Vec2(0, 10));
                        //唤醒小球
                        bodyBall.wakeUp();
                        //重置游戏状态为主菜单
                        gameState = game_menu;
                        //游戏暂停。胜利， 失败条件还原默认false
                        gameIsPause = false;
                        gameIsLost = false;
                        gameIsWin = false;
                    }
                }

                //判断用户手势上下左右方向
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        //手指按下的时候：初始化 x,y 值
                        x = (int) event.getX();
                        y = (int) event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        break;
                    case MotionEvent.ACTION_UP:
                /*
                 * 手指抬起来触发 ，所以判断在这里进行
                 * 1.获得结束的x,y
                 * 2.进行判断
                 */
                        upx = (int) event.getX();
                        upy = (int) event.getY();

                        break;
                }
                if (!gameIsPause && !gameIsLost && !gameIsWin) {

                    if (upx - x > 50) {
                        world.setGravity(new Vec2(10, 2));
                    }
                    if (x - upx > 50) {
                        world.setGravity(new Vec2(-10, 2));
                    }
                    if (upy - y > 50) {
                        world.setGravity(new Vec2(2, 10));
                    }
                    if (y - upy > 50) {
                        world.setGravity(new Vec2(2, -10));
                    }
                }
                break;
            case game_help:
                //判断返回按钮是否被点击
                if (hbBack.isPressed(event))
                    gameState = game_menu;
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当前游戏状态不处于正在游戏中时，屏蔽“返回”实体按键,避免程序进入后台;
        if (keyCode == KeyEvent.KEYCODE_BACK && gameState != game_ing) {
            return true;
        }
        switch (gameState) {
            case game_menu:
                break;
            case game_help:
                break;
            case game_ing:
                // 游戏没有暂停、失败、胜利
                if (!gameIsPause && !gameIsLost && !gameIsWin) {
                    //如果方向键左键被按下
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                        //设置物理世界的重力方向向左
                        world.setGravity(new Vec2(-10, 2));
                        //如果方向键右键被按下
                    else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                        //设置物理世界的重力方向向右
                        world.setGravity(new Vec2(10, 2));
                        //如果方向键上键被按下
                    else if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
                        //设置物理世界的重力方向向上
                        world.setGravity(new Vec2(0, -10));
                        //如果方向键下键被按下
                    else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                        //设置物理世界的重力方向向下
                        world.setGravity(new Vec2(0, 10));
                        //如果返回键被按下
                    else if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //进入游戏暂停界面
                        gameIsPause = true;
                    }
                }
                //屏蔽“返回”实体按键,避免程序进入后台;
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void logic() {
        //开始模拟物理世界
        switch (gameState) {
            case game_menu:
                break;
            case game_help:
                break;
            case game_ing:
                if (!gameIsWin && !gameIsLost && !gameIsPause) {
                    //开始模拟世界
                    world.step(timeStep, itetation);
                    Body body = world.getBodyList();
                    for (int i = 1; i < world.getBodyCount(); i++) {
                        if ((body.m_userData) instanceof MyRect) {
                            MyRect rect = (MyRect) (body.m_userData);
                            rect.setX(body.getPosition().x * rate - rect.getW() / 2);
                            rect.setY(body.getPosition().y * rate - rect.getH() / 2);
                        } else if ((body.m_userData) instanceof MyCircle) {
                            MyCircle mcc = (MyCircle) body.m_userData;
                            mcc.setX(body.getPosition().x * rate - mcc.getR());
                            mcc.setY(body.getPosition().y * rate - mcc.getR());
                            mcc.setAngle((float) (body.getAngle() * 180 / Math.PI));
                        }
                        body = body.m_next;
                    }

                }


                break;

        }


    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        flag = false;
    }

    @Override
    public void run() {
        while (flag) {
            myDraw();
            logic();
            try {
                th.sleep((long) (timeStep * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void add(ContactPoint point) {
        //当前游戏状态为进行游戏时
        if (gameState == game_ing) {
            //游戏没有进入暂停、失败、胜利界面
            if (!gameIsPause && !gameIsLost && !gameIsWin) {
                //判定主角小球是否与失败小球1发生碰撞
                if (point.shape1.getBody() == bodyBall && point.shape2.getBody() == lostBody2) {
                    //游戏失败
                    gameIsLost = true;
                    //判定主角小球是否与失败小球2发生碰撞
                } else if (point.shape1.getBody() == bodyBall && point.shape2.getBody() == lostBody1) {
                    //游戏失败
                    gameIsLost = true;
                    //判定主角小球是否与胜利小球发生碰撞
                } else if (point.shape1.getBody() == bodyBall && point.shape2.getBody() == winBody) {
                    //游戏胜利
                    gameIsWin = true;
                }
            }
        }
    }


    @Override
    public void persist(ContactPoint contactPoint) {

    }

    @Override
    public void remove(ContactPoint contactPoint) {

    }

    @Override
    public void result(ContactResult contactResult) {

    }
}

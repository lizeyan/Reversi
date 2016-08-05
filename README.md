---
title: Online Reversi Game powered by Java
date: 2016-08-04 22:09:23
tags: ["java", "ai", "game", "swing"]
---

使用Java SE制作的网络[黑白棋][Reversi]小游戏。同时提供接口，支持自定义AI进行人机对战或者AI对战。
Git Repo地址: [https://github.com/lizeyan/Reversi][Git Repo]

<!-- more -->

# 游戏功能介绍
游戏主界面如图示:
![main](/images/reversi/main.PNG)
窗口上方是菜单栏，其中集成了整个游戏的所有操作:
- Local 开始本地的双人对战或者人机对战，加载本地棋局
- Online 建立主机或者连接主机，断开连接，开始网络双人对战
- Operate 进行悔棋，认输，求和，托管等游戏操作
- General 打开设置，保存当前的棋局，显示程序信息(About)
- 所有的菜单操作都有对应的全局快捷键。

窗体左侧是棋盘，用来显示棋局和接受本地玩家的操作。
窗体右侧是信息栏：
- 信息栏上方分别显示两个玩家的执子，棋子数和名字。游戏未开始则只显示自己的名字
- 中间显示当前一步剩余的时间。游戏未开始则显示规定的每步时限。本地对战时，时限在`General`->`Setting`中设置。当联网对战时，使用主机规定的时限。
- 下方是消息栏，显示游戏通知和聊天信息。支持html格式文本。
- 最下方是聊天信息编辑窗口，任何时候你所发送的消息都将会显示在本地的消息栏中。网络已连接时会发送给其他玩家。消息栏支持html格式文本，你可以使用html格式化你的文本，以显示丰富的信息。你甚至可以发送来自web的图片(不支持音乐和视频等html5标签)：
eg:`<img src="http://img0.bdstatic.com/img/image/shouye/xiaoxiao/%E5%94%AF%E7%BE%8E%E6%91%84%E5%BD%B183.jpg"/>`
![sendImg](/images/reversi/sendImg.PNG)

棋盘和信息栏的大小会自动随着窗口大小的改变而变化。

1. 本地游戏
    - 点击`Local`->`Start`会开始一局本地游戏，你可以选择人机对战或者双人对战。
人机对战你可以选择执黑子或者执白子。双人对战的落子由电脑前的两个玩家自行商议。
    - 点击`Local`->`Load`可以开始加载本地棋局，所有的棋局均保存为名为`*.rc`的文本文件。选择棋局之后你同样可以选择是人机对战还是双人对战，不同的是如果棋局本身是人机对战或者网络对战留下的棋局，你只能执当时所执子而不能选择。
每一局游戏必须结束，或者选择认输或者求和，否则不能开始新的游戏或者进行网络对战。
2. 网络对战
    - 点击`Online`->`Connect`可以建立网络连接。
        - 若选择`Server`模式，你可以指定每步的时限。你需要输入一个[端口号][port]，然后程序会等待其他玩家加入。程序在等待期间不能进行其他操作，10秒后如无人加入则取消连接。
        - 若选择`Client`模式，则你必须接受主机的设置。你需要输入主机的主机名或者[IP地址][ip address]以及[端口号][port]。
    
    任何一方，连接之后不能再打开本地游戏（即使未开始网络对战）。

    - 点击`Online`->`Disconnect`可以断开连接。任何时候，如果你对对手的设置或者棋力不满意都可以断开连接。但是如果是在一局游戏进行中的话，断开连接视同认输（包括通过关闭游戏，关闭计算机等方式断开连接）。

    - 点击`Online`->'Start'可以向对方发送开始在线游戏的请求，对方同意之后就可以开始新的一局游戏了。
    同样地，一局在线游戏必须结束之后才可以开始新的在线游戏。
3. 操作
所有操作都必须在一局游戏进行中，且当前由你执子时才能进行。
    - 点击`Operate`->`Undo`可以进行悔棋操作。本地双人对战至多悔2次。人机对战取决于AI的实现，默认AI限定一局悔2次。网络对战需要对方同意方可，但没有次数限制。跳过的落子视为空的落子，也当作一步处理。

    - 点击`Operate`->`Give In`进行认输操作。本地双人对战和默认AI默认同意，自定义AI可以自行设置是否接受。网络对战需要对手接受请求。

    - 点击`Operate`->`Sue For Peace`进行求和操作。本地双人对战和默认AI默认同意，自定义AI可以自行设置是否接受。网络对战需要对手接受请求。

    - 点击`Operate`->`Detach`进行托管操作。选择托管之后你的手动落子都将无效，而只能由当前的AI决定实际的落子。再次点击可以取消托管。

4. 通用
    - 点击`General`->`Setting`打开设置窗口。最上方可以设置名字和时限，在一局游戏中或者网络已连接时禁止修改名字和时限。下面可以设置背景音乐和背景图片。设置空的背景音乐即可停止播放音乐。最下方可以加载新的AI文件，AI文件为编译好的Java Class文件(`*.class`),该类必须继承LocalMachinePlayer，否则设置无效。在一局游戏中禁止更换AI。

    - 点击`General`->`Save`可以保存当前棋局。无论是双人对战，人机对战还是网络对战都可以保存。但之后加载只能作为本地棋局而继续。保存时注意必须存为`*.rc`。

    - 点击`General`->`About`显示程序相关信息。
5. AI的编写
你需要继承LocalMachinePlayer，然后将你自己的类编译为`*.class`文件然后再到游戏中加载。使用网络双人对战，然后双方均托管即可实现AI对战。
如何实现可以参考`AlphaBetaAI`，这是一个简单的α-β搜索实现的AI。

# 程序实现

- 主要模块
![modules](/images/reversi/modules.png)
    1. Reversi是控制类和主窗体，继承JFrame。主要功能是控制整个游戏的流程。唯一有权修改Composition的类（参见[security key][security key])。
    2. Composition是棋局数据类。主要功能是存储棋局，计算棋局的胜负，可落子点之类。
    3. Chessboard继承JPanel。一方面根据Composition的信息来绘制棋盘和棋子。另一方面在激活的状态下可以接受用户的点击并且保存落子结果（是否激活由其他类控制）。
    4. Noticeboard继承JPanel，就是接受信息然后显示而已。再联网的情况下发送的信息会通过Proxy想对手传递。
    5. Proxy，建立一个连接之后会新建一个Proxy用来处理关于网络的所有操作，其他模块只需要调用Proxy的类的接口。对于发送，Proxy类会接受传入的数据并且向对方发送。对于接受，当Proxy新建的时候就会新建一个线程不停地轮询(while死循环:)新的信息，然后解析并存储在缓冲区中。当其他类调用查询接口的时候如果缓冲区有数据就可以直接取走，否则就需要等待。
    6. Player，抽象玩家的操作。这样任何一局游戏的流程都可以抽象为轮流询问两个Player的决策结果。任何关于悔棋，认输，求和的操作都可以抽象为询问Player的意见然后处理。那么双人对战，人机对战，网络对战的不同就是Player的各个接口实现不同而已。
    ![player](/images/reversi/player.png)
    7. SettingDialog 修改各种设置。单击其中的ok按钮会修改Reversi中的某些数据以修改设置。Reversi可以控制其中的某些设置不可用来避免发生错误。

- Composition
使用二维数组存储棋局信息，使用`ArrayList<Point>`记录棋局的历史。其中的`set(point:Point):void`方法可以接受一个`Point`，先判断其合不合法，如果合法则在对应位置落子，然后修改历史，修改可落子点信息，修改上一步落子方，进行胜负判断。
每次更新可落子点信息，都遍历棋盘的所有点。如果一个点是当前执子方的棋子，就从这个点开始，往周围八个方向搜索。每个方向上连续的敌人棋子（至少一个）之后的第一个空白标记为可落子。
胜负判断时，查看黑白双方是否都不能落子（通过上述算法）。如果双方都不能落子，则游戏结束，通过计数黑白棋子数目得到游戏结果。
悔棋时，首先在历史信息中从尾部取出相应步数的元素，然后清除棋盘。然后根据历史信息从头开始逐个重新调用`set(point:Point):void`，得到悔棋后的棋局。

- 游戏流程
本地双人对战时新建两个`LocalMePlayer`
本地人机对战时新建一个`LocalMePlayer`，一个`LocalMachinePlayer`
网络连接时新建`OnlineMePlayer`和`OnlineEnemyPlayer`
游戏开始之后，轮流调用两个`Player`。
新建线程执行`Player::makingPolicy(long)`，然后等待线程相应的时限。如果没有在时限内返回结果则关闭该线程，AI落子。否则接受`Player`给出的结果并落子。如果没有可落子点则跳过。如果`Player`给出非法的结果则AI落子。然后判断胜负，如果游戏结束则停止循环，显示游戏结束信息。
```
while (true):
    idx = (idx + 1) % 2
    th = thread (() -> {policy = players[idx].makingPolicy(time)})
    th.join (time)
    policy = machinePolicy()
    composition.set (policy)
    if (composition.finished()):
        terminate();
```

- LocalMePlayer
`makingPolicy`会轮询`chessBoard`是否有合法的鼠标释放事件。
```
while (chessboard.policy == null):
    sleep ();
Point ret = chessboard.policy;
chessboard.policy = null;
return ret;
```
其他的请求均是调用Reversi的相应方法，弹出对话框有用户选择。

- LocalMachinePlayer
仅依赖棋局`Composition`来计算决策和对请求的回应，默认的是取左上的可落子点，对请求均接受。但是`undo`超过两次则拒绝。虽然`LocalMachinePlayer`允许任何人开放地继承并且加载新AI，但是任何aI的作者都没有办法直接修改传入的`Composition`，这就保证了安全性。（参见[security key][security key]）

- OnlineMePlayer
继承`LocalMePlayer`，唯一的不同在于做出决策的时候会通过`Proxy`向对手发送一份。因为两边采用的是相同的默认AI处理超时和非法的情况，所以尽管超时和非法的时候两端都是在本地计算新的落子而不能通讯，但可以保持棋局一致。

- OnlineEnemyPlayer
所有的接口都实现为向Proxy询问结果。如果远端做出决策或者对请求有回应，那么Proxy自然会收到并返回结果。
```
Point makingPolicy (long tc)
{
    return proxy.waitForPolicy ();
}
Point waitForPolicy ()
{
    while (policyBuffer.empty):
        sleep ()
    Point ret = policyBuffer.get();
    policyBuffer.clear ();
    return ret;
}
```

- 网络连接，断开
网络连接时，弹出对话框由用户选择Server或者Client；
然后输入[Ip][ip address]和[Port][port]进行连接。
连接之后需要新建`Player`和`Proxy`，然后将`Proxy`传入`Player`中。`Proxy`需要连接对方的`Socket`和本地的`Player`才能工作。
断开连接时，如果是用户主动点击connect，则向对方发送disconnect信息，然后将`Proxy`和`Player`清空，恢复到未连接状态。如果是接收到close信息或者在轮询socket时发现连接关闭，除了不向对方发送之外，同上处理。


- 支持HTML的聊天窗口
使用JEditorPane，设置ContentType为"text/html"即可。

- 动态调整界面布局
每个部件的尺寸都不是固定的值，而依赖于父部件的尺寸。
在`Reversi`类中添加`ComponentListener`，每当监听到尺寸变化就调整子部件的大小，然后让子部件自己在再重新调整。
``` java
int w = getContentPane ().getWidth () * 19 / 20, h = getContentPane ().getHeight () * 19 / 20;
chessBoard.pack ();
chessBoard.setBounds (0, 0, chessBoard.getWidth (), chessBoard.getHeight ());
noticeBoard.setBounds (chessBoard.getWidth (), 0, w - chessBoard.getWidth (), h);
noticeBoard.setSize (w - chessBoard.getWidth (), h);
noticeBoard.pack ();
```

- 悔棋，求和，认输
用户主动发出请求，则询问`Player`。如果Player返回同意，则在本地进行相应处理。否则提示请求被拒绝。
如果是接收到请求，那么本地`OnlineMePlayer`会调用`Reversi`中的接口，弹出对话框询问用户。得到结果之后再发回对方。
这里需要处理的问题就是用户可能在超时之后才作出回应，要忽略这种回应。

- 托管
托管只需要处理LocalMePlayer即可。在每次的makingPolicy函数开始之后，除了等待Chessboard的结果，还会同时新建一个AI实例，并行地计算结果。如果用户开启了托管，那么轮询的时候就只询问AI的结果；否则只询问Chessboard的结果。

- 动态加载新的AI
    Java本身提供了支持动态类型的API。可以使用`Class`类存储当前的AI类，然后在需要新建`Player`时从`Class`中获取构造函数:
    ``` java
    private Class aiClass;
    player[1] = (LocalMachinePlayer) aiClass.getConstructor (Composition.class).newInstance (composition);
    ```
    设置新AI是需要先用ClassLoader加载类，然后再查找对应Class:
    ``` java
    File aiFile = new File (aiEdit.getText ());
    URLClassLoader loader = new URLClassLoader (new URL[]{aiFile.toURI ().toURL ()});
    String name = aiFile.getName ();
    game.setAiClass (loader.loadClass (name.substring (0, name.length () - 6)));     
    ```

- 播放背景音乐
使用Java SE8的API有一个局限就是只能播放wav格式的音乐。
``` java
AudioInputStream audioInputStream = AudioSystem.getAudioInputStream (new File (musicEdit.getText ()));
Clip clip AudioSystem.getClip();
clip.open (audioInputStream);
clip.loop (Clip.LOOP_CONTINUOUSLY);
```

[Git Repo]: https://github.com/lizeyan/Reversi "Reversi Git Repo"
[Reversi]: https://en.wikipedia.org/wiki/Reversi "Reversi"
[port]: https://en.wikipedia.org/wiki/Port_(computer_networking) "port"
[ip address]: https://en.wikipedia.org/wiki/IP_address "ip address"
[security key]: https://lizeyan.github.io/2016/07/26/How-to-simulate-keyword-friend-like-in-c-in-JAVA/ "security key"
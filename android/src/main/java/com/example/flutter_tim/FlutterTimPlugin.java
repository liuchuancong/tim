package com.example.flutter_tim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConnListener;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMFaceElem;
import com.tencent.imsdk.TIMFriendshipManager;
import com.tencent.imsdk.TIMGroupEventListener;
import com.tencent.imsdk.TIMGroupTipsElem;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMRefreshListener;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMSoundElem;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.imsdk.TIMUserStatusListener;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.message.TIMConversationExt;
import com.tencent.imsdk.ext.message.TIMManagerExt;
import com.tencent.imsdk.ext.message.TIMUserConfigMsgExt;
import com.tencent.imsdk.friendship.TIMDelFriendType;
import com.tencent.imsdk.friendship.TIMFriend;
import com.tencent.imsdk.friendship.TIMFriendRequest;
import com.tencent.imsdk.friendship.TIMFriendResult;

import android.os.Environment;
import android.os.Bundle;
import android.util.Log;

import java.util.Map;
import java.util.HashMap;

import static com.tencent.imsdk.TIMLogLevel.DEBUG;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FutterTimPlugin
 */
public class FlutterTimPlugin implements MethodCallHandler,StreamHandler  {
  /**
   * Plugin registration.
   */
  private static final String CHANNEL = "futter_tim";
  private static final String tag = "FlutterActivity";
  private Registrar registrar;
  private EventChannel.EventSink eventSink;

  public FlutterTimPlugin(Registrar registrar) {
    this.registrar = registrar;
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL);
    final FlutterTimPlugin flutterTimPlugin =
            new FlutterTimPlugin(registrar);
    final EventChannel eventChannel =
            new EventChannel(registrar.messenger(), "futter_tim:message");
    channel.setMethodCallHandler(flutterTimPlugin);
    eventChannel.setStreamHandler(flutterTimPlugin);
  }
  @Override
  public void onListen(Object arguments, EventSink events) {
    TIMManager.getInstance().addMessageListener(new TIMMessageListener() {
      @Override
      public boolean onNewMessages(List<TIMMessage> list) {
        eventSink.success(list);
        return false;
      }
    });
  }
  @Override
  public void onCancel(java.lang.Object o) {
    TIMManager.getInstance().removeMessageListener(new TIMMessageListener() {
      @Override
      public boolean onNewMessages(List<TIMMessage> list) {
        return false;
      }
    });

  }
  @Override
  public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
    if (methodCall.method.equals("initTim")) {
      initTim(methodCall);
    } else if (methodCall.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (methodCall.method.equals("login")) {
      login(methodCall, result);
    } else if (methodCall.method.equals("signOut")) {
      signOut(methodCall, result);
    } else if (methodCall.method.equals("getConversations")) {
      getConversations(methodCall, result);
    } else if (methodCall.method.equals("delConversation")) {
      delConversation(methodCall, result);
    } else if (methodCall.method.equals("getMessages")) {
      getMessages(methodCall, result);
    } else if (methodCall.method.equals("sendTextMessages")) {
      sendTextMessages(methodCall, result);
    } else if (methodCall.method.equals("sendImageMessages")) {
      sendImageMessages(methodCall, result);
    } else if (methodCall.method.equals("sendSoundMessages")) {
      sendSoundMessages(methodCall, result);
    } else if (methodCall.method.equals("sendFaceMessages")) {
      sendFaceMessages(methodCall, result);
    } else if (methodCall.method.equals("getFriendList")) {
      getFriendList(methodCall, result);
    } else if (methodCall.method.equals("addFriend")) {
      addFriend(methodCall, result);
    } else if (methodCall.method.equals("deleteFriend")) {
      deleteFriend(methodCall, result);
    } else {
      result.notImplemented();
    }

  }


  private void initTim(MethodCall call) {
    HashMap<String, Object> map = call.arguments();

    int IM_SDK_APP_ID = (int) map.get("app_id");
    /**
     * 初始化腾讯云IM
     *
     * @param context
     * @param config
     */
    TIMSdkConfig config = new TIMSdkConfig(IM_SDK_APP_ID).enableCrashReport(false)
            .enableLogPrint(true)
            .setLogLevel(DEBUG)
            .setAccoutType("792")
            .setLogPath(Environment.getExternalStorageDirectory().getPath() + "/justfortest/");
    TIMManager.getInstance().init(registrar.context(), config);
    setUserConfig();
    Log.d(tag, "initTim: success");
  }

  public void setUserConfig() {
    //基本用户配置
    //基本用户配置
    TIMUserConfig userConfig = new TIMUserConfig();
    userConfig.setUserStatusListener(new TIMUserStatusListener() {
      @Override
      public void onForceOffline() {
        //被其他终端踢下线
        Log.i(tag, "onForceOffline");
      }

      @Override
      public void onUserSigExpired() {
        //用户签名过期了，需要刷新 userSig 重新登录 SDK
        Log.i(tag, "onUserSigExpired");
      }
    });
    userConfig.setConnectionListener(new TIMConnListener() {
      @Override
      public void onConnected() {
        Log.i(tag, "onConnected");
      }

      @Override
      public void onDisconnected(int code, String desc) {
        Log.i(tag, "onDisconnected");
      }

      @Override
      public void onWifiNeedAuth(String name) {
        Log.i(tag, "onWifiNeedAuth");
      }
    });
    userConfig.setGroupEventListener(new TIMGroupEventListener() {
      @Override
      public void onGroupTipsEvent(TIMGroupTipsElem elem) {
        Log.i(tag, "onGroupTipsEvent, type: " + elem.getTipsType());
      }
    });
    userConfig.setRefreshListener(new TIMRefreshListener() {
      @Override
      public void onRefresh() {
        Log.i(tag, "onRefresh");
      }

      @Override
      public void onRefreshConversation(List<TIMConversation> conversations) {
        Log.i(tag, "onRefreshConversation, conversation size: " + conversations.size());
      }
    });
    //设置用户状态变更事件监听器
    //设置连接状态事件监听器
    //设置群组事件监听器
    //设置会话刷新监听器

    //消息扩展用户配置
    userConfig = new TIMUserConfigMsgExt(userConfig)
            //禁用消息存储
            .enableStorage(false)
            //开启消息已读回执
            .enableReadReceipt(true);
    //设置消息监听器，收到新消息时，通过此监听器回调

    //将用户配置与通讯管理器进行绑定
    TIMManager.getInstance().setUserConfig(userConfig);
    TIMManager.getInstance().addMessageListener(new TIMMessageListener() {
      @Override
      public boolean onNewMessages(List<TIMMessage> list) {
        if (list != null && list.size() > 0) {
          eventSink.success(new Gson().toJson(list, new TypeToken<Collection<TIMMessage>>() {
          }.getType()));
        } else {
          eventSink.success("[]");
        }
        return false;
      }
    });

  }

  private void login(MethodCall call, final MethodChannel.Result result) {
    HashMap<String, Object> map = call.arguments();
    String identifier = (String) map.get("username");
    String userSig = (String) map.get("userSig");
    final Map requset = new HashMap();
    // identifier为用户名，userSig 为用户登录凭证
    TIMManager.getInstance().login(identifier, userSig, new TIMCallBack() {
      @Override
      public void onError(int code, String desc) {
        //错误码 code 和错误描述 desc，可用于定位请求失败原因
        //错误码 code 列表请参见错误码表
        Log.d(tag, "login failed. code: " + code + " errmsg: " + desc);
        requset.put("code", code);
        requset.put("desc", desc);

      }

      @Override
      public void onSuccess() {
        requset.put("code", "200");
        requset.put("desc", "登陆成功");
        Log.d(tag, "login succ");
      }
    });
    result.success(requset);

  }

  //        登出
  private void signOut(MethodCall call, MethodChannel.Result result) {
    final Map requset = new HashMap();
    TIMManager.getInstance().logout(new TIMCallBack() {
      @Override
      public void onError(int code, String desc) {

        //错误码 code 和错误描述 desc，可用于定位请求失败原因
        //错误码 code 列表请参见错误码表
        Log.d(tag, "login failed. code: " + code + " errmsg: " + desc);
        requset.put("code", code);
        requset.put("desc", desc);
      }

      @Override
      public void onSuccess() {

        requset.put("code", "200");
        requset.put("desc", "退出成功");

        Log.d(tag, "logou succ");
      }
    });
    result.success(requset);
  }
// 获取会话实例

  private void getConversations(MethodCall call, MethodChannel.Result result) {
    List<TIMConversation> list = TIMManagerExt.getInstance().getConversationList();
    if (list != null && list.size() > 0) {
      result.success(new Gson().toJson(list, new TypeToken<Collection<TIMConversation>>() {
      }.getType()));
    } else {
      result.success("[]");
    }
  }

  private void delConversation(MethodCall call, MethodChannel.Result result) {
    String identifier = call.argument("identifier");
    TIMManagerExt.getInstance().deleteConversation(TIMConversationType.C2C, identifier);
    result.success("delConversation success");
  }

  private void getMessages(MethodCall call, final MethodChannel.Result result) {
    HashMap<String, Object> map = call.arguments();
    String identifier = (String) map.get("identifier");
//        TIMMessage lastMsg = (TIMMessage) map.get("lastMsg");
    Log.d(tag, "identifier: " + identifier);
//        获取会话扩展实
    TIMConversation con = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
    TIMConversationExt conExt = new TIMConversationExt(con);

    //获取此会话的消息
    conExt.getMessage(10, //获取此会话最近的 10 条消息
            null, //不指定从哪条消息开始获取 - 等同于从最新的消息开始往前
            new TIMValueCallBack<List<TIMMessage>>() {//回调接口
              @Override
              public void onError(int code, String desc) {//获取消息失败
                //接口返回了错误码 code 和错误描述 desc，可用于定位请求失败原因
                //错误码 code 含义请参见错误码表
                Log.d(tag, "get message failed. code: " + code + " errmsg: " + desc);
              }

              @Override
              public void onSuccess(List<TIMMessage> msgs) {//获取消息成功
                //遍历取得的消
                if (msgs != null && msgs.size() > 0) {
                  result.success(new Gson().toJson(msgs, new TypeToken<Collection<TIMMessage>>() {
                  }.getType()));
                } else {
                  result.success("[]");
                }
              }
            });
  }

  private void sendTextMessages(MethodCall call, final MethodChannel.Result result) {
    String identifier = call.argument("identifier");
    String content = call.argument("content");
    TIMMessage msg = new TIMMessage();
    //添加文本内容
    TIMTextElem elem = new TIMTextElem();
    elem.setText(content);

    //将elem添加到消息
    if (msg.addElement(elem) != 0) {
      Log.d(tag, "addElement failed");
      return;
    }
    TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
    //发送消息
    conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
      @Override
      public void onError(int code, String desc) {//发送消息失败
        //错误码 code 和错误描述 desc，可用于定位请求失败原因
        //错误码 code 含义请参见错误码表
        Log.d(tag, "send message failed. code: " + code + " errmsg: " + desc);
        result.error("send message failed. code: ", desc, code);
      }

      @Override
      public void onSuccess(TIMMessage msg) {//发送消息成功
        Log.e(tag, "SendMsg ok");
        result.success(msg);
      }
    });
  }

  private void sendImageMessages(MethodCall call, final MethodChannel.Result result) {
    String identifier = call.argument("identifier");
    String iamgePath = call.argument("image_path");
    //构造一条消息
    TIMMessage msg = new TIMMessage();

    //添加图片
    TIMImageElem elem = new TIMImageElem();
    //elem.setPath(Environment.getExternalStorageDirectory() + "/DCIM/Camera/1.jpg");
    elem.setPath(iamgePath);
    //将 elem 添加到消息
    if (msg.addElement(elem) != 0) {
      Log.d(tag, "addElement failed");
      return;
    }
    TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
    //发送消息
    conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
      @Override
      public void onError(int code, String desc) {//发送消息失败
        //错误码 code 和错误描述 desc，可用于定位请求失败原因
        //错误码 code 列表请参见错误码表
        Log.d(tag, "send message failed. code: " + code + " errmsg: " + desc);
        result.error("send message failed. code: ", desc, code);
      }

      @Override
      public void onSuccess(TIMMessage msg) {//发送消息成功
        Log.e(tag, "SendMsg ok");
        result.success("SendMsg ok");
      }
    });
  }

  private void sendSoundMessages(MethodCall call, final MethodChannel.Result result) {
    String identifier = call.argument("identifier");
    String soundPath = call.argument("soundPath");
    //构造一条消息
    TIMMessage msg = new TIMMessage();

    //添加语音
    TIMSoundElem elem = new TIMSoundElem();
    elem.setPath(soundPath); //填写语音文件路径
    elem.setDuration(20);  //填写语音时长写语音时长
    //将 elem 添加到消息
    if (msg.addElement(elem) != 0) {
      Log.d(tag, "addElement failed");
      return;
    }
    TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
    //发送消息
    conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
      @Override
      public void onError(int code, String desc) {//发送消息失败
        //错误码 code 和错误描述 desc，可用于定位请求失败原因
        //错误码 code 列表请参见错误码表
        Log.d(tag, "send message failed. code: " + code + " errmsg: " + desc);
        result.error("send message failed. code: ", desc, code);
      }

      @Override
      public void onSuccess(TIMMessage msg) {//发送消息成功
        Log.e(tag, "SendMsg ok");
        result.success("SendMsg ok");
      }
    });
  }

  private void sendFaceMessages(MethodCall call, final MethodChannel.Result result) {
    String identifier = call.argument("identifier");
    byte[] sampleByteArray = call.argument("faceArr");
    //构造一条消息
    TIMMessage msg = new TIMMessage();

    //添加表情
    TIMFaceElem elem = new TIMFaceElem();
    elem.setData(sampleByteArray); //自定义 byte[]
    elem.setIndex(10);   //自定义表情索引
    //将 elem 添加到消息
    if (msg.addElement(elem) != 0) {
      Log.d(tag, "addElement failed");
      return;
    }

    TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
    //发送消息
    conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
      @Override
      public void onError(int code, String desc) {//发送消息失败
        //错误码 code 和错误描述 desc，可用于定位请求失败原因
        //错误码 code 列表请参见错误码表
        Log.d(tag, "send message failed. code: " + code + " errmsg: " + desc);
        result.error("send message failed. code: ", desc, code);
      }

      @Override
      public void onSuccess(TIMMessage msg) {//发送消息成功
        Log.e(tag, "SendMsg ok");
        result.success("SendMsg ok");
      }
    });
  }

  private void getFriendList(MethodCall call, final MethodChannel.Result result) {
    TIMFriendshipManager.getInstance().getFriendList(new TIMValueCallBack<List<TIMFriend>>() {
      @Override
      public void onError(int code, String desc) {
        Log.d(tag, "getFriendList err code = " + code);
        result.error(desc, String.valueOf(code), null);
      }

      @Override
      public void onSuccess(List<TIMFriend> timFriends) {

        if (timFriends.size() > 0 && timFriends != null) {
          result.success(new Gson().toJson(timFriends, new TypeToken<Collection<TIMFriend>>() {
          }.getType()));
        } else {
          result.success("[]");//返回一个空的json array
        }
      }
    });

  }

  private void addFriend(MethodCall call, final MethodChannel.Result result) {
    String identifier = call.argument("identifier");
    String addWords = call.argument("words");
    String source = call.argument("source");
    TIMFriendRequest timFriendRequest = new TIMFriendRequest(identifier);
    timFriendRequest.setAddWording(addWords);
    timFriendRequest.setAddSource(source);
    TIMFriendshipManager.getInstance().addFriend(timFriendRequest, new TIMValueCallBack<TIMFriendResult>() {
      @Override
      public void onError(int i, String s) {
        Log.d(tag, "addFriend err code = " + i + ", desc = " + s);
        result.error(s, String.valueOf(i), null);
      }

      @Override
      public void onSuccess(TIMFriendResult timFriendResult) {
        Log.d(tag, "addFriend success result = " + timFriendResult.toString());
        result.success(timFriendResult.getIdentifier());
      }
    });

  }

  private void deleteFriend(MethodCall call, final MethodChannel.Result result) {
    String identifier = call.argument("identifier");
    List<String> identifiers = new ArrayList<>();
    identifiers.add("test_id");
    TIMFriendshipManager.getInstance().deleteFriends(identifiers, TIMDelFriendType.TIM_FRIEND_DEL_SINGLE, new TIMValueCallBack<List<TIMFriendResult>>() {
      @Override
      public void onError(int i, String s) {
        Log.d(tag, "addFriend err code = " + i + ", desc = " + s);
        result.error(s, String.valueOf(i), null);
      }

      @Override
      public void onSuccess(List<TIMFriendResult> timUserProfiles) {
        Log.d(tag, "addFriend success result = " + timUserProfiles.toString());
        result.success(timUserProfiles.get(0).getIdentifier());
      }
    });

  }
}
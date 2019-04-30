import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class FlutterTim {
  static const MethodChannel _channel = const MethodChannel('futter_tim');
  static const EventChannel eventChannel =
  const EventChannel('futter_tim:message');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
  static get timMessageList async {
    var list = await eventChannel.receiveBroadcastStream();
    return list;
  }
  Future setup({
    int appId,
  }) async {
    var result = await  _channel.invokeMethod('initTim', {'app_id': appId});
    return result;
  }

  Future login({
    String username,
    String userSig,
  }) async {
    var result = await  _channel.invokeMethod('login', {'username': username, 'userSig': userSig});
    return result;
  }

  Future signOut() async {
    var result = await _channel.invokeMethod('signOut');
    return result;
  }

  Future getConversations() async {
    var result = await  _channel.invokeMethod('getConversations');
    return result;
  }

  Future delConversation({String identifier}) async {
    var result = await _channel.invokeMethod('delConversation',{'identifier':identifier});
    return result;
  }

  Future getMessages({
    String identifier,
    dynamic lastMsg, //从指定消息获取10条，不指定则是最新的
  }) async {
    var result = await _channel.invokeMethod('getMessages',<String, dynamic>{'identifier':identifier,'lastMsg':lastMsg});
    return result;
  }

  Future sendTextMessages({
    String identifier,
    String content,
  }) async {
    var result = await _channel.invokeMethod('sendTextMessages',{'identifier':identifier,'content':content});
    return result;
  }

  Future sendImageMessages({
    String identifier,
    String image_path,
  }) async {
    var result = await  _channel.invokeMethod('sendImageMessages',{'identifier':identifier,'content':image_path});
    return result;
  }

  Future sendSoundMessages({
    String identifier,
    String soundPath,
  }) async {
    var result = await _channel.invokeMethod('sendSoundMessages',{'identifier':identifier,'content':soundPath});
    return result;
  }

  Future sendFaceMessages({
    String identifier,
    Uint8List faceArr,
  }) async {
    var result = await  _channel.invokeMethod('sendFaceMessages',{'identifier':identifier,'faceArr':faceArr});
    return result;
  }

  Future getFriendList() async {
    var result = await _channel.invokeMethod('getFriendList');
    return result;
  }

  Future addFriend({
    String identifier,
    String addWords,
    String source,
  }) async {
    var result = await  _channel.invokeMethod('addFriend',{'identifier':identifier,'addWords':addWords,'source':source});
    return result;
  }

  Future deleteFriend({String identifier}) async {
    var result = await _channel.invokeMethod('deleteFriend',{'identifier':identifier});
    return result;
  }
}

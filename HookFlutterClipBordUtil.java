package cn.lhj.flutter.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.systemchannels.PlatformChannel;

/**
 * File description.
 * hook flutter输入框 自动调用getClipboardData 引起的合规问题
 *
 * @author lihongjun
 * @date 10/13/21
 */
public class HookFlutterClipBordUtil {

    /**
     * 代理剪切板功能
     * @param flutterEngine
     */
    public static void hookClipBoard(FlutterEngine flutterEngine) {

        Class IPlatformMessageHandler;
        Field platformMessageHandlerField;
        try {
            IPlatformMessageHandler = Class.forName(PlatformChannel.PlatformMessageHandler.class.getName());
            platformMessageHandlerField = PlatformChannel.class.getDeclaredField("platformMessageHandler");
            if (platformMessageHandlerField == null) {
                return;
            }
            platformMessageHandlerField.setAccessible(true);

            // 拿到真实正常的实例，给不需要被hook的方法使用
            PlatformChannel channel = flutterEngine.getPlatformChannel();
            Object real = platformMessageHandlerField.get(channel);
            // 代理PlatformMessageHandler的方法
            platformMessageHandlerField.set(channel, Proxy.newProxyInstance(
                    IPlatformMessageHandler.getClassLoader(), new Class[]{
                            IPlatformMessageHandler
                    }, new IClipboardInvocationHandler(real))
            );

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }


    public static class IClipboardInvocationHandler implements InvocationHandler {

        // 真是的调用方，如果不是获取剪切板 正常调用功能
        private Object real;

        public IClipboardInvocationHandler(Object real) {
            this.real = real;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // flutter输入框会调用获取剪切板 合规问题屏蔽掉
            if ("getClipboardData".equals(method.getName())) {
                return "";
            }
            return method.invoke(real, args);
        }
    }

}

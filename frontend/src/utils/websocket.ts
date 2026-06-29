import { ElNotification } from 'element-plus';
import { useNoticeStore } from '@/store/modules/notice';
import request from '@/utils/request';

// 初始化socket
export const initWebSocket = async (url: string) => {
  if (import.meta.env.VITE_APP_WEBSOCKET === 'false') {
    return;
  }
  const ticketResponse = await request({
    url: '/resource/websocket/ticket',
    method: 'get'
  });
  const wsTicket = ticketResponse?.data;
  if (!wsTicket) {
    console.warn('WebSocket ticket was not issued');
    return;
  }
  const separator = url.includes('?') ? '&' : '?';
  const connectUrl = `${url}${separator}wsTicket=${encodeURIComponent(wsTicket)}&clientid=${encodeURIComponent(import.meta.env.VITE_APP_CLIENT_ID)}`;
  useWebSocket(connectUrl, {
    autoReconnect: {
      // 重连最大次数
      retries: 3,
      // 重连间隔
      delay: 1000,
      onFailed() {
        console.log('websocket重连失败');
      }
    },
    heartbeat: {
      message: JSON.stringify({ type: 'ping' }),
      // 发送心跳的间隔
      interval: 10000,
      // 接收到心跳response的超时时间
      pongTimeout: 2000
    },
    onConnected() {
      console.log('websocket已经连接');
    },
    onDisconnected() {
      console.log('websocket已经断开');
    },
    onMessage: (_, e) => {
      if (e.data.indexOf('ping') > 0) {
        return;
      }
      useNoticeStore().addNotice({
        message: e.data,
        read: false,
        time: new Date().toLocaleString()
      });
      ElNotification({
        title: '消息',
        message: e.data,
        type: 'success',
        duration: 3000
      });
    }
  });
};

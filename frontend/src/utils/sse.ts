import { ElNotification } from 'element-plus';
import { useNoticeStore } from '@/store/modules/notice';
import request from '@/utils/request';

// 初始化
export const initSSE = async (url: string) => {
  if (import.meta.env.VITE_APP_SSE === 'false') {
    return;
  }

  const ticketResponse = await request({
    url: '/resource/sse/ticket',
    method: 'get'
  });
  const sseTicket = ticketResponse?.data;
  if (!sseTicket) {
    console.warn('SSE ticket was not issued');
    return;
  }

  const separator = url.includes('?') ? '&' : '?';
  const connectUrl = `${url}${separator}sseTicket=${encodeURIComponent(sseTicket)}&clientid=${encodeURIComponent(import.meta.env.VITE_APP_CLIENT_ID)}`;
  const { data, error } = useEventSource(connectUrl, [], {
    autoReconnect: {
      retries: 5,
      delay: 5000,
      onFailed() {
        console.log('Failed to connect after 5 retries');
      }
    }
  });

  watch(error, () => {
    console.log('SSE connection error:', error.value);
    error.value = null;
  });

  watch(data, () => {
    if (!data.value) return;
    useNoticeStore().addNotice({
      message: data.value,
      read: false,
      time: new Date().toLocaleString()
    });
    ElNotification({
      title: '消息',
      message: data.value,
      type: 'success',
      duration: 3000
    });
    data.value = null;
  });
};

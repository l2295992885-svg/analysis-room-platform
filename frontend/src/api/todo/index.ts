import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TodoTaskCloseForm, TodoTaskQuery, TodoTaskVO } from './types';

export const listMyTodos = (query: TodoTaskQuery): AxiosPromise<TodoTaskVO[]> => {
  return request({
    url: '/todos/my',
    method: 'get',
    params: query
  });
};

export const listDoneTodos = (query: TodoTaskQuery): AxiosPromise<TodoTaskVO[]> => {
  return request({
    url: '/todos/done',
    method: 'get',
    params: query
  });
};

export const getTodo = (id: string | number): AxiosPromise<TodoTaskVO> => {
  return request({
    url: `/todos/${id}`,
    method: 'get'
  });
};

export const openTodo = (id: string | number): AxiosPromise<TodoTaskVO> => {
  return request({
    url: `/todos/${id}/open`,
    method: 'post'
  });
};

export const closeTodo = (id: string | number, data: TodoTaskCloseForm) => {
  return request({
    url: `/todos/${id}/close`,
    method: 'post',
    data
  });
};

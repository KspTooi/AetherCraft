// 会话线程列表视图对象
export interface ModelRoleThreadListVo {
  // 会话线程ID
  id: number;

  // 会话标题
  title: string;

  // 会话描述
  description: string;

  // AI模型代码
  modelCode: string;

  // 是否为当前激活的对话 0-存档 1-激活
  active: number;

  // 创建时间
  createTime: string; // 使用 string 类型表示日期时间

  // 更新时间
  updateTime: string; // 使用 string 类型表示日期时间

  // 最后一条消息内容（可选，如果需要显示预览）
  lastMessage: string;
} 
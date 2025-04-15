/**
 * 聊天消息片段VO (对应后端 com.ksptool.ql.biz.model.vo.RpSegmentVo)
 */
export default interface RpSegmentVo {

  /**
   * 会话ID
   * (注意：后端为 Long，前端通常处理为 string)
   */
  threadId: string;

  /**
   * 历史记录ID
   * (注意：后端为 Long，前端通常处理为 string)
   * 仅在 type 为 2 (结束) 时有有效值
   */
  historyId: string;

  /**
   * 角色 0-用户 1-AI助手
   */
  role: 0 | 1;

  /**
   * 片段序号
   */
  sequence: number;

  /**
   * 片段内容
   */
  content: string;

  /**
   * 片段类型
   * 0: 开始
   * 1: 数据
   * 2: 结束
   * 10: 错误
   * null: 无数据 (在 TypeScript 中通常用 undefined 表示)
   */
  type: 0 | 1 | 2 | 10;

  /**
   * 角色ID (AI助手的角色ID)
   * (注意：后端为 Long，前端通常处理为 string)
   */
  roleId: string;

  /**
   * 角色名称 (AI助手的名称)
   */
  roleName: string;

  /**
   * 角色头像路径 (AI助手的头像路径)
   */
  roleAvatarPath: string;
} 
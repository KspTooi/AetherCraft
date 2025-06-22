export interface MessageItemVo {
  id: string,                        //消息记录ID(-1为临时消息)
  status: number,                    //消息状态 0:等待响应 1:正在计算 2:正在输入 3:结束
  senderName: string,                //发送人名称
  senderAvatarUrl: string,           //发送人头像
  senderRole: number,                //发送人角色 0:玩家 1:模型
  content: string,                   //消息内容
  contentThoughts: string | null,    //思考内容
  createTime: string | null          //消息时间 yyyy年mm月dd日 HH:mm:ss
}
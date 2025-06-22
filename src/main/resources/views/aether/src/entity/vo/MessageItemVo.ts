export interface MessageItemVo {
  id: string,                        //消息记录ID(-1为临时消息)
  status?: number | null,            //消息状态 0:正在计算 1:正在输入
  senderName: string,                //发送人名称
  senderAvatarUrl: string,           //发送人头像
  senderRole: number,                //发送人角色 0:玩家 1:模型
  content: string,                   //消息内容
  contentThoughts: string | null,    //思考内容
  createTime: string | null          //消息时间 yyyy年mm月dd日 HH:mm:ss
}